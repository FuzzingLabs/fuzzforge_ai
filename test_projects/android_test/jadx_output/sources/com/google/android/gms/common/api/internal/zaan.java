package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.internal.BaseGmsClient;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zaan extends zabd {
    private final /* synthetic */ BaseGmsClient.ConnectionProgressReportCallbacks zagp;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public zaan(zaal zaalVar, zabb zabbVar, BaseGmsClient.ConnectionProgressReportCallbacks connectionProgressReportCallbacks) {
        super(zabbVar);
        this.zagp = connectionProgressReportCallbacks;
    }

    @Override // com.google.android.gms.common.api.internal.zabd
    public final void zaal() {
        this.zagp.onReportServiceBinding(new ConnectionResult(16, null));
    }
}
