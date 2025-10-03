package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultTransform;
import java.lang.ref.WeakReference;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zacn implements Runnable {
    private final /* synthetic */ zack zaky;
    private final /* synthetic */ Result zakz;

    zacn(zack zackVar, Result result) {
        this.zaky = zackVar;
        this.zakz = result;
    }

    @Override // java.lang.Runnable
    public final void run() {
        WeakReference weakReference;
        zacm zacmVar;
        zacm zacmVar2;
        WeakReference weakReference2;
        ResultTransform resultTransform;
        zacm zacmVar3;
        zacm zacmVar4;
        WeakReference weakReference3;
        try {
            try {
                BasePendingResult.zado.set(true);
                resultTransform = this.zaky.zakr;
                PendingResult onSuccess = resultTransform.onSuccess(this.zakz);
                zacmVar3 = this.zaky.zakw;
                zacmVar4 = this.zaky.zakw;
                zacmVar3.sendMessage(zacmVar4.obtainMessage(0, onSuccess));
                BasePendingResult.zado.set(false);
                zack zackVar = this.zaky;
                zack.zab(this.zakz);
                weakReference3 = this.zaky.zadr;
                GoogleApiClient googleApiClient = (GoogleApiClient) weakReference3.get();
                if (googleApiClient != null) {
                    googleApiClient.zab(this.zaky);
                }
            } catch (RuntimeException e) {
                zacmVar = this.zaky.zakw;
                zacmVar2 = this.zaky.zakw;
                zacmVar.sendMessage(zacmVar2.obtainMessage(1, e));
                BasePendingResult.zado.set(false);
                zack zackVar2 = this.zaky;
                zack.zab(this.zakz);
                weakReference2 = this.zaky.zadr;
                GoogleApiClient googleApiClient2 = (GoogleApiClient) weakReference2.get();
                if (googleApiClient2 != null) {
                    googleApiClient2.zab(this.zaky);
                }
            }
        } catch (Throwable th) {
            BasePendingResult.zado.set(false);
            zack zackVar3 = this.zaky;
            zack.zab(this.zakz);
            weakReference = this.zaky.zadr;
            GoogleApiClient googleApiClient3 = (GoogleApiClient) weakReference.get();
            if (googleApiClient3 != null) {
                googleApiClient3.zab(this.zaky);
            }
            throw th;
        }
    }
}
