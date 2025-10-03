package com.google.android.gms.common.api.internal;

import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import java.util.concurrent.locks.Lock;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public final class zas implements zabs {
    private final /* synthetic */ zaq zaet;

    private zas(zaq zaqVar) {
        this.zaet = zaqVar;
    }

    @Override // com.google.android.gms.common.api.internal.zabs
    public final void zab(Bundle bundle) {
        Lock lock;
        Lock lock2;
        lock = this.zaet.zaer;
        lock.lock();
        try {
            this.zaet.zaa(bundle);
            this.zaet.zaeo = ConnectionResult.RESULT_SUCCESS;
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
            this.zaet.zaeo = connectionResult;
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
        ConnectionResult connectionResult;
        ConnectionResult connectionResult2;
        zabe zabeVar;
        lock = this.zaet.zaer;
        lock.lock();
        try {
            z2 = this.zaet.zaeq;
            if (!z2) {
                connectionResult = this.zaet.zaep;
                if (connectionResult != null) {
                    connectionResult2 = this.zaet.zaep;
                    if (connectionResult2.isSuccess()) {
                        this.zaet.zaeq = true;
                        zabeVar = this.zaet.zaej;
                        zabeVar.onConnectionSuspended(i);
                        return;
                    }
                }
            }
            this.zaet.zaeq = false;
            this.zaet.zaa(i, z);
        } finally {
            lock2 = this.zaet.zaer;
            lock2.unlock();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public /* synthetic */ zas(zaq zaqVar, zat zatVar) {
        this(zaqVar);
    }
}
