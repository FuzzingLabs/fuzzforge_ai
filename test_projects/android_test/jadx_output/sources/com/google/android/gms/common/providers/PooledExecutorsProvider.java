package com.google.android.gms.common.providers;

import java.util.concurrent.ScheduledExecutorService;

/* loaded from: classes.dex */
public class PooledExecutorsProvider {
    private static PooledExecutorFactory zzey;

    /* loaded from: classes.dex */
    public interface PooledExecutorFactory {
        ScheduledExecutorService newSingleThreadScheduledExecutor();
    }

    public static synchronized PooledExecutorFactory getInstance() {
        PooledExecutorFactory pooledExecutorFactory;
        synchronized (PooledExecutorsProvider.class) {
            if (zzey == null) {
                zzey = new zza();
            }
            pooledExecutorFactory = zzey;
        }
        return pooledExecutorFactory;
    }

    private PooledExecutorsProvider() {
    }
}
