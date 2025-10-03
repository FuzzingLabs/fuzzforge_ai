package com.google.android.gms.signin;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.signin.internal.SignInClientImpl;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public final class zab {
    public static final Api<SignInOptions> API;
    private static final Api.ClientKey<SignInClientImpl> CLIENT_KEY;
    public static final Api.AbstractClientBuilder<SignInClientImpl, SignInOptions> zapv;
    private static final Scope zar;
    private static final Scope zas;
    private static final Api.ClientKey<SignInClientImpl> zasj;
    private static final Api.AbstractClientBuilder<SignInClientImpl, Object> zask;
    private static final Api<Object> zasl;

    static {
        Api.ClientKey<SignInClientImpl> clientKey = new Api.ClientKey<>();
        CLIENT_KEY = clientKey;
        Api.ClientKey<SignInClientImpl> clientKey2 = new Api.ClientKey<>();
        zasj = clientKey2;
        zaa zaaVar = new zaa();
        zapv = zaaVar;
        zad zadVar = new zad();
        zask = zadVar;
        zar = new Scope(Scopes.PROFILE);
        zas = new Scope("email");
        API = new Api<>("SignIn.API", zaaVar, clientKey);
        zasl = new Api<>("SignIn.INTERNAL_API", zadVar, clientKey2);
    }
}
