package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.ConnectionResult;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zacd implements Runnable {
    private final /* synthetic */ zace zakl;

    zacd(zace zaceVar) {
        this.zakl = zaceVar;
    }

    @Override // java.lang.Runnable
    public final void run() {
        zacf zacfVar;
        zacfVar = this.zakl.zakn;
        zacfVar.zag(new ConnectionResult(4));
    }
}
