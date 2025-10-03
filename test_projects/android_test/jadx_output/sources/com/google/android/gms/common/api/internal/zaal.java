package com.google.android.gms.common.api.internal;

import android.content.Context;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.internal.GoogleApiAvailabilityCache;
import java.util.ArrayList;
import java.util.Map;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public final class zaal extends zaau {
    final /* synthetic */ zaak zafz;
    private final Map<Api.Client, zaam> zagn;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public zaal(zaak zaakVar, Map<Api.Client, zaam> map) {
        super(zaakVar, null);
        this.zafz = zaakVar;
        this.zagn = map;
    }

    @Override // com.google.android.gms.common.api.internal.zaau
    public final void zaal() {
        GoogleApiAvailabilityLight googleApiAvailabilityLight;
        Context context;
        boolean z;
        Context context2;
        zabe zabeVar;
        com.google.android.gms.signin.zac zacVar;
        com.google.android.gms.signin.zac zacVar2;
        zabe zabeVar2;
        Context context3;
        boolean z2;
        googleApiAvailabilityLight = this.zafz.zaey;
        GoogleApiAvailabilityCache googleApiAvailabilityCache = new GoogleApiAvailabilityCache(googleApiAvailabilityLight);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (Api.Client client : this.zagn.keySet()) {
            if (client.requiresGooglePlayServices()) {
                z2 = this.zagn.get(client).zaee;
                if (!z2) {
                    arrayList.add(client);
                }
            }
            arrayList2.add(client);
        }
        int i = -1;
        int i2 = 0;
        if (!arrayList.isEmpty()) {
            int size = arrayList.size();
            while (i2 < size) {
                Object obj = arrayList.get(i2);
                i2++;
                context = this.zafz.mContext;
                i = googleApiAvailabilityCache.getClientAvailability(context, (Api.Client) obj);
                if (i != 0) {
                    break;
                }
            }
        } else {
            int size2 = arrayList2.size();
            while (i2 < size2) {
                Object obj2 = arrayList2.get(i2);
                i2++;
                context3 = this.zafz.mContext;
                i = googleApiAvailabilityCache.getClientAvailability(context3, (Api.Client) obj2);
                if (i == 0) {
                    break;
                }
            }
        }
        if (i != 0) {
            ConnectionResult connectionResult = new ConnectionResult(i, null);
            zabeVar2 = this.zafz.zafv;
            zabeVar2.zaa(new zaao(this, this.zafz, connectionResult));
            return;
        }
        z = this.zafz.zagh;
        if (z) {
            zacVar = this.zafz.zagf;
            if (zacVar != null) {
                zacVar2 = this.zafz.zagf;
                zacVar2.connect();
            }
        }
        for (Api.Client client2 : this.zagn.keySet()) {
            zaam zaamVar = this.zagn.get(client2);
            if (client2.requiresGooglePlayServices()) {
                context2 = this.zafz.mContext;
                if (googleApiAvailabilityCache.getClientAvailability(context2, client2) != 0) {
                    zabeVar = this.zafz.zafv;
                    zabeVar.zaa(new zaan(this, this.zafz, zaamVar));
                }
            }
            client2.connect(zaamVar);
        }
    }
}
