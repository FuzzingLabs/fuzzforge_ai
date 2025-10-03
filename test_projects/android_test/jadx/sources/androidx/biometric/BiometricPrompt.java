package androidx.biometric;

import android.os.Build;
import android.security.identity.IdentityCredential;
import android.text.TextUtils;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModelProvider;
import java.lang.ref.WeakReference;
import java.security.Signature;
import java.util.concurrent.Executor;
import javax.crypto.Cipher;
import javax.crypto.Mac;

/* loaded from: classes.dex */
public class BiometricPrompt {
    public static final int AUTHENTICATION_RESULT_TYPE_BIOMETRIC = 2;
    public static final int AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL = 1;
    public static final int AUTHENTICATION_RESULT_TYPE_UNKNOWN = -1;
    private static final String BIOMETRIC_FRAGMENT_TAG = "androidx.biometric.BiometricFragment";
    static final int BIOMETRIC_SUCCESS = 0;
    public static final int ERROR_CANCELED = 5;
    public static final int ERROR_HW_NOT_PRESENT = 12;
    public static final int ERROR_HW_UNAVAILABLE = 1;
    public static final int ERROR_LOCKOUT = 7;
    public static final int ERROR_LOCKOUT_PERMANENT = 9;
    public static final int ERROR_NEGATIVE_BUTTON = 13;
    public static final int ERROR_NO_BIOMETRICS = 11;
    public static final int ERROR_NO_DEVICE_CREDENTIAL = 14;
    public static final int ERROR_NO_SPACE = 4;
    public static final int ERROR_SECURITY_UPDATE_REQUIRED = 15;
    public static final int ERROR_TIMEOUT = 3;
    public static final int ERROR_UNABLE_TO_PROCESS = 2;
    public static final int ERROR_USER_CANCELED = 10;
    public static final int ERROR_VENDOR = 8;
    private static final String TAG = "BiometricPromptCompat";
    private FragmentManager mClientFragmentManager;

    public static class CryptoObject {
        private final Cipher mCipher;
        private final IdentityCredential mIdentityCredential;
        private final Mac mMac;
        private final Signature mSignature;

        public CryptoObject(Signature signature) {
            this.mSignature = signature;
            this.mCipher = null;
            this.mMac = null;
            this.mIdentityCredential = null;
        }

        public CryptoObject(Cipher cipher) {
            this.mSignature = null;
            this.mCipher = cipher;
            this.mMac = null;
            this.mIdentityCredential = null;
        }

        public CryptoObject(Mac mac) {
            this.mSignature = null;
            this.mCipher = null;
            this.mMac = mac;
            this.mIdentityCredential = null;
        }

        public CryptoObject(IdentityCredential identityCredential) {
            this.mSignature = null;
            this.mCipher = null;
            this.mMac = null;
            this.mIdentityCredential = identityCredential;
        }

        public Signature getSignature() {
            return this.mSignature;
        }

        public Cipher getCipher() {
            return this.mCipher;
        }

        public Mac getMac() {
            return this.mMac;
        }

        public IdentityCredential getIdentityCredential() {
            return this.mIdentityCredential;
        }
    }

    public static class AuthenticationResult {
        private final int mAuthenticationType;
        private final CryptoObject mCryptoObject;

        AuthenticationResult(CryptoObject crypto, int authenticationType) {
            this.mCryptoObject = crypto;
            this.mAuthenticationType = authenticationType;
        }

        public CryptoObject getCryptoObject() {
            return this.mCryptoObject;
        }

        public int getAuthenticationType() {
            return this.mAuthenticationType;
        }
    }

    public static abstract class AuthenticationCallback {
        public void onAuthenticationError(int errorCode, CharSequence errString) {
        }

        public void onAuthenticationSucceeded(AuthenticationResult result) {
        }

        public void onAuthenticationFailed() {
        }
    }

    public static class PromptInfo {
        private final int mAllowedAuthenticators;
        private final CharSequence mDescription;
        private final boolean mIsConfirmationRequired;
        private final boolean mIsDeviceCredentialAllowed;
        private final CharSequence mNegativeButtonText;
        private final CharSequence mSubtitle;
        private final CharSequence mTitle;

        public static class Builder {
            private CharSequence mTitle = null;
            private CharSequence mSubtitle = null;
            private CharSequence mDescription = null;
            private CharSequence mNegativeButtonText = null;
            private boolean mIsConfirmationRequired = true;
            private boolean mIsDeviceCredentialAllowed = false;
            private int mAllowedAuthenticators = 0;

            public Builder setTitle(CharSequence title) {
                this.mTitle = title;
                return this;
            }

            public Builder setSubtitle(CharSequence subtitle) {
                this.mSubtitle = subtitle;
                return this;
            }

            public Builder setDescription(CharSequence description) {
                this.mDescription = description;
                return this;
            }

            public Builder setNegativeButtonText(CharSequence negativeButtonText) {
                this.mNegativeButtonText = negativeButtonText;
                return this;
            }

            public Builder setConfirmationRequired(boolean confirmationRequired) {
                this.mIsConfirmationRequired = confirmationRequired;
                return this;
            }

            @Deprecated
            public Builder setDeviceCredentialAllowed(boolean deviceCredentialAllowed) {
                this.mIsDeviceCredentialAllowed = deviceCredentialAllowed;
                return this;
            }

            public Builder setAllowedAuthenticators(int allowedAuthenticators) {
                this.mAllowedAuthenticators = allowedAuthenticators;
                return this;
            }

            public PromptInfo build() {
                boolean isDeviceCredentialAllowed;
                if (TextUtils.isEmpty(this.mTitle)) {
                    throw new IllegalArgumentException("Title must be set and non-empty.");
                }
                if (!AuthenticatorUtils.isSupportedCombination(this.mAllowedAuthenticators)) {
                    throw new IllegalArgumentException("Authenticator combination is unsupported on API " + Build.VERSION.SDK_INT + ": " + AuthenticatorUtils.convertToString(this.mAllowedAuthenticators));
                }
                int i = this.mAllowedAuthenticators;
                if (i != 0) {
                    isDeviceCredentialAllowed = AuthenticatorUtils.isDeviceCredentialAllowed(i);
                } else {
                    isDeviceCredentialAllowed = this.mIsDeviceCredentialAllowed;
                }
                if (TextUtils.isEmpty(this.mNegativeButtonText) && !isDeviceCredentialAllowed) {
                    throw new IllegalArgumentException("Negative text must be set and non-empty.");
                }
                if (!TextUtils.isEmpty(this.mNegativeButtonText) && isDeviceCredentialAllowed) {
                    throw new IllegalArgumentException("Negative text must not be set if device credential authentication is allowed.");
                }
                return new PromptInfo(this.mTitle, this.mSubtitle, this.mDescription, this.mNegativeButtonText, this.mIsConfirmationRequired, this.mIsDeviceCredentialAllowed, this.mAllowedAuthenticators);
            }
        }

        PromptInfo(CharSequence title, CharSequence subtitle, CharSequence description, CharSequence negativeButtonText, boolean confirmationRequired, boolean deviceCredentialAllowed, int allowedAuthenticators) {
            this.mTitle = title;
            this.mSubtitle = subtitle;
            this.mDescription = description;
            this.mNegativeButtonText = negativeButtonText;
            this.mIsConfirmationRequired = confirmationRequired;
            this.mIsDeviceCredentialAllowed = deviceCredentialAllowed;
            this.mAllowedAuthenticators = allowedAuthenticators;
        }

        public CharSequence getTitle() {
            return this.mTitle;
        }

        public CharSequence getSubtitle() {
            return this.mSubtitle;
        }

        public CharSequence getDescription() {
            return this.mDescription;
        }

        public CharSequence getNegativeButtonText() {
            CharSequence charSequence = this.mNegativeButtonText;
            return charSequence != null ? charSequence : "";
        }

        public boolean isConfirmationRequired() {
            return this.mIsConfirmationRequired;
        }

        @Deprecated
        public boolean isDeviceCredentialAllowed() {
            return this.mIsDeviceCredentialAllowed;
        }

        public int getAllowedAuthenticators() {
            return this.mAllowedAuthenticators;
        }
    }

    private static class ResetCallbackObserver implements LifecycleObserver {
        private final WeakReference<BiometricViewModel> mViewModelRef;

        ResetCallbackObserver(BiometricViewModel viewModel) {
            this.mViewModelRef = new WeakReference<>(viewModel);
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void resetCallback() {
            if (this.mViewModelRef.get() != null) {
                this.mViewModelRef.get().resetClientCallback();
            }
        }
    }

    public BiometricPrompt(FragmentActivity activity, AuthenticationCallback callback) {
        if (activity == null) {
            throw new IllegalArgumentException("FragmentActivity must not be null.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("AuthenticationCallback must not be null.");
        }
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        BiometricViewModel viewModel = getViewModel(activity);
        init(fragmentManager, viewModel, null, callback);
    }

    public BiometricPrompt(Fragment fragment, AuthenticationCallback callback) {
        if (fragment == null) {
            throw new IllegalArgumentException("Fragment must not be null.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("AuthenticationCallback must not be null.");
        }
        FragmentActivity activity = fragment.getActivity();
        FragmentManager fragmentManager = fragment.getChildFragmentManager();
        BiometricViewModel viewModel = getViewModel(activity);
        addObservers(fragment, viewModel);
        init(fragmentManager, viewModel, null, callback);
    }

    public BiometricPrompt(FragmentActivity activity, Executor executor, AuthenticationCallback callback) {
        if (activity == null) {
            throw new IllegalArgumentException("FragmentActivity must not be null.");
        }
        if (executor == null) {
            throw new IllegalArgumentException("Executor must not be null.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("AuthenticationCallback must not be null.");
        }
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        BiometricViewModel viewModel = getViewModel(activity);
        init(fragmentManager, viewModel, executor, callback);
    }

    public BiometricPrompt(Fragment fragment, Executor executor, AuthenticationCallback callback) {
        if (fragment == null) {
            throw new IllegalArgumentException("Fragment must not be null.");
        }
        if (executor == null) {
            throw new IllegalArgumentException("Executor must not be null.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("AuthenticationCallback must not be null.");
        }
        FragmentActivity activity = fragment.getActivity();
        FragmentManager fragmentManager = fragment.getChildFragmentManager();
        BiometricViewModel viewModel = getViewModel(activity);
        addObservers(fragment, viewModel);
        init(fragmentManager, viewModel, executor, callback);
    }

    private void init(FragmentManager fragmentManager, BiometricViewModel viewModel, Executor executor, AuthenticationCallback callback) {
        this.mClientFragmentManager = fragmentManager;
        if (viewModel != null) {
            if (executor != null) {
                viewModel.setClientExecutor(executor);
            }
            viewModel.setClientCallback(callback);
        }
    }

    public void authenticate(PromptInfo info, CryptoObject crypto) {
        if (info == null) {
            throw new IllegalArgumentException("PromptInfo cannot be null.");
        }
        if (crypto == null) {
            throw new IllegalArgumentException("CryptoObject cannot be null.");
        }
        int authenticators = AuthenticatorUtils.getConsolidatedAuthenticators(info, crypto);
        if (AuthenticatorUtils.isWeakBiometricAllowed(authenticators)) {
            throw new IllegalArgumentException("Crypto-based authentication is not supported for Class 2 (Weak) biometrics.");
        }
        if (Build.VERSION.SDK_INT < 30 && AuthenticatorUtils.isDeviceCredentialAllowed(authenticators)) {
            throw new IllegalArgumentException("Crypto-based authentication is not supported for device credential prior to API 30.");
        }
        authenticateInternal(info, crypto);
    }

    public void authenticate(PromptInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("PromptInfo cannot be null.");
        }
        authenticateInternal(info, null);
    }

    private void authenticateInternal(PromptInfo info, CryptoObject crypto) {
        FragmentManager fragmentManager = this.mClientFragmentManager;
        if (fragmentManager == null) {
            Log.e(TAG, "Unable to start authentication. Client fragment manager was null.");
        } else if (fragmentManager.isStateSaved()) {
            Log.e(TAG, "Unable to start authentication. Called after onSaveInstanceState().");
        } else {
            BiometricFragment biometricFragment = findOrAddBiometricFragment(this.mClientFragmentManager);
            biometricFragment.authenticate(info, crypto);
        }
    }

    public void cancelAuthentication() {
        FragmentManager fragmentManager = this.mClientFragmentManager;
        if (fragmentManager == null) {
            Log.e(TAG, "Unable to start authentication. Client fragment manager was null.");
            return;
        }
        BiometricFragment biometricFragment = findBiometricFragment(fragmentManager);
        if (biometricFragment == null) {
            Log.e(TAG, "Unable to cancel authentication. BiometricFragment not found.");
        } else {
            biometricFragment.cancelAuthentication(3);
        }
    }

    private static BiometricViewModel getViewModel(FragmentActivity activity) {
        if (activity != null) {
            return (BiometricViewModel) new ViewModelProvider(activity).get(BiometricViewModel.class);
        }
        return null;
    }

    private static void addObservers(Fragment fragment, BiometricViewModel viewModel) {
        if (viewModel != null) {
            fragment.getLifecycle().addObserver(new ResetCallbackObserver(viewModel));
        }
    }

    private static BiometricFragment findBiometricFragment(FragmentManager fragmentManager) {
        return (BiometricFragment) fragmentManager.findFragmentByTag(BIOMETRIC_FRAGMENT_TAG);
    }

    private static BiometricFragment findOrAddBiometricFragment(FragmentManager fragmentManager) {
        BiometricFragment biometricFragment = findBiometricFragment(fragmentManager);
        if (biometricFragment == null) {
            BiometricFragment biometricFragment2 = BiometricFragment.newInstance();
            fragmentManager.beginTransaction().add(biometricFragment2, BIOMETRIC_FRAGMENT_TAG).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
            return biometricFragment2;
        }
        return biometricFragment;
    }
}
