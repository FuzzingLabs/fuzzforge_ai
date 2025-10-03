package com.google.android.gms.common.api.internal;

import android.os.RemoteException;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public interface RemoteCall<T, U> {
    void accept(T t, U u) throws RemoteException;
}
