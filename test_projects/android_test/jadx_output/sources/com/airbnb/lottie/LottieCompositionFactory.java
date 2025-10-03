package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.airbnb.lottie.model.LottieCompositionCache;
import com.airbnb.lottie.network.NetworkCache;
import com.airbnb.lottie.network.NetworkFetcher;
import com.airbnb.lottie.parser.LottieCompositionMoshiParser;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.Utils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import okio.Okio;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class LottieCompositionFactory {
    private static final Map<String, LottieTask<LottieComposition>> taskCache = new HashMap();

    private LottieCompositionFactory() {
    }

    public static void setMaxCacheSize(int size) {
        LottieCompositionCache.getInstance().resize(size);
    }

    public static void clearCache(Context context) {
        taskCache.clear();
        LottieCompositionCache.getInstance().clear();
        new NetworkCache(context).clear();
    }

    public static LottieTask<LottieComposition> fromUrl(Context context, String url) {
        return fromUrl(context, url, "url_" + url);
    }

    public static LottieTask<LottieComposition> fromUrl(final Context context, final String url, final String cacheKey) {
        return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieCompositionFactory.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public LottieResult<LottieComposition> call() {
                return NetworkFetcher.fetchSync(context, url, cacheKey);
            }
        });
    }

    public static LottieResult<LottieComposition> fromUrlSync(Context context, String url) {
        return fromUrlSync(context, url, url);
    }

    public static LottieResult<LottieComposition> fromUrlSync(Context context, String url, String cacheKey) {
        return NetworkFetcher.fetchSync(context, url, cacheKey);
    }

    public static LottieTask<LottieComposition> fromAsset(Context context, String fileName) {
        String cacheKey = "asset_" + fileName;
        return fromAsset(context, fileName, cacheKey);
    }

    public static LottieTask<LottieComposition> fromAsset(Context context, final String fileName, final String cacheKey) {
        final Context appContext = context.getApplicationContext();
        return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieCompositionFactory.2
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public LottieResult<LottieComposition> call() {
                return LottieCompositionFactory.fromAssetSync(appContext, fileName, cacheKey);
            }
        });
    }

    public static LottieResult<LottieComposition> fromAssetSync(Context context, String fileName) {
        String cacheKey = "asset_" + fileName;
        return fromAssetSync(context, fileName, cacheKey);
    }

    public static LottieResult<LottieComposition> fromAssetSync(Context context, String fileName, String cacheKey) {
        try {
            if (fileName.endsWith(".zip")) {
                return fromZipStreamSync(new ZipInputStream(context.getAssets().open(fileName)), cacheKey);
            }
            return fromJsonInputStreamSync(context.getAssets().open(fileName), cacheKey);
        } catch (IOException e) {
            return new LottieResult<>((Throwable) e);
        }
    }

    public static LottieTask<LottieComposition> fromRawRes(Context context, int rawRes) {
        return fromRawRes(context, rawRes, rawResCacheKey(context, rawRes));
    }

    public static LottieTask<LottieComposition> fromRawRes(Context context, final int rawRes, String cacheKey) {
        final WeakReference<Context> contextRef = new WeakReference<>(context);
        final Context appContext = context.getApplicationContext();
        return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieCompositionFactory.3
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public LottieResult<LottieComposition> call() {
                Context originalContext = (Context) contextRef.get();
                Context context2 = originalContext != null ? originalContext : appContext;
                return LottieCompositionFactory.fromRawResSync(context2, rawRes);
            }
        });
    }

    public static LottieResult<LottieComposition> fromRawResSync(Context context, int rawRes) {
        return fromRawResSync(context, rawRes, rawResCacheKey(context, rawRes));
    }

    public static LottieResult<LottieComposition> fromRawResSync(Context context, int rawRes, String cacheKey) {
        try {
            return fromJsonInputStreamSync(context.getResources().openRawResource(rawRes), cacheKey);
        } catch (Resources.NotFoundException e) {
            return new LottieResult<>((Throwable) e);
        }
    }

    private static String rawResCacheKey(Context context, int resId) {
        StringBuilder sb = new StringBuilder();
        sb.append("rawRes");
        sb.append(isNightMode(context) ? "_night_" : "_day_");
        sb.append(resId);
        return sb.toString();
    }

    private static boolean isNightMode(Context context) {
        int nightModeMasked = context.getResources().getConfiguration().uiMode & 48;
        return nightModeMasked == 32;
    }

    public static LottieTask<LottieComposition> fromJsonInputStream(final InputStream stream, final String cacheKey) {
        return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieCompositionFactory.4
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public LottieResult<LottieComposition> call() {
                return LottieCompositionFactory.fromJsonInputStreamSync(stream, cacheKey);
            }
        });
    }

    public static LottieResult<LottieComposition> fromJsonInputStreamSync(InputStream stream, String cacheKey) {
        return fromJsonInputStreamSync(stream, cacheKey, true);
    }

    private static LottieResult<LottieComposition> fromJsonInputStreamSync(InputStream stream, String cacheKey, boolean close) {
        try {
            return fromJsonReaderSync(JsonReader.m15of(Okio.buffer(Okio.source(stream))), cacheKey);
        } finally {
            if (close) {
                Utils.closeQuietly(stream);
            }
        }
    }

    @Deprecated
    public static LottieTask<LottieComposition> fromJson(final JSONObject json, final String cacheKey) {
        return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieCompositionFactory.5
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public LottieResult<LottieComposition> call() {
                return LottieCompositionFactory.fromJsonSync(json, cacheKey);
            }
        });
    }

    @Deprecated
    public static LottieResult<LottieComposition> fromJsonSync(JSONObject json, String cacheKey) {
        return fromJsonStringSync(json.toString(), cacheKey);
    }

    public static LottieTask<LottieComposition> fromJsonString(final String json, final String cacheKey) {
        return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieCompositionFactory.6
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public LottieResult<LottieComposition> call() {
                return LottieCompositionFactory.fromJsonStringSync(json, cacheKey);
            }
        });
    }

    public static LottieResult<LottieComposition> fromJsonStringSync(String json, String cacheKey) {
        ByteArrayInputStream stream = new ByteArrayInputStream(json.getBytes());
        return fromJsonReaderSync(JsonReader.m15of(Okio.buffer(Okio.source(stream))), cacheKey);
    }

    public static LottieTask<LottieComposition> fromJsonReader(final JsonReader reader, final String cacheKey) {
        return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieCompositionFactory.7
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public LottieResult<LottieComposition> call() {
                return LottieCompositionFactory.fromJsonReaderSync(JsonReader.this, cacheKey);
            }
        });
    }

    public static LottieResult<LottieComposition> fromJsonReaderSync(JsonReader reader, String cacheKey) {
        return fromJsonReaderSyncInternal(reader, cacheKey, true);
    }

    private static LottieResult<LottieComposition> fromJsonReaderSyncInternal(JsonReader reader, String cacheKey, boolean close) {
        try {
            try {
                LottieComposition composition = LottieCompositionMoshiParser.parse(reader);
                if (cacheKey != null) {
                    LottieCompositionCache.getInstance().put(cacheKey, composition);
                }
                LottieResult<LottieComposition> lottieResult = new LottieResult<>(composition);
                if (close) {
                    Utils.closeQuietly(reader);
                }
                return lottieResult;
            } catch (Exception e) {
                LottieResult<LottieComposition> lottieResult2 = new LottieResult<>(e);
                if (close) {
                    Utils.closeQuietly(reader);
                }
                return lottieResult2;
            }
        } catch (Throwable th) {
            if (close) {
                Utils.closeQuietly(reader);
            }
            throw th;
        }
    }

    public static LottieTask<LottieComposition> fromZipStream(final ZipInputStream inputStream, final String cacheKey) {
        return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieCompositionFactory.8
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // java.util.concurrent.Callable
            public LottieResult<LottieComposition> call() {
                return LottieCompositionFactory.fromZipStreamSync(inputStream, cacheKey);
            }
        });
    }

    public static LottieResult<LottieComposition> fromZipStreamSync(ZipInputStream inputStream, String cacheKey) {
        try {
            return fromZipStreamSyncInternal(inputStream, cacheKey);
        } finally {
            Utils.closeQuietly(inputStream);
        }
    }

    private static LottieResult<LottieComposition> fromZipStreamSyncInternal(ZipInputStream inputStream, String cacheKey) {
        LottieComposition composition = null;
        Map<String, Bitmap> images = new HashMap<>();
        try {
            ZipEntry entry = inputStream.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();
                if (entryName.contains("__MACOSX")) {
                    inputStream.closeEntry();
                } else if (entry.getName().contains(".json")) {
                    JsonReader reader = JsonReader.m15of(Okio.buffer(Okio.source(inputStream)));
                    composition = fromJsonReaderSyncInternal(reader, null, false).getValue();
                } else {
                    if (!entryName.contains(".png") && !entryName.contains(".webp")) {
                        inputStream.closeEntry();
                    }
                    String[] splitName = entryName.split("/");
                    String name = splitName[splitName.length - 1];
                    images.put(name, BitmapFactory.decodeStream(inputStream));
                }
                entry = inputStream.getNextEntry();
            }
            if (composition == null) {
                return new LottieResult<>((Throwable) new IllegalArgumentException("Unable to parse composition"));
            }
            for (Map.Entry<String, Bitmap> e : images.entrySet()) {
                LottieImageAsset imageAsset = findImageAssetForFileName(composition, e.getKey());
                if (imageAsset != null) {
                    imageAsset.setBitmap(Utils.resizeBitmapIfNeeded(e.getValue(), imageAsset.getWidth(), imageAsset.getHeight()));
                }
            }
            for (Map.Entry<String, LottieImageAsset> entry2 : composition.getImages().entrySet()) {
                if (entry2.getValue().getBitmap() == null) {
                    return new LottieResult<>((Throwable) new IllegalStateException("There is no image for " + entry2.getValue().getFileName()));
                }
            }
            if (cacheKey != null) {
                LottieCompositionCache.getInstance().put(cacheKey, composition);
            }
            return new LottieResult<>(composition);
        } catch (IOException e2) {
            return new LottieResult<>((Throwable) e2);
        }
    }

    private static LottieImageAsset findImageAssetForFileName(LottieComposition composition, String fileName) {
        for (LottieImageAsset asset : composition.getImages().values()) {
            if (asset.getFileName().equals(fileName)) {
                return asset;
            }
        }
        return null;
    }

    private static LottieTask<LottieComposition> cache(final String cacheKey, Callable<LottieResult<LottieComposition>> callable) {
        final LottieComposition cachedComposition = cacheKey == null ? null : LottieCompositionCache.getInstance().get(cacheKey);
        if (cachedComposition != null) {
            return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() { // from class: com.airbnb.lottie.LottieCompositionFactory.9
                /* JADX WARN: Can't rename method to resolve collision */
                @Override // java.util.concurrent.Callable
                public LottieResult<LottieComposition> call() {
                    return new LottieResult<>(LottieComposition.this);
                }
            });
        }
        if (cacheKey != null) {
            Map<String, LottieTask<LottieComposition>> map = taskCache;
            if (map.containsKey(cacheKey)) {
                return map.get(cacheKey);
            }
        }
        LottieTask<LottieComposition> task = new LottieTask<>(callable);
        if (cacheKey != null) {
            task.addListener(new LottieListener<LottieComposition>() { // from class: com.airbnb.lottie.LottieCompositionFactory.10
                @Override // com.airbnb.lottie.LottieListener
                public void onResult(LottieComposition result) {
                    LottieCompositionFactory.taskCache.remove(cacheKey);
                }
            });
            task.addFailureListener(new LottieListener<Throwable>() { // from class: com.airbnb.lottie.LottieCompositionFactory.11
                @Override // com.airbnb.lottie.LottieListener
                public void onResult(Throwable result) {
                    LottieCompositionFactory.taskCache.remove(cacheKey);
                }
            });
            taskCache.put(cacheKey, task);
        }
        return task;
    }
}
