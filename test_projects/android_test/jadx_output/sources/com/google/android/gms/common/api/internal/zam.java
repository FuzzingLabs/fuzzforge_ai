package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.internal.Preconditions;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zam {
    private final int zadm;
    private final ConnectionResult zadn;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zam(ConnectionResult connectionResult, int i) {
        Preconditions.checkNotNull(connectionResult);
        this.zadn = connectionResult;
        this.zadm = i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final int zap() {
        return this.zadm;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final ConnectionResult getConnectionResult() {
        return this.zadn;
    }
}
