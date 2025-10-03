package com.google.android.gms.tasks;

/* loaded from: classes.dex */
final class zzh implements Runnable {
    private final /* synthetic */ zzg zzk;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zzh(zzg zzgVar) {
        this.zzk = zzgVar;
    }

    @Override // java.lang.Runnable
    public final void run() {
        Object obj;
        OnCanceledListener onCanceledListener;
        OnCanceledListener onCanceledListener2;
        obj = this.zzk.mLock;
        synchronized (obj) {
            onCanceledListener = this.zzk.zzj;
            if (onCanceledListener != null) {
                onCanceledListener2 = this.zzk.zzj;
                onCanceledListener2.onCanceled();
            }
        }
    }
}
