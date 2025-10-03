package com.google.android.gms.tasks;

/* loaded from: classes.dex */
final class zzl implements Runnable {
    private final /* synthetic */ Task zzg;
    private final /* synthetic */ zzk zzo;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zzl(zzk zzkVar, Task task) {
        this.zzo = zzkVar;
        this.zzg = task;
    }

    @Override // java.lang.Runnable
    public final void run() {
        Object obj;
        OnFailureListener onFailureListener;
        OnFailureListener onFailureListener2;
        obj = this.zzo.mLock;
        synchronized (obj) {
            onFailureListener = this.zzo.zzn;
            if (onFailureListener != null) {
                onFailureListener2 = this.zzo.zzn;
                onFailureListener2.onFailure(this.zzg.getException());
            }
        }
    }
}
