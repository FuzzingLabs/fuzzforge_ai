package com.google.android.gms.tasks;

import java.util.concurrent.CancellationException;

/* loaded from: classes.dex */
final class zzp implements Runnable {
    private final /* synthetic */ Task zzg;
    private final /* synthetic */ zzo zzs;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zzp(zzo zzoVar, Task task) {
        this.zzs = zzoVar;
        this.zzg = task;
    }

    @Override // java.lang.Runnable
    public final void run() {
        SuccessContinuation successContinuation;
        try {
            successContinuation = this.zzs.zzr;
            Task then = successContinuation.then(this.zzg.getResult());
            if (then == null) {
                this.zzs.onFailure(new NullPointerException("Continuation returned null"));
                return;
            }
            then.addOnSuccessListener(TaskExecutors.zzw, this.zzs);
            then.addOnFailureListener(TaskExecutors.zzw, this.zzs);
            then.addOnCanceledListener(TaskExecutors.zzw, this.zzs);
        } catch (RuntimeExecutionException e) {
            if (e.getCause() instanceof Exception) {
                this.zzs.onFailure((Exception) e.getCause());
            } else {
                this.zzs.onFailure(e);
            }
        } catch (CancellationException e2) {
            this.zzs.onCanceled();
        } catch (Exception e3) {
            this.zzs.onFailure(e3);
        }
    }
}
