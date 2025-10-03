package com.airbnb.lottie.model;

import androidx.collection.LruCache;
import com.airbnb.lottie.LottieComposition;

/* loaded from: classes.dex */
public class LottieCompositionCache {
    private static final LottieCompositionCache INSTANCE = new LottieCompositionCache();
    private final LruCache<String, LottieComposition> cache = new LruCache<>(20);

    public static LottieCompositionCache getInstance() {
        return INSTANCE;
    }

    LottieCompositionCache() {
    }

    public LottieComposition get(String cacheKey) {
        if (cacheKey == null) {
            return null;
        }
        return this.cache.get(cacheKey);
    }

    public void put(String cacheKey, LottieComposition composition) {
        if (cacheKey == null) {
            return;
        }
        this.cache.put(cacheKey, composition);
    }

    public void clear() {
        this.cache.evictAll();
    }

    public void resize(int size) {
        this.cache.resize(size);
    }
}
