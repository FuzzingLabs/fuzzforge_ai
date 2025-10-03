package androidx.biometric;

import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import androidx.biometric.BiometricPrompt;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class AuthenticationCallbackProvider {
    private BiometricPrompt.AuthenticationCallback mBiometricCallback;
    private FingerprintManagerCompat.AuthenticationCallback mFingerprintCallback;
    final Listener mListener;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class Listener {
        void onSuccess(BiometricPrompt.AuthenticationResult result) {
        }

        void onError(int errorCode, CharSequence errorMessage) {
        }

        void onHelp(CharSequence helpMessage) {
        }

        void onFailure() {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AuthenticationCallbackProvider(Listener listener) {
        this.mListener = listener;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public BiometricPrompt.AuthenticationCallback getBiometricCallback() {
        if (this.mBiometricCallback == null) {
            this.mBiometricCallback = Api28Impl.createCallback(this.mListener);
        }
        return this.mBiometricCallback;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public FingerprintManagerCompat.AuthenticationCallback getFingerprintCallback() {
        if (this.mFingerprintCallback == null) {
            this.mFingerprintCallback = new FingerprintManagerCompat.AuthenticationCallback() { // from class: androidx.biometric.AuthenticationCallbackProvider.1
                @Override // androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback
                public void onAuthenticationError(int errMsgId, CharSequence errString) {
                    AuthenticationCallbackProvider.this.mListener.onError(errMsgId, errString);
                }

                @Override // androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                    AuthenticationCallbackProvider.this.mListener.onHelp(helpString);
                }

                @Override // androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback
                public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                    BiometricPrompt.CryptoObject crypto;
                    if (result != null) {
                        crypto = CryptoObjectUtils.unwrapFromFingerprintManager(result.getCryptoObject());
                    } else {
                        crypto = null;
                    }
                    BiometricPrompt.AuthenticationResult resultCompat = new BiometricPrompt.AuthenticationResult(crypto, 2);
                    AuthenticationCallbackProvider.this.mListener.onSuccess(resultCompat);
                }

                @Override // androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback
                public void onAuthenticationFailed() {
                    AuthenticationCallbackProvider.this.mListener.onFailure();
                }
            };
        }
        return this.mFingerprintCallback;
    }

    /* loaded from: classes.dex */
    private static class Api30Impl {
        private Api30Impl() {
        }

        static int getAuthenticationType(BiometricPrompt.AuthenticationResult result) {
            return result.getAuthenticationType();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Api28Impl {
        private Api28Impl() {
        }

        static BiometricPrompt.AuthenticationCallback createCallback(final Listener listener) {
            return new BiometricPrompt.AuthenticationCallback() { // from class: androidx.biometric.AuthenticationCallbackProvider.Api28Impl.1
                @Override // android.hardware.biometrics.BiometricPrompt.AuthenticationCallback
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    Listener.this.onError(errorCode, errString);
                }

                @Override // android.hardware.biometrics.BiometricPrompt.AuthenticationCallback
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                }

                @Override // android.hardware.biometrics.BiometricPrompt.AuthenticationCallback
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    BiometricPrompt.CryptoObject crypto;
                    int authenticationType;
                    if (result != null) {
                        crypto = CryptoObjectUtils.unwrapFromBiometricPrompt(result.getCryptoObject());
                    } else {
                        crypto = null;
                    }
                    if (Build.VERSION.SDK_INT >= 30) {
                        if (result != null) {
                            authenticationType = Api30Impl.getAuthenticationType(result);
                        } else {
                            authenticationType = -1;
                        }
                    } else {
                        int authenticationType2 = Build.VERSION.SDK_INT;
                        if (authenticationType2 == 29) {
                            authenticationType = -1;
                        } else {
                            authenticationType = 2;
                        }
                    }
                    BiometricPrompt.AuthenticationResult resultCompat = new BiometricPrompt.AuthenticationResult(crypto, authenticationType);
                    Listener.this.onSuccess(resultCompat);
                }

                @Override // android.hardware.biometrics.BiometricPrompt.AuthenticationCallback
                public void onAuthenticationFailed() {
                    Listener.this.onFailure();
                }
            };
        }
    }
}
