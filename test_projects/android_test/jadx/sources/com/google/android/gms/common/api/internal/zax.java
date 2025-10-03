package com.google.android.gms.common.api.internal;

import android.util.Log;
import androidx.collection.ArrayMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.AvailabilityException;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zax implements OnCompleteListener<Map<ApiKey<?>, String>> {
    private final /* synthetic */ zav zafl;

    private zax(zav zavVar) {
        this.zafl = zavVar;
    }

    @Override // com.google.android.gms.tasks.OnCompleteListener
    public final void onComplete(Task<Map<ApiKey<?>, String>> task) {
        Lock lock;
        Lock lock2;
        boolean z;
        boolean z2;
        ConnectionResult zaac;
        Map map;
        Map map2;
        boolean zaa;
        Map map3;
        Map map4;
        Map map5;
        ConnectionResult connectionResult;
        zaaw zaawVar;
        ConnectionResult connectionResult2;
        Condition condition;
        Map map6;
        Map map7;
        ConnectionResult zaac2;
        Map map8;
        Map map9;
        Map map10;
        lock = this.zafl.zaer;
        lock.lock();
        try {
            z = this.zafl.zafe;
            if (z) {
                if (task.isSuccessful()) {
                    zav zavVar = this.zafl;
                    map8 = this.zafl.zaeu;
                    zavVar.zaff = new ArrayMap(map8.size());
                    map9 = this.zafl.zaeu;
                    for (zaw zawVar : map9.values()) {
                        map10 = this.zafl.zaff;
                        map10.put(zawVar.getApiKey(), ConnectionResult.RESULT_SUCCESS);
                    }
                } else if (task.getException() instanceof AvailabilityException) {
                    AvailabilityException availabilityException = (AvailabilityException) task.getException();
                    z2 = this.zafl.zafc;
                    if (z2) {
                        zav zavVar2 = this.zafl;
                        map = this.zafl.zaeu;
                        zavVar2.zaff = new ArrayMap(map.size());
                        map2 = this.zafl.zaeu;
                        for (zaw zawVar2 : map2.values()) {
                            Object apiKey = zawVar2.getApiKey();
                            ConnectionResult connectionResult3 = availabilityException.getConnectionResult((GoogleApi<? extends Api.ApiOptions>) zawVar2);
                            zaa = this.zafl.zaa((zaw<?>) zawVar2, connectionResult3);
                            if (zaa) {
                                map3 = this.zafl.zaff;
                                map3.put(apiKey, new ConnectionResult(16));
                            } else {
                                map4 = this.zafl.zaff;
                                map4.put(apiKey, connectionResult3);
                            }
                        }
                    } else {
                        this.zafl.zaff = availabilityException.zaj();
                    }
                    zav zavVar3 = this.zafl;
                    zaac = zavVar3.zaac();
                    zavVar3.zafi = zaac;
                } else {
                    Log.e("ConnectionlessGAC", "Unexpected availability exception", task.getException());
                    this.zafl.zaff = Collections.emptyMap();
                    this.zafl.zafi = new ConnectionResult(8);
                }
                map5 = this.zafl.zafg;
                if (map5 != null) {
                    map6 = this.zafl.zaff;
                    map7 = this.zafl.zafg;
                    map6.putAll(map7);
                    zav zavVar4 = this.zafl;
                    zaac2 = zavVar4.zaac();
                    zavVar4.zafi = zaac2;
                }
                connectionResult = this.zafl.zafi;
                if (connectionResult == null) {
                    this.zafl.zaaa();
                    this.zafl.zaab();
                } else {
                    zav.zaa(this.zafl, false);
                    zaawVar = this.zafl.zaex;
                    connectionResult2 = this.zafl.zafi;
                    zaawVar.zac(connectionResult2);
                }
                condition = this.zafl.zaez;
                condition.signalAll();
            }
        } finally {
            lock2 = this.zafl.zaer;
            lock2.unlock();
        }
    }
}
