package com.google.android.gms.common.config;

import com.google.android.gms.common.config.GservicesValue;

/* loaded from: classes.dex */
final class zze extends GservicesValue<String> {
    zze(String str, String str2) {
        super(str, str2);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.google.android.gms.common.config.GservicesValue
    protected final /* synthetic */ String zzd(String str) {
        GservicesValue.zza zzaVar = null;
        return zzaVar.getString(this.mKey, (String) this.zzbq);
    }
}
