package com.google.android.gms.common.api.internal;

import android.os.Looper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.internal.BaseGmsClient;
import com.google.android.gms.common.internal.Preconditions;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zaam implements BaseGmsClient.ConnectionProgressReportCallbacks {
    private final Api<?> mApi;
    private final boolean zaee;
    private final WeakReference<zaak> zago;

    public zaam(zaak zaakVar, Api<?> api, boolean z) {
        this.zago = new WeakReference<>(zaakVar);
        this.mApi = api;
        this.zaee = z;
    }

    @Override // com.google.android.gms.common.internal.BaseGmsClient.ConnectionProgressReportCallbacks
    public final void onReportServiceBinding(ConnectionResult connectionResult) {
        zabe zabeVar;
        Lock lock;
        Lock lock2;
        boolean zac;
        boolean zaam;
        zaak zaakVar = this.zago.get();
        if (zaakVar == null) {
            return;
        }
        Looper myLooper = Looper.myLooper();
        zabeVar = zaakVar.zafv;
        Preconditions.checkState(myLooper == zabeVar.zaeh.getLooper(), "onReportServiceBinding must be called on the GoogleApiClient handler thread");
        lock = zaakVar.zaer;
        lock.lock();
        try {
            zac = zaakVar.zac(0);
            if (!zac) {
                return;
            }
            if (!connectionResult.isSuccess()) {
                zaakVar.zab(connectionResult, this.mApi, this.zaee);
            }
            zaam = zaakVar.zaam();
            if (zaam) {
                zaakVar.zaan();
            }
        } finally {
            lock2 = zaakVar.zaer;
            lock2.unlock();
        }
    }
}
