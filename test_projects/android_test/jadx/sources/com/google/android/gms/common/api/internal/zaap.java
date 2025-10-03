package com.google.android.gms.common.api.internal;

import java.lang.ref.WeakReference;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zaap extends com.google.android.gms.signin.internal.zad {
    private final WeakReference<zaak> zago;

    zaap(zaak zaakVar) {
        this.zago = new WeakReference<>(zaakVar);
    }

    @Override // com.google.android.gms.signin.internal.zad, com.google.android.gms.signin.internal.zac
    public final void zab(com.google.android.gms.signin.internal.zak zakVar) {
        zabe zabeVar;
        zaak zaakVar = this.zago.get();
        if (zaakVar == null) {
            return;
        }
        zabeVar = zaakVar.zafv;
        zabeVar.zaa(new zaas(this, zaakVar, zaakVar, zakVar));
    }
}
