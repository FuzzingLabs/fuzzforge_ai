package com.airbnb.lottie.network;

import android.content.Context;
import androidx.core.util.Pair;
import com.airbnb.lottie.utils.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* loaded from: classes.dex */
public class NetworkCache {
    private final Context appContext;

    public NetworkCache(Context appContext) {
        this.appContext = appContext.getApplicationContext();
    }

    public void clear() {
        File parentDir = parentDir();
        if (parentDir.exists()) {
            File[] files = parentDir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : parentDir.listFiles()) {
                    file.delete();
                }
            }
            parentDir.delete();
        }
    }

    Pair<FileExtension, InputStream> fetch(String url) {
        FileExtension extension;
        try {
            File cachedFile = getCachedFile(url);
            if (cachedFile == null) {
                return null;
            }
            try {
                FileInputStream inputStream = new FileInputStream(cachedFile);
                if (cachedFile.getAbsolutePath().endsWith(".zip")) {
                    extension = FileExtension.ZIP;
                } else {
                    extension = FileExtension.JSON;
                }
                Logger.debug("Cache hit for " + url + " at " + cachedFile.getAbsolutePath());
                return new Pair<>(extension, inputStream);
            } catch (FileNotFoundException e) {
                return null;
            }
        } catch (FileNotFoundException e2) {
            return null;
        }
    }

    File writeTempCacheFile(String url, InputStream stream, FileExtension extension) throws IOException {
        String fileName = filenameForUrl(url, extension, true);
        File file = new File(parentDir(), fileName);
        try {
            OutputStream output = new FileOutputStream(file);
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    int read = stream.read(buffer);
                    if (read != -1) {
                        output.write(buffer, 0, read);
                    } else {
                        output.flush();
                        return file;
                    }
                }
            } finally {
                output.close();
            }
        } finally {
            stream.close();
        }
    }

    void renameTempFile(String url, FileExtension extension) {
        String fileName = filenameForUrl(url, extension, true);
        File file = new File(parentDir(), fileName);
        String newFileName = file.getAbsolutePath().replace(".temp", "");
        File newFile = new File(newFileName);
        boolean renamed = file.renameTo(newFile);
        Logger.debug("Copying temp file to real file (" + newFile + ")");
        if (!renamed) {
            Logger.warning("Unable to rename cache file " + file.getAbsolutePath() + " to " + newFile.getAbsolutePath() + ".");
        }
    }

    private File getCachedFile(String url) throws FileNotFoundException {
        File jsonFile = new File(parentDir(), filenameForUrl(url, FileExtension.JSON, false));
        if (jsonFile.exists()) {
            return jsonFile;
        }
        File zipFile = new File(parentDir(), filenameForUrl(url, FileExtension.ZIP, false));
        if (zipFile.exists()) {
            return zipFile;
        }
        return null;
    }

    private File parentDir() {
        File file = new File(this.appContext.getCacheDir(), "lottie_network_cache");
        if (file.isFile()) {
            file.delete();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private static String filenameForUrl(String url, FileExtension extension, boolean isTemp) {
        StringBuilder sb = new StringBuilder();
        sb.append("lottie_cache_");
        sb.append(url.replaceAll("\\W+", ""));
        sb.append(isTemp ? extension.tempExtension() : extension.extension);
        return sb.toString();
    }
}
