package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.api.Status;

/* loaded from: classes.dex */
public interface StatusExceptionMapper {
    Exception getException(Status status);
}
