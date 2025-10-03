package com.google.android.gms.common.api.internal;

import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.internal.GoogleApiManager;
import java.util.Map;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zabo implements Runnable {
    private final /* synthetic */ ConnectionResult zajc;
    private final /* synthetic */ GoogleApiManager.zab zajk;

    zabo(GoogleApiManager.zab zabVar, ConnectionResult connectionResult) {
        this.zajk = zabVar;
        this.zajc = connectionResult;
    }

    @Override // java.lang.Runnable
    public final void run() {
        ApiKey apiKey;
        Api.Client client;
        Api.Client client2;
        Api.Client client3;
        Map map = GoogleApiManager.this.zaim;
        apiKey = this.zajk.zaft;
        GoogleApiManager.zaa zaaVar = (GoogleApiManager.zaa) map.get(apiKey);
        if (zaaVar == null) {
            return;
        }
        if (!this.zajc.isSuccess()) {
            zaaVar.onConnectionFailed(this.zajc);
            return;
        }
        GoogleApiManager.zab.zaa(this.zajk, true);
        client = this.zajk.zais;
        if (client.requiresSignIn()) {
            this.zajk.zabp();
            return;
        }
        try {
            client2 = this.zajk.zais;
            client3 = this.zajk.zais;
            client2.getRemoteService(null, client3.getScopesForConnectionlessNonSignIn());
        } catch (SecurityException e) {
            Log.e("GoogleApiManager", "Failed to get service from broker. ", e);
            zaaVar.onConnectionFailed(new ConnectionResult(10));
        }
    }
}
