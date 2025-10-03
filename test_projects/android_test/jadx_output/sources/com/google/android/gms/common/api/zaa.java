package com.google.android.gms.common.api;

import com.google.android.gms.common.api.PendingResult;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public final class zaa implements PendingResult.StatusListener {
    private final /* synthetic */ Batch zabb;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zaa(Batch batch) {
        this.zabb = batch;
    }

    @Override // com.google.android.gms.common.api.PendingResult.StatusListener
    public final void onComplete(Status status) {
        Object obj;
        int i;
        boolean z;
        boolean z2;
        Status status2;
        PendingResult[] pendingResultArr;
        obj = this.zabb.mLock;
        synchronized (obj) {
            if (this.zabb.isCanceled()) {
                return;
            }
            if (status.isCanceled()) {
                Batch.zaa(this.zabb, true);
            } else if (!status.isSuccess()) {
                Batch.zab(this.zabb, true);
            }
            Batch.zab(this.zabb);
            i = this.zabb.zabc;
            if (i == 0) {
                z = this.zabb.zabe;
                if (z) {
                    super/*com.google.android.gms.common.api.internal.BasePendingResult*/.cancel();
                } else {
                    z2 = this.zabb.zabd;
                    if (z2) {
                        status2 = new Status(13);
                    } else {
                        status2 = Status.RESULT_SUCCESS;
                    }
                    Batch batch = this.zabb;
                    pendingResultArr = batch.zabf;
                    batch.setResult(new BatchResult(status2, pendingResultArr));
                }
            }
        }
    }
}
