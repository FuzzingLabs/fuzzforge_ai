package com.google.android.gms.common.api;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.internal.zacc;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public abstract class ResultTransform<R extends Result, S extends Result> {
    public abstract PendingResult<S> onSuccess(R r);

    public Status onFailure(Status status) {
        return status;
    }

    public final PendingResult<S> createFailedResult(Status status) {
        return new zacc(status);
    }
}
