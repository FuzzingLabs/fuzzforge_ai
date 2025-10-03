package com.google.android.gms.common.api.internal;

import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import java.util.concurrent.locks.Lock;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zau implements zabs {
    private final /* synthetic */ zaq zaet;

    private zau(zaq zaqVar) {
        this.zaet = zaqVar;
    }

    @Override // com.google.android.gms.common.api.internal.zabs
    public final void zab(Bundle bundle) {
        Lock lock;
        Lock lock2;
        lock = this.zaet.zaer;
        lock.lock();
        try {
            this.zaet.zaep = ConnectionResult.RESULT_SUCCESS;
            this.zaet.zav();
        } finally {
            lock2 = this.zaet.zaer;
            lock2.unlock();
        }
    }

    @Override // com.google.android.gms.common.api.internal.zabs
    public final void zac(ConnectionResult connectionResult) {
        Lock lock;
        Lock lock2;
        lock = this.zaet.zaer;
        lock.lock();
        try {
            this.zaet.zaep = connectionResult;
            this.zaet.zav();
        } finally {
            lock2 = this.zaet.zaer;
            lock2.unlock();
        }
    }

    @Override // com.google.android.gms.common.api.internal.zabs
    public final void zab(int i, boolean z) {
        Lock lock;
        Lock lock2;
        boolean z2;
        zabe zabeVar;
        lock = this.zaet.zaer;
        lock.lock();
        try {
            z2 = this.zaet.zaeq;
            if (z2) {
                this.zaet.zaeq = false;
                this.zaet.zaa(i, z);
            } else {
                this.zaet.zaeq = true;
                zabeVar = this.zaet.zaei;
                zabeVar.onConnectionSuspended(i);
            }
        } finally {
            lock2 = this.zaet.zaer;
            lock2.unlock();
        }
    }

    /* synthetic */ zau(zaq zaqVar, zat zatVar) {
        this(zaqVar);
    }
}
