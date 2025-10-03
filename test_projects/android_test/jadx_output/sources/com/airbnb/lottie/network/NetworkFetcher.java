package com.airbnb.lottie.network;

import android.content.Context;
import androidx.core.util.Pair;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieResult;
import com.airbnb.lottie.utils.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipInputStream;

/* loaded from: classes.dex */
public class NetworkFetcher {
    private final Context appContext;
    private final NetworkCache networkCache;
    private final String url;

    public static LottieResult<LottieComposition> fetchSync(Context context, String url, String cacheKey) {
        return new NetworkFetcher(context, url, cacheKey).fetchSync();
    }

    private NetworkFetcher(Context context, String url, String cacheKey) {
        Context applicationContext = context.getApplicationContext();
        this.appContext = applicationContext;
        this.url = url;
        if (cacheKey == null) {
            this.networkCache = null;
        } else {
            this.networkCache = new NetworkCache(applicationContext);
        }
    }

    public LottieResult<LottieComposition> fetchSync() {
        LottieComposition result = fetchFromCache();
        if (result != null) {
            return new LottieResult<>(result);
        }
        Logger.debug("Animation for " + this.url + " not found in cache. Fetching from network.");
        return fetchFromNetwork();
    }

    private LottieComposition fetchFromCache() {
        Pair<FileExtension, InputStream> cacheResult;
        LottieResult<LottieComposition> result;
        NetworkCache networkCache = this.networkCache;
        if (networkCache == null || (cacheResult = networkCache.fetch(this.url)) == null) {
            return null;
        }
        FileExtension extension = cacheResult.first;
        InputStream inputStream = cacheResult.second;
        if (extension == FileExtension.ZIP) {
            result = LottieCompositionFactory.fromZipStreamSync(new ZipInputStream(inputStream), this.url);
        } else {
            result = LottieCompositionFactory.fromJsonInputStreamSync(inputStream, this.url);
        }
        if (result.getValue() != null) {
            return result.getValue();
        }
        return null;
    }

    private LottieResult<LottieComposition> fetchFromNetwork() {
        try {
            return fetchFromNetworkInternal();
        } catch (IOException e) {
            return new LottieResult<>((Throwable) e);
        }
    }

    private LottieResult<LottieComposition> fetchFromNetworkInternal() throws IOException {
        Logger.debug("Fetching " + this.url);
        HttpURLConnection connection = (HttpURLConnection) new URL(this.url).openConnection();
        connection.setRequestMethod("GET");
        try {
            connection.connect();
            if (connection.getErrorStream() == null && connection.getResponseCode() == 200) {
                LottieResult<LottieComposition> result = getResultFromConnection(connection);
                StringBuilder sb = new StringBuilder();
                sb.append("Completed fetch from network. Success: ");
                sb.append(result.getValue() != null);
                Logger.debug(sb.toString());
                return result;
            }
            String error = getErrorFromConnection(connection);
            return new LottieResult<>((Throwable) new IllegalArgumentException("Unable to fetch " + this.url + ". Failed with " + connection.getResponseCode() + "\n" + error));
        } catch (Exception e) {
            return new LottieResult<>((Throwable) e);
        } finally {
            connection.disconnect();
        }
    }

    private String getErrorFromConnection(HttpURLConnection connection) throws IOException {
        connection.getResponseCode();
        BufferedReader r = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        StringBuilder error = new StringBuilder();
        while (true) {
            try {
                try {
                    String line = r.readLine();
                    if (line != null) {
                        error.append(line);
                        error.append('\n');
                    } else {
                        try {
                            break;
                        } catch (Exception e) {
                        }
                    }
                } catch (Throwable th) {
                    try {
                        r.close();
                    } catch (Exception e2) {
                    }
                    throw th;
                }
            } catch (Exception e3) {
                throw e3;
            }
        }
        r.close();
        return error.toString();
    }

    private LottieResult<LottieComposition> getResultFromConnection(HttpURLConnection connection) throws IOException {
        FileExtension extension;
        LottieResult<LottieComposition> result;
        String contentType = connection.getContentType();
        if (contentType == null) {
            contentType = "application/json";
        }
        if (contentType.contains("application/zip")) {
            Logger.debug("Handling zip response.");
            extension = FileExtension.ZIP;
            NetworkCache networkCache = this.networkCache;
            if (networkCache == null) {
                result = LottieCompositionFactory.fromZipStreamSync(new ZipInputStream(connection.getInputStream()), null);
            } else {
                File file = networkCache.writeTempCacheFile(this.url, connection.getInputStream(), extension);
                result = LottieCompositionFactory.fromZipStreamSync(new ZipInputStream(new FileInputStream(file)), this.url);
            }
        } else {
            Logger.debug("Received json response.");
            extension = FileExtension.JSON;
            NetworkCache networkCache2 = this.networkCache;
            if (networkCache2 == null) {
                result = LottieCompositionFactory.fromJsonInputStreamSync(connection.getInputStream(), null);
            } else {
                File file2 = networkCache2.writeTempCacheFile(this.url, connection.getInputStream(), extension);
                result = LottieCompositionFactory.fromJsonInputStreamSync(new FileInputStream(new File(file2.getAbsolutePath())), this.url);
            }
        }
        if (this.networkCache != null && result.getValue() != null) {
            this.networkCache.renameTempFile(this.url, extension);
        }
        return result;
    }
}
