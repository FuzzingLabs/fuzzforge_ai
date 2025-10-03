package com.google.firebase.database.core.persistence;

import kotlin.jvm.internal.LongCompanionObject;

/* loaded from: classes.dex */
public interface CachePolicy {
    public static final CachePolicy NONE = new CachePolicy() { // from class: com.google.firebase.database.core.persistence.CachePolicy.1
        @Override // com.google.firebase.database.core.persistence.CachePolicy
        public boolean shouldPrune(long currentSizeBytes, long countOfPrunableQueries) {
            return false;
        }

        @Override // com.google.firebase.database.core.persistence.CachePolicy
        public boolean shouldCheckCacheSize(long serverUpdatesSinceLastCheck) {
            return false;
        }

        @Override // com.google.firebase.database.core.persistence.CachePolicy
        public float getPercentOfQueriesToPruneAtOnce() {
            return 0.0f;
        }

        @Override // com.google.firebase.database.core.persistence.CachePolicy
        public long getMaxNumberOfQueriesToKeep() {
            return LongCompanionObject.MAX_VALUE;
        }
    };

    long getMaxNumberOfQueriesToKeep();

    float getPercentOfQueriesToPruneAtOnce();

    boolean shouldCheckCacheSize(long j);

    boolean shouldPrune(long j, long j2);
}
