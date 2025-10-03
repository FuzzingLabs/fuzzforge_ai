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

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public final class zaaa implements OnCompleteListener<Map<ApiKey<?>, String>> {
    private final /* synthetic */ zav zafl;
    private SignInConnectionListener zafo;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zaaa(zav zavVar, SignInConnectionListener signInConnectionListener) {
        this.zafl = zavVar;
        this.zafo = signInConnectionListener;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void cancel() {
        this.zafo.onComplete();
    }

    @Override // com.google.android.gms.tasks.OnCompleteListener
    public final void onComplete(Task<Map<ApiKey<?>, String>> task) {
        Lock lock;
        Lock lock2;
        boolean z;
        boolean z2;
        Map map;
        Map map2;
        boolean zaa;
        Map map3;
        Map map4;
        Map map5;
        Map map6;
        ConnectionResult zaac;
        Condition condition;
        Map map7;
        Map map8;
        Map map9;
        lock = this.zafl.zaer;
        lock.lock();
        try {
            z = this.zafl.zafe;
            if (!z) {
                this.zafo.onComplete();
                return;
            }
            if (task.isSuccessful()) {
                zav zavVar = this.zafl;
                map7 = this.zafl.zaev;
                zavVar.zafg = new ArrayMap(map7.size());
                map8 = this.zafl.zaev;
                for (zaw zawVar : map8.values()) {
                    map9 = this.zafl.zafg;
                    map9.put(zawVar.getApiKey(), ConnectionResult.RESULT_SUCCESS);
                }
            } else if (task.getException() instanceof AvailabilityException) {
                AvailabilityException availabilityException = (AvailabilityException) task.getException();
                z2 = this.zafl.zafc;
                if (z2) {
                    zav zavVar2 = this.zafl;
                    map = this.zafl.zaev;
                    zavVar2.zafg = new ArrayMap(map.size());
                    map2 = this.zafl.zaev;
                    for (zaw zawVar2 : map2.values()) {
                        Object apiKey = zawVar2.getApiKey();
                        ConnectionResult connectionResult = availabilityException.getConnectionResult((GoogleApi<? extends Api.ApiOptions>) zawVar2);
                        zaa = this.zafl.zaa((zaw<?>) zawVar2, connectionResult);
                        if (zaa) {
                            map3 = this.zafl.zafg;
                            map3.put(apiKey, new ConnectionResult(16));
                        } else {
                            map4 = this.zafl.zafg;
                            map4.put(apiKey, connectionResult);
                        }
                    }
                } else {
                    this.zafl.zafg = availabilityException.zaj();
                }
            } else {
                Log.e("ConnectionlessGAC", "Unexpected availability exception", task.getException());
                this.zafl.zafg = Collections.emptyMap();
            }
            if (this.zafl.isConnected()) {
                map5 = this.zafl.zaff;
                map6 = this.zafl.zafg;
                map5.putAll(map6);
                zaac = this.zafl.zaac();
                if (zaac == null) {
                    this.zafl.zaaa();
                    this.zafl.zaab();
                    condition = this.zafl.zaez;
                    condition.signalAll();
                }
            }
            this.zafo.onComplete();
        } finally {
            lock2 = this.zafl.zaer;
            lock2.unlock();
        }
    }
}
