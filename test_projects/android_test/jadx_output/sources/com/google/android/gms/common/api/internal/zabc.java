package com.google.android.gms.common.api.internal;

import java.lang.ref.WeakReference;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public final class zabc extends zabp {
    private WeakReference<zaaw> zahp;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zabc(zaaw zaawVar) {
        this.zahp = new WeakReference<>(zaawVar);
    }

    @Override // com.google.android.gms.common.api.internal.zabp
    public final void zas() {
        zaaw zaawVar = this.zahp.get();
        if (zaawVar == null) {
            return;
        }
        zaawVar.resume();
    }
}
