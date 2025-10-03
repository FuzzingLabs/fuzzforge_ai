package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.ConnectionResult;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zaao extends zabd {
    private final /* synthetic */ ConnectionResult zagq;
    private final /* synthetic */ zaal zagr;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public zaao(zaal zaalVar, zabb zabbVar, ConnectionResult connectionResult) {
        super(zabbVar);
        this.zagr = zaalVar;
        this.zagq = connectionResult;
    }

    @Override // com.google.android.gms.common.api.internal.zabd
    public final void zaal() {
        this.zagr.zafz.zae(this.zagq);
    }
}
