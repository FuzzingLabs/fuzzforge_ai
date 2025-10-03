package com.google.common.cache;

import com.google.common.base.Preconditions;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public final class RemovalListeners {
    private RemovalListeners() {
    }

    public static <K, V> RemovalListener<K, V> asynchronous(final RemovalListener<K, V> listener, final Executor executor) {
        Preconditions.checkNotNull(listener);
        Preconditions.checkNotNull(executor);
        return new RemovalListener<K, V>() { // from class: com.google.common.cache.RemovalListeners.1
            @Override // com.google.common.cache.RemovalListener
            public void onRemoval(final RemovalNotification<K, V> notification) {
                executor.execute(new Runnable() { // from class: com.google.common.cache.RemovalListeners.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        listener.onRemoval(notification);
                    }
                });
            }
        };
    }
}
