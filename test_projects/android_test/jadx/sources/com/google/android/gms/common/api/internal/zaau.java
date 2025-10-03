package com.google.android.gms.common.api.internal;

import java.util.concurrent.locks.Lock;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
abstract class zaau implements Runnable {
    private final /* synthetic */ zaak zafz;

    private zaau(zaak zaakVar) {
        this.zafz = zaakVar;
    }

    protected abstract void zaal();

    @Override // java.lang.Runnable
    public void run() {
        Lock lock;
        Lock lock2;
        zabe zabeVar;
        lock = this.zafz.zaer;
        lock.lock();
        try {
            if (Thread.interrupted()) {
                return;
            }
            zaal();
        } catch (RuntimeException e) {
            zabeVar = this.zafz.zafv;
            zabeVar.zab(e);
        } finally {
            lock2 = this.zafz.zaer;
            lock2.unlock();
        }
    }

    /* synthetic */ zaau(zaak zaakVar, zaaj zaajVar) {
        this(zaakVar);
    }
}
