package com.google.common.cache;

@FunctionalInterface
/* loaded from: classes.dex */
public interface RemovalListener<K, V> {
    void onRemoval(RemovalNotification<K, V> removalNotification);
}
