package com.google.android.gms.common.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.google.android.gms.common.wrappers.Wrappers;
import com.google.android.gms.internal.common.zzg;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* loaded from: classes.dex */
public class AndroidUtilsLight {
    private static volatile int zzgf = -1;

    public static byte[] getPackageCertificateHashBytes(Context context, String str) throws PackageManager.NameNotFoundException {
        MessageDigest zzj;
        PackageInfo packageInfo = Wrappers.packageManager(context).getPackageInfo(str, 64);
        if (packageInfo.signatures != null && packageInfo.signatures.length == 1 && (zzj = zzj("SHA1")) != null) {
            return zzj.digest(packageInfo.signatures[0].toByteArray());
        }
        return null;
    }

    public static MessageDigest zzj(String str) {
        MessageDigest messageDigest;
        for (int i = 0; i < 2; i++) {
            try {
                messageDigest = MessageDigest.getInstance(str);
            } catch (NoSuchAlgorithmException e) {
            }
            if (messageDigest != null) {
                return messageDigest;
            }
        }
        return null;
    }

    @Deprecated
    public static Context getDeviceProtectedStorageContext(Context context) {
        if (zzg.zzam()) {
            return zzg.getDeviceProtectedStorageContext(context);
        }
        return context;
    }
}
