package com.google.firebase.appcheck;

import com.google.firebase.FirebaseException;

/* loaded from: classes.dex */
public abstract class AppCheckTokenResult {
    public abstract FirebaseException getError();

    public abstract String getToken();
}
