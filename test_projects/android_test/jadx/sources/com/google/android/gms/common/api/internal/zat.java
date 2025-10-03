package com.google.android.gms.common.api.internal;

import java.util.concurrent.locks.Lock;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zat implements Runnable {
    private final /* synthetic */ zaq zaet;

    zat(zaq zaqVar) {
        this.zaet = zaqVar;
    }

    @Override // java.lang.Runnable
    public final void run() {
        Lock lock;
        Lock lock2;
        lock = this.zaet.zaer;
        lock.lock();
        try {
            this.zaet.zav();
        } finally {
            lock2 = this.zaet.zaer;
            lock2.unlock();
        }
    }
}
