package com.google.android.gms.tasks;

/* loaded from: classes.dex */
public class TaskCompletionSource<TResult> {
    private final zzu<TResult> zza = new zzu<>();

    public TaskCompletionSource() {
    }

    public TaskCompletionSource(CancellationToken cancellationToken) {
        cancellationToken.onCanceledRequested(new zzs(this));
    }

    public void setResult(TResult tresult) {
        this.zza.setResult(tresult);
    }

    public boolean trySetResult(TResult tresult) {
        return this.zza.trySetResult(tresult);
    }

    public void setException(Exception exc) {
        this.zza.setException(exc);
    }

    public boolean trySetException(Exception exc) {
        return this.zza.trySetException(exc);
    }

    public Task<TResult> getTask() {
        return this.zza;
    }
}
