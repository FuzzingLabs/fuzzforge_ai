package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.internal.GoogleApiManager;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zabj implements Runnable {
    private final /* synthetic */ GoogleApiManager.zaa zaiq;
    private final /* synthetic */ ConnectionResult zajc;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zabj(GoogleApiManager.zaa zaaVar, ConnectionResult connectionResult) {
        this.zaiq = zaaVar;
        this.zajc = connectionResult;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.zaiq.onConnectionFailed(this.zajc);
    }
}
