package com.google.android.gms.tasks;

/* loaded from: classes.dex */
final class zzn implements Runnable {
    private final /* synthetic */ Task zzg;
    private final /* synthetic */ zzm zzq;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zzn(zzm zzmVar, Task task) {
        this.zzq = zzmVar;
        this.zzg = task;
    }

    @Override // java.lang.Runnable
    public final void run() {
        Object obj;
        OnSuccessListener onSuccessListener;
        OnSuccessListener onSuccessListener2;
        obj = this.zzq.mLock;
        synchronized (obj) {
            onSuccessListener = this.zzq.zzp;
            if (onSuccessListener != null) {
                onSuccessListener2 = this.zzq.zzp;
                onSuccessListener2.onSuccess(this.zzg.getResult());
            }
        }
    }
}
