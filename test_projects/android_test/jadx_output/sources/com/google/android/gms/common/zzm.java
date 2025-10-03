package com.google.android.gms.common;

import android.util.Log;
import com.google.android.gms.common.util.AndroidUtilsLight;
import com.google.android.gms.common.util.Hex;
import java.util.concurrent.Callable;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
@CheckReturnValue
/* loaded from: classes.dex */
public class zzm {
    private static final zzm zzac = new zzm(true, null, null);
    private final Throwable cause;
    final boolean zzad;
    private final String zzae;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zzm(boolean z, @Nullable String str, @Nullable Throwable th) {
        this.zzad = z;
        this.zzae = str;
        this.cause = th;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static zzm zze() {
        return zzac;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static zzm zza(Callable<String> callable) {
        return new zzo(callable);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static zzm zzb(String str) {
        return new zzm(false, str, null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static zzm zza(String str, Throwable th) {
        return new zzm(false, str, th);
    }

    @Nullable
    String getErrorMessage() {
        return this.zzae;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void zzf() {
        if (!this.zzad && Log.isLoggable("GoogleCertificatesRslt", 3)) {
            if (this.cause != null) {
                Log.d("GoogleCertificatesRslt", getErrorMessage(), this.cause);
            } else {
                Log.d("GoogleCertificatesRslt", getErrorMessage());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String zzc(String str, zze zzeVar, boolean z, boolean z2) {
        return String.format("%s: pkg=%s, sha1=%s, atk=%s, ver=%s", z2 ? "debug cert rejected" : "not whitelisted", str, Hex.bytesToStringLowercase(AndroidUtilsLight.zzj("SHA-1").digest(zzeVar.getBytes())), Boolean.valueOf(z), "12451009.false");
    }
}
