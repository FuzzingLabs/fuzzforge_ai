package com.google.android.gms.common.api.internal;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public final class zaco implements zacq {
    private final /* synthetic */ zacp zala;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zaco(zacp zacpVar) {
        this.zala = zacpVar;
    }

    @Override // com.google.android.gms.common.api.internal.zacq
    public final void zab(BasePendingResult<?> basePendingResult) {
        this.zala.zald.remove(basePendingResult);
        basePendingResult.zal();
    }
}
