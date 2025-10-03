package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.api.Api;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zabl implements Runnable {
    private final /* synthetic */ zabm zajd;

    zabl(zabm zabmVar) {
        this.zajd = zabmVar;
    }

    @Override // java.lang.Runnable
    public final void run() {
        Api.Client client;
        client = this.zajd.zaiq.zais;
        client.disconnect();
    }
}
