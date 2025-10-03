package com.google.android.gms.common.api.internal;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zacg implements Runnable {
    private final /* synthetic */ com.google.android.gms.signin.internal.zak zagu;
    private final /* synthetic */ zace zakl;

    zacg(zace zaceVar, com.google.android.gms.signin.internal.zak zakVar) {
        this.zakl = zaceVar;
        this.zagu = zakVar;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.zakl.zac(this.zagu);
    }
}
