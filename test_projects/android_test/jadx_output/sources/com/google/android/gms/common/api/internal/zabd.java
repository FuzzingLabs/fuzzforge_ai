package com.google.android.gms.common.api.internal;

import java.util.concurrent.locks.Lock;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public abstract class zabd {
    private final zabb zahq;

    /* JADX INFO: Access modifiers changed from: protected */
    public zabd(zabb zabbVar) {
        this.zahq = zabbVar;
    }

    protected abstract void zaal();

    public final void zaa(zabe zabeVar) {
        Lock lock;
        Lock lock2;
        zabb zabbVar;
        lock = zabeVar.zaer;
        lock.lock();
        try {
            zabbVar = zabeVar.zahu;
            if (zabbVar != this.zahq) {
                return;
            }
            zaal();
        } finally {
            lock2 = zabeVar.zaer;
            lock2.unlock();
        }
    }
}
