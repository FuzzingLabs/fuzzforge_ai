package com.google.android.gms.common.api;

import com.google.android.gms.common.api.PendingResult;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zaa implements PendingResult.StatusListener {
    private final /* synthetic */ Batch zabb;

    zaa(Batch batch) {
        this.zabb = batch;
    }

    @Override // com.google.android.gms.common.api.PendingResult.StatusListener
    public final void onComplete(Status status) {
        Object obj;
        int i;
        boolean z;
        boolean z2;
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
                    Status status2 = z2 ? new Status(13) : Status.RESULT_SUCCESS;
                    Batch batch = this.zabb;
                    pendingResultArr = batch.zabf;
                    batch.setResult(new BatchResult(status2, pendingResultArr));
                }
            }
        }
    }
}
