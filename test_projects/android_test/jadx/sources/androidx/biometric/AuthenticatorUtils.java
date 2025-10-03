package androidx.biometric;

import android.os.Build;
import androidx.biometric.BiometricPrompt;

/* loaded from: classes.dex */
class AuthenticatorUtils {
    private static final int BIOMETRIC_CLASS_MASK = 32767;

    private AuthenticatorUtils() {
    }

    static String convertToString(int authenticators) {
        switch (authenticators) {
            case 15:
                return "BIOMETRIC_STRONG";
            case 255:
                return "BIOMETRIC_WEAK";
            case 32768:
                return "DEVICE_CREDENTIAL";
            case 32783:
                return "BIOMETRIC_STRONG | DEVICE_CREDENTIAL";
            case 33023:
                return "BIOMETRIC_WEAK | DEVICE_CREDENTIAL";
            default:
                return String.valueOf(authenticators);
        }
    }

    static int getConsolidatedAuthenticators(BiometricPrompt.PromptInfo info, BiometricPrompt.CryptoObject crypto) {
        int authenticators;
        if (info.getAllowedAuthenticators() != 0) {
            int authenticators2 = info.getAllowedAuthenticators();
            return authenticators2;
        }
        if (crypto != null) {
            authenticators = 15;
        } else {
            authenticators = 255;
        }
        if (info.isDeviceCredentialAllowed()) {
            return authenticators | 32768;
        }
        return authenticators;
    }

    static boolean isSupportedCombination(int authenticators) {
        switch (authenticators) {
            case 15:
            case 255:
            case 33023:
                return true;
            case 32768:
                return Build.VERSION.SDK_INT >= 30;
            case 32783:
                return Build.VERSION.SDK_INT < 28 || Build.VERSION.SDK_INT > 29;
            default:
                return authenticators == 0;
        }
    }

    static boolean isDeviceCredentialAllowed(int authenticators) {
        return (32768 & authenticators) != 0;
    }

    static boolean isSomeBiometricAllowed(int authenticators) {
        return (authenticators & BIOMETRIC_CLASS_MASK) != 0;
    }

    static boolean isWeakBiometricAllowed(int authenticators) {
        return (authenticators & 255) == 255;
    }
}
