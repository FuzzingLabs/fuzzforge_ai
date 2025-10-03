package androidx.biometric;

import android.content.Context;
import android.os.Build;

/* loaded from: classes.dex */
class DeviceUtils {
    private DeviceUtils() {
    }

    static boolean shouldUseFingerprintForCrypto(Context context, String vendor, String model) {
        if (Build.VERSION.SDK_INT != 28) {
            return false;
        }
        return isVendorInList(context, vendor, C0161R.array.crypto_fingerprint_fallback_vendors) || isModelInPrefixList(context, model, C0161R.array.crypto_fingerprint_fallback_prefixes);
    }

    static boolean shouldHideFingerprintDialog(Context context, String model) {
        if (Build.VERSION.SDK_INT != 28) {
            return false;
        }
        return isModelInPrefixList(context, model, C0161R.array.hide_fingerprint_instantly_prefixes);
    }

    static boolean shouldDelayShowingPrompt(Context context, String model) {
        if (Build.VERSION.SDK_INT != 29) {
            return false;
        }
        return isModelInList(context, model, C0161R.array.delay_showing_prompt_models);
    }

    static boolean canAssumeStrongBiometrics(Context context, String model) {
        if (Build.VERSION.SDK_INT >= 30) {
            return false;
        }
        return isModelInList(context, model, C0161R.array.assume_strong_biometrics_models);
    }

    private static boolean isVendorInList(Context context, String vendor, int resId) {
        if (vendor == null) {
            return false;
        }
        String[] vendorNames = context.getResources().getStringArray(resId);
        for (String vendorName : vendorNames) {
            if (vendor.equalsIgnoreCase(vendorName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isModelInPrefixList(Context context, String model, int resId) {
        if (model == null) {
            return false;
        }
        String[] modelPrefixes = context.getResources().getStringArray(resId);
        for (String modelPrefix : modelPrefixes) {
            if (model.startsWith(modelPrefix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isModelInList(Context context, String model, int resId) {
        if (model == null) {
            return false;
        }
        String[] modelNames = context.getResources().getStringArray(resId);
        for (String modelName : modelNames) {
            if (model.equals(modelName)) {
                return true;
            }
        }
        return false;
    }
}
