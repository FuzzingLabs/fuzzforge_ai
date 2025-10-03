package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.ConnectionResult;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public final class zacd implements Runnable {
    private final /* synthetic */ zace zakl;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zacd(zace zaceVar) {
        this.zakl = zaceVar;
    }

    @Override // java.lang.Runnable
    public final void run() {
        zacf zacfVar;
        zacfVar = this.zakl.zakn;
        zacfVar.zag(new ConnectionResult(4));
    }
}
