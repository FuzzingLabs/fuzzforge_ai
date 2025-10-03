package com.google.android.gms.common.api.internal;

import android.app.Dialog;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zan extends zabp {
    private final /* synthetic */ Dialog zaec;
    private final /* synthetic */ zal zaed;

    zan(zal zalVar, Dialog dialog) {
        this.zaed = zalVar;
        this.zaec = dialog;
    }

    @Override // com.google.android.gms.common.api.internal.zabp
    public final void zas() {
        this.zaed.zadl.zao();
        if (this.zaec.isShowing()) {
            this.zaec.dismiss();
        }
    }
}
