package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.ApiOptions;
import com.google.android.gms.common.internal.Objects;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public final class ApiKey<O extends Api.ApiOptions> {
    private final Api<O> mApi;
    private final O zabj;
    private final boolean zacx;
    private final int zacy;

    private ApiKey(Api<O> api, O o) {
        this.zacx = false;
        this.mApi = api;
        this.zabj = o;
        this.zacy = Objects.hashCode(api, o);
    }

    private ApiKey(Api<O> api) {
        this.zacx = true;
        this.mApi = api;
        this.zabj = null;
        this.zacy = System.identityHashCode(this);
    }

    public static <O extends Api.ApiOptions> ApiKey<O> getSharedApiKey(Api<O> api, O o) {
        return new ApiKey<>(api, o);
    }

    public static <O extends Api.ApiOptions> ApiKey<O> getUniqueApiKey(Api<O> api) {
        return new ApiKey<>(api);
    }

    public final Api.AnyClientKey<?> getClientKey() {
        return this.mApi.getClientKey();
    }

    public final boolean isUnique() {
        return this.zacx;
    }

    public final String getApiName() {
        return this.mApi.getName();
    }

    public final int hashCode() {
        return this.zacy;
    }

    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ApiKey)) {
            return false;
        }
        ApiKey apiKey = (ApiKey) obj;
        return !this.zacx && !apiKey.zacx && Objects.equal(this.mApi, apiKey.mApi) && Objects.equal(this.zabj, apiKey.zabj);
    }
}
