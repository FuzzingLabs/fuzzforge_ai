package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiActivity;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zal implements Runnable {
    private final zam zadk;
    final /* synthetic */ zak zadl;

    zal(zak zakVar, zam zamVar) {
        this.zadl = zakVar;
        this.zadk = zamVar;
    }

    @Override // java.lang.Runnable
    public final void run() {
        if (!this.zadl.zadh) {
            return;
        }
        ConnectionResult connectionResult = this.zadk.getConnectionResult();
        if (connectionResult.hasResolution()) {
            this.zadl.mLifecycleFragment.startActivityForResult(GoogleApiActivity.zaa(this.zadl.getActivity(), connectionResult.getResolution(), this.zadk.zap(), false), 1);
            return;
        }
        if (this.zadl.zace.isUserResolvableError(connectionResult.getErrorCode())) {
            this.zadl.zace.zaa(this.zadl.getActivity(), this.zadl.mLifecycleFragment, connectionResult.getErrorCode(), 2, this.zadl);
        } else if (connectionResult.getErrorCode() == 18) {
            this.zadl.zace.zaa(this.zadl.getActivity().getApplicationContext(), new zan(this, GoogleApiAvailability.zaa(this.zadl.getActivity(), this.zadl)));
        } else {
            this.zadl.zaa(connectionResult, this.zadk.zap());
        }
    }
}
