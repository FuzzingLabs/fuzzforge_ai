package com.google.firebase.database.core.persistence;

/* loaded from: classes.dex */
public class LRUCachePolicy implements CachePolicy {
    private static final long MAX_NUMBER_OF_PRUNABLE_QUERIES_TO_KEEP = 1000;
    private static final float PERCENT_OF_QUERIES_TO_PRUNE_AT_ONCE = 0.2f;
    private static final long SERVER_UPDATES_BETWEEN_CACHE_SIZE_CHECKS = 1000;
    public final long maxSizeBytes;

    public LRUCachePolicy(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    @Override // com.google.firebase.database.core.persistence.CachePolicy
    public boolean shouldPrune(long currentSizeBytes, long countOfPrunableQueries) {
        return currentSizeBytes > this.maxSizeBytes || countOfPrunableQueries > 1000;
    }

    @Override // com.google.firebase.database.core.persistence.CachePolicy
    public boolean shouldCheckCacheSize(long serverUpdatesSinceLastCheck) {
        return serverUpdatesSinceLastCheck > 1000;
    }

    @Override // com.google.firebase.database.core.persistence.CachePolicy
    public float getPercentOfQueriesToPruneAtOnce() {
        return 0.2f;
    }

    @Override // com.google.firebase.database.core.persistence.CachePolicy
    public long getMaxNumberOfQueriesToKeep() {
        return 1000L;
    }
}
