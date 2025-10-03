package androidx.biometric;

import android.content.Context;
import android.util.Log;

/* loaded from: classes.dex */
class ErrorUtils {
    private ErrorUtils() {
    }

    static boolean isKnownError(int errorCode) {
        switch (errorCode) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                return true;
            case 6:
            default:
                return false;
        }
    }

    static boolean isLockoutError(int errorCode) {
        return errorCode == 7 || errorCode == 9;
    }

    static String getFingerprintErrorString(Context context, int errorCode) {
        if (context == null) {
            return "";
        }
        switch (errorCode) {
            case 1:
                return context.getString(C0161R.string.fingerprint_error_hw_not_available);
            case 7:
            case 9:
                return context.getString(C0161R.string.fingerprint_error_lockout);
            case 10:
                return context.getString(C0161R.string.fingerprint_error_user_canceled);
            case 11:
                return context.getString(C0161R.string.fingerprint_error_no_fingerprints);
            case 12:
                return context.getString(C0161R.string.fingerprint_error_hw_not_present);
            default:
                Log.e("BiometricUtils", "Unknown error code: " + errorCode);
                return context.getString(C0161R.string.default_error_msg);
        }
    }
}
