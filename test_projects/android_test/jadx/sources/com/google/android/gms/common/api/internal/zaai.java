package com.google.android.gms.common.api.internal;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zaai extends zabd {
    private final /* synthetic */ zaaf zafy;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    zaai(zaaf zaafVar, zabb zabbVar) {
        super(zabbVar);
        this.zafy = zaafVar;
    }

    @Override // com.google.android.gms.common.api.internal.zabd
    public final void zaal() {
        this.zafy.onConnectionSuspended(1);
    }
}
