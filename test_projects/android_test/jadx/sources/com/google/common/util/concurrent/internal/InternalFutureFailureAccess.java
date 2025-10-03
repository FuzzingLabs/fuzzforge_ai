package com.google.common.util.concurrent.internal;

/* loaded from: classes.dex */
public abstract class InternalFutureFailureAccess {
    protected abstract Throwable tryInternalFastPathGetFailure();

    protected InternalFutureFailureAccess() {
    }
}
