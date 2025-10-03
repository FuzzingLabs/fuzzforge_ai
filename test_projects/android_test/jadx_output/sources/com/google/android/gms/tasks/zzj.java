package com.google.android.gms.tasks;

/* loaded from: classes.dex */
final class zzj implements Runnable {
    private final /* synthetic */ Task zzg;
    private final /* synthetic */ zzi zzm;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zzj(zzi zziVar, Task task) {
        this.zzm = zziVar;
        this.zzg = task;
    }

    @Override // java.lang.Runnable
    public final void run() {
        Object obj;
        OnCompleteListener onCompleteListener;
        OnCompleteListener onCompleteListener2;
        obj = this.zzm.mLock;
        synchronized (obj) {
            onCompleteListener = this.zzm.zzl;
            if (onCompleteListener != null) {
                onCompleteListener2 = this.zzm.zzl;
                onCompleteListener2.onComplete(this.zzg);
            }
        }
    }
}
