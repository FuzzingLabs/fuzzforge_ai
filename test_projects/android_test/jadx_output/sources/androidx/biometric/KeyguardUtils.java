package androidx.biometric;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;

/* loaded from: classes.dex */
class KeyguardUtils {
    private KeyguardUtils() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static KeyguardManager getKeyguardManager(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            return Api23Impl.getKeyguardManager(context);
        }
        Object service = context.getSystemService("keyguard");
        if (service instanceof KeyguardManager) {
            return (KeyguardManager) service;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean isDeviceSecuredWithCredential(Context context) {
        KeyguardManager keyguardManager = getKeyguardManager(context);
        if (keyguardManager == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            return Api23Impl.isDeviceSecure(keyguardManager);
        }
        if (Build.VERSION.SDK_INT < 16) {
            return false;
        }
        return Api16Impl.isKeyguardSecure(keyguardManager);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Api23Impl {
        private Api23Impl() {
        }

        static KeyguardManager getKeyguardManager(Context context) {
            return (KeyguardManager) context.getSystemService(KeyguardManager.class);
        }

        static boolean isDeviceSecure(KeyguardManager keyguardManager) {
            return keyguardManager.isDeviceSecure();
        }
    }

    /* loaded from: classes.dex */
    private static class Api16Impl {
        private Api16Impl() {
        }

        static boolean isKeyguardSecure(KeyguardManager keyguardManager) {
            return keyguardManager.isKeyguardSecure();
        }
    }
}
