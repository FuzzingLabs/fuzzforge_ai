package androidx.biometric;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import androidx.biometric.BiometricPrompt;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.core.os.CancellationSignal;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class BiometricFragment extends Fragment {
    static final int CANCELED_FROM_CLIENT = 3;
    static final int CANCELED_FROM_INTERNAL = 0;
    static final int CANCELED_FROM_NEGATIVE_BUTTON = 2;
    static final int CANCELED_FROM_USER = 1;
    private static final int DISMISS_INSTANTLY_DELAY_MS = 500;
    private static final String FINGERPRINT_DIALOG_FRAGMENT_TAG = "androidx.biometric.FingerprintDialogFragment";
    private static final int HIDE_DIALOG_DELAY_MS = 2000;
    private static final int REQUEST_CONFIRM_CREDENTIAL = 1;
    private static final int SHOW_PROMPT_DELAY_MS = 600;
    private static final String TAG = "BiometricFragment";
    Handler mHandler = new Handler(Looper.getMainLooper());
    BiometricViewModel mViewModel;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class PromptExecutor implements Executor {
        private final Handler mPromptHandler = new Handler(Looper.getMainLooper());

        PromptExecutor() {
        }

        @Override // java.util.concurrent.Executor
        public void execute(Runnable runnable) {
            this.mPromptHandler.post(runnable);
        }
    }

    /* loaded from: classes.dex */
    private static class ShowPromptForAuthenticationRunnable implements Runnable {
        private final WeakReference<BiometricFragment> mFragmentRef;

        ShowPromptForAuthenticationRunnable(BiometricFragment fragment) {
            this.mFragmentRef = new WeakReference<>(fragment);
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.mFragmentRef.get() != null) {
                this.mFragmentRef.get().showPromptForAuthentication();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class StopDelayingPromptRunnable implements Runnable {
        private final WeakReference<BiometricViewModel> mViewModelRef;

        StopDelayingPromptRunnable(BiometricViewModel viewModel) {
            this.mViewModelRef = new WeakReference<>(viewModel);
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.mViewModelRef.get() != null) {
                this.mViewModelRef.get().setDelayingPrompt(false);
            }
        }
    }

    /* loaded from: classes.dex */
    private static class StopIgnoringCancelRunnable implements Runnable {
        private final WeakReference<BiometricViewModel> mViewModelRef;

        StopIgnoringCancelRunnable(BiometricViewModel viewModel) {
            this.mViewModelRef = new WeakReference<>(viewModel);
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.mViewModelRef.get() != null) {
                this.mViewModelRef.get().setIgnoringCancel(false);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static BiometricFragment newInstance() {
        return new BiometricFragment();
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectViewModel();
    }

    @Override // androidx.fragment.app.Fragment
    public void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT == 29 && AuthenticatorUtils.isDeviceCredentialAllowed(this.mViewModel.getAllowedAuthenticators())) {
            this.mViewModel.setIgnoringCancel(true);
            this.mHandler.postDelayed(new StopIgnoringCancelRunnable(this.mViewModel), 250L);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT < 29 && !this.mViewModel.isConfirmingDeviceCredential() && !isChangingConfigurations()) {
            cancelAuthentication(0);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            this.mViewModel.setConfirmingDeviceCredential(false);
            handleConfirmCredentialResult(resultCode);
        }
    }

    private void connectViewModel() {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        BiometricViewModel biometricViewModel = (BiometricViewModel) new ViewModelProvider(getActivity()).get(BiometricViewModel.class);
        this.mViewModel = biometricViewModel;
        biometricViewModel.getAuthenticationResult().observe(this, new Observer<BiometricPrompt.AuthenticationResult>() { // from class: androidx.biometric.BiometricFragment.1
            @Override // androidx.lifecycle.Observer
            public void onChanged(BiometricPrompt.AuthenticationResult authenticationResult) {
                if (authenticationResult != null) {
                    BiometricFragment.this.onAuthenticationSucceeded(authenticationResult);
                    BiometricFragment.this.mViewModel.setAuthenticationResult(null);
                }
            }
        });
        this.mViewModel.getAuthenticationError().observe(this, new Observer<BiometricErrorData>() { // from class: androidx.biometric.BiometricFragment.2
            @Override // androidx.lifecycle.Observer
            public void onChanged(BiometricErrorData authenticationError) {
                if (authenticationError != null) {
                    BiometricFragment.this.onAuthenticationError(authenticationError.getErrorCode(), authenticationError.getErrorMessage());
                    BiometricFragment.this.mViewModel.setAuthenticationError(null);
                }
            }
        });
        this.mViewModel.getAuthenticationHelpMessage().observe(this, new Observer<CharSequence>() { // from class: androidx.biometric.BiometricFragment.3
            @Override // androidx.lifecycle.Observer
            public void onChanged(CharSequence authenticationHelpMessage) {
                if (authenticationHelpMessage != null) {
                    BiometricFragment.this.onAuthenticationHelp(authenticationHelpMessage);
                    BiometricFragment.this.mViewModel.setAuthenticationError(null);
                }
            }
        });
        this.mViewModel.isAuthenticationFailurePending().observe(this, new Observer<Boolean>() { // from class: androidx.biometric.BiometricFragment.4
            @Override // androidx.lifecycle.Observer
            public void onChanged(Boolean authenticationFailurePending) {
                if (authenticationFailurePending.booleanValue()) {
                    BiometricFragment.this.onAuthenticationFailed();
                    BiometricFragment.this.mViewModel.setAuthenticationFailurePending(false);
                }
            }
        });
        this.mViewModel.isNegativeButtonPressPending().observe(this, new Observer<Boolean>() { // from class: androidx.biometric.BiometricFragment.5
            @Override // androidx.lifecycle.Observer
            public void onChanged(Boolean negativeButtonPressPending) {
                if (negativeButtonPressPending.booleanValue()) {
                    if (BiometricFragment.this.isManagingDeviceCredentialButton()) {
                        BiometricFragment.this.onDeviceCredentialButtonPressed();
                    } else {
                        BiometricFragment.this.onCancelButtonPressed();
                    }
                    BiometricFragment.this.mViewModel.setNegativeButtonPressPending(false);
                }
            }
        });
        this.mViewModel.isFingerprintDialogCancelPending().observe(this, new Observer<Boolean>() { // from class: androidx.biometric.BiometricFragment.6
            @Override // androidx.lifecycle.Observer
            public void onChanged(Boolean fingerprintDialogCancelPending) {
                if (fingerprintDialogCancelPending.booleanValue()) {
                    BiometricFragment.this.cancelAuthentication(1);
                    BiometricFragment.this.dismiss();
                    BiometricFragment.this.mViewModel.setFingerprintDialogCancelPending(false);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void authenticate(BiometricPrompt.PromptInfo info, BiometricPrompt.CryptoObject crypto) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            Log.e(TAG, "Not launching prompt. Client activity was null.");
            return;
        }
        this.mViewModel.setPromptInfo(info);
        int authenticators = AuthenticatorUtils.getConsolidatedAuthenticators(info, crypto);
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 30 && authenticators == 15 && crypto == null) {
            this.mViewModel.setCryptoObject(CryptoObjectUtils.createFakeCryptoObject());
        } else {
            this.mViewModel.setCryptoObject(crypto);
        }
        if (isManagingDeviceCredentialButton()) {
            this.mViewModel.setNegativeButtonTextOverride(getString(C0161R.string.confirm_device_credential_password));
        } else {
            this.mViewModel.setNegativeButtonTextOverride(null);
        }
        if (Build.VERSION.SDK_INT >= 21 && isManagingDeviceCredentialButton() && BiometricManager.from(activity).canAuthenticate(255) != 0) {
            this.mViewModel.setAwaitingResult(true);
            launchConfirmCredentialActivity();
        } else if (this.mViewModel.isDelayingPrompt()) {
            this.mHandler.postDelayed(new ShowPromptForAuthenticationRunnable(this), 600L);
        } else {
            showPromptForAuthentication();
        }
    }

    void showPromptForAuthentication() {
        if (!this.mViewModel.isPromptShowing()) {
            if (getContext() == null) {
                Log.w(TAG, "Not showing biometric prompt. Context is null.");
                return;
            }
            this.mViewModel.setPromptShowing(true);
            this.mViewModel.setAwaitingResult(true);
            if (isUsingFingerprintDialog()) {
                showFingerprintDialogForAuthentication();
            } else {
                showBiometricPromptForAuthentication();
            }
        }
    }

    private void showFingerprintDialogForAuthentication() {
        Context context = requireContext().getApplicationContext();
        FingerprintManagerCompat fingerprintManagerCompat = FingerprintManagerCompat.from(context);
        int errorCode = checkForFingerprintPreAuthenticationErrors(fingerprintManagerCompat);
        if (errorCode != 0) {
            sendErrorAndDismiss(errorCode, ErrorUtils.getFingerprintErrorString(context, errorCode));
            return;
        }
        if (isAdded()) {
            this.mViewModel.setFingerprintDialogDismissedInstantly(true);
            if (!DeviceUtils.shouldHideFingerprintDialog(context, Build.MODEL)) {
                this.mHandler.postDelayed(new Runnable() { // from class: androidx.biometric.BiometricFragment.7
                    @Override // java.lang.Runnable
                    public void run() {
                        BiometricFragment.this.mViewModel.setFingerprintDialogDismissedInstantly(false);
                    }
                }, 500L);
                FingerprintDialogFragment dialog = FingerprintDialogFragment.newInstance();
                dialog.show(getParentFragmentManager(), FINGERPRINT_DIALOG_FRAGMENT_TAG);
            }
            this.mViewModel.setCanceledFrom(0);
            authenticateWithFingerprint(fingerprintManagerCompat, context);
        }
    }

    private void showBiometricPromptForAuthentication() {
        BiometricPrompt.Builder builder = Api28Impl.createPromptBuilder(requireContext().getApplicationContext());
        CharSequence title = this.mViewModel.getTitle();
        CharSequence subtitle = this.mViewModel.getSubtitle();
        CharSequence description = this.mViewModel.getDescription();
        if (title != null) {
            Api28Impl.setTitle(builder, title);
        }
        if (subtitle != null) {
            Api28Impl.setSubtitle(builder, subtitle);
        }
        if (description != null) {
            Api28Impl.setDescription(builder, description);
        }
        CharSequence negativeButtonText = this.mViewModel.getNegativeButtonText();
        if (!TextUtils.isEmpty(negativeButtonText)) {
            Api28Impl.setNegativeButton(builder, negativeButtonText, this.mViewModel.getClientExecutor(), this.mViewModel.getNegativeButtonListener());
        }
        if (Build.VERSION.SDK_INT >= 29) {
            Api29Impl.setConfirmationRequired(builder, this.mViewModel.isConfirmationRequired());
        }
        int authenticators = this.mViewModel.getAllowedAuthenticators();
        if (Build.VERSION.SDK_INT >= 30) {
            Api30Impl.setAllowedAuthenticators(builder, authenticators);
        } else if (Build.VERSION.SDK_INT >= 29) {
            Api29Impl.setDeviceCredentialAllowed(builder, AuthenticatorUtils.isDeviceCredentialAllowed(authenticators));
        }
        authenticateWithBiometricPrompt(Api28Impl.buildPrompt(builder), getContext());
    }

    void authenticateWithFingerprint(FingerprintManagerCompat fingerprintManager, Context context) {
        FingerprintManagerCompat.CryptoObject crypto = CryptoObjectUtils.wrapForFingerprintManager(this.mViewModel.getCryptoObject());
        CancellationSignal cancellationSignal = this.mViewModel.getCancellationSignalProvider().getFingerprintCancellationSignal();
        FingerprintManagerCompat.AuthenticationCallback callback = this.mViewModel.getAuthenticationCallbackProvider().getFingerprintCallback();
        try {
            fingerprintManager.authenticate(crypto, 0, cancellationSignal, callback, null);
        } catch (NullPointerException e) {
            Log.e(TAG, "Got NPE while authenticating with fingerprint.", e);
            sendErrorAndDismiss(1, ErrorUtils.getFingerprintErrorString(context, 1));
        }
    }

    void authenticateWithBiometricPrompt(android.hardware.biometrics.BiometricPrompt biometricPrompt, Context context) {
        String errorString;
        BiometricPrompt.CryptoObject cryptoObject = CryptoObjectUtils.wrapForBiometricPrompt(this.mViewModel.getCryptoObject());
        android.os.CancellationSignal cancellationSignal = this.mViewModel.getCancellationSignalProvider().getBiometricCancellationSignal();
        Executor executor = new PromptExecutor();
        BiometricPrompt.AuthenticationCallback callback = this.mViewModel.getAuthenticationCallbackProvider().getBiometricCallback();
        try {
            if (cryptoObject == null) {
                Api28Impl.authenticate(biometricPrompt, cancellationSignal, executor, callback);
            } else {
                Api28Impl.authenticate(biometricPrompt, cryptoObject, cancellationSignal, executor, callback);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "Got NPE while authenticating with biometric prompt.", e);
            if (context != null) {
                errorString = context.getString(C0161R.string.default_error_msg);
            } else {
                errorString = "";
            }
            sendErrorAndDismiss(1, errorString);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void cancelAuthentication(int canceledFrom) {
        if (canceledFrom != 3 && this.mViewModel.isIgnoringCancel()) {
            return;
        }
        if (isUsingFingerprintDialog()) {
            this.mViewModel.setCanceledFrom(canceledFrom);
            if (canceledFrom == 1) {
                sendErrorToClient(10, ErrorUtils.getFingerprintErrorString(getContext(), 10));
            }
        }
        this.mViewModel.getCancellationSignalProvider().cancel();
    }

    void dismiss() {
        this.mViewModel.setPromptShowing(false);
        dismissFingerprintDialog();
        if (!this.mViewModel.isConfirmingDeviceCredential() && isAdded()) {
            getParentFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        }
        Context context = getContext();
        if (context != null && DeviceUtils.shouldDelayShowingPrompt(context, Build.MODEL)) {
            this.mViewModel.setDelayingPrompt(true);
            this.mHandler.postDelayed(new StopDelayingPromptRunnable(this.mViewModel), 600L);
        }
    }

    private void dismissFingerprintDialog() {
        this.mViewModel.setPromptShowing(false);
        if (isAdded()) {
            FragmentManager fragmentManager = getParentFragmentManager();
            FingerprintDialogFragment fingerprintDialog = (FingerprintDialogFragment) fragmentManager.findFragmentByTag(FINGERPRINT_DIALOG_FRAGMENT_TAG);
            if (fingerprintDialog != null) {
                if (fingerprintDialog.isAdded()) {
                    fingerprintDialog.dismissAllowingStateLoss();
                } else {
                    fragmentManager.beginTransaction().remove(fingerprintDialog).commitAllowingStateLoss();
                }
            }
        }
    }

    void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
        sendSuccessAndDismiss(result);
    }

    void onAuthenticationError(int errorCode, CharSequence errorMessage) {
        final int knownErrorCode;
        CharSequence errorString;
        final CharSequence errorString2;
        if (ErrorUtils.isKnownError(errorCode)) {
            knownErrorCode = errorCode;
        } else {
            knownErrorCode = 8;
        }
        Context context = getContext();
        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 29 && ErrorUtils.isLockoutError(knownErrorCode) && context != null && KeyguardUtils.isDeviceSecuredWithCredential(context) && AuthenticatorUtils.isDeviceCredentialAllowed(this.mViewModel.getAllowedAuthenticators())) {
            launchConfirmCredentialActivity();
            return;
        }
        if (isUsingFingerprintDialog()) {
            if (errorMessage != null) {
                errorString2 = errorMessage;
            } else {
                errorString2 = ErrorUtils.getFingerprintErrorString(getContext(), knownErrorCode);
            }
            if (knownErrorCode == 5) {
                int canceledFrom = this.mViewModel.getCanceledFrom();
                if (canceledFrom == 0 || canceledFrom == 3) {
                    sendErrorToClient(knownErrorCode, errorString2);
                }
                dismiss();
                return;
            }
            if (this.mViewModel.isFingerprintDialogDismissedInstantly()) {
                sendErrorAndDismiss(knownErrorCode, errorString2);
            } else {
                showFingerprintErrorMessage(errorString2);
                this.mHandler.postDelayed(new Runnable() { // from class: androidx.biometric.BiometricFragment.8
                    @Override // java.lang.Runnable
                    public void run() {
                        BiometricFragment.this.sendErrorAndDismiss(knownErrorCode, errorString2);
                    }
                }, getDismissDialogDelay());
            }
            this.mViewModel.setFingerprintDialogDismissedInstantly(true);
            return;
        }
        if (errorMessage != null) {
            errorString = errorMessage;
        } else {
            errorString = getString(C0161R.string.default_error_msg) + " " + knownErrorCode;
        }
        sendErrorAndDismiss(knownErrorCode, errorString);
    }

    void onAuthenticationHelp(CharSequence helpMessage) {
        if (isUsingFingerprintDialog()) {
            showFingerprintErrorMessage(helpMessage);
        }
    }

    void onAuthenticationFailed() {
        if (isUsingFingerprintDialog()) {
            showFingerprintErrorMessage(getString(C0161R.string.fingerprint_not_recognized));
        }
        sendFailureToClient();
    }

    void onDeviceCredentialButtonPressed() {
        if (Build.VERSION.SDK_INT < 21) {
            Log.e(TAG, "Failed to check device credential. Not supported prior to API 21.");
        } else {
            launchConfirmCredentialActivity();
        }
    }

    void onCancelButtonPressed() {
        CharSequence string;
        CharSequence negativeButtonText = this.mViewModel.getNegativeButtonText();
        if (negativeButtonText != null) {
            string = negativeButtonText;
        } else {
            string = getString(C0161R.string.default_error_msg);
        }
        sendErrorAndDismiss(13, string);
        cancelAuthentication(2);
    }

    private void launchConfirmCredentialActivity() {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            Log.e(TAG, "Failed to check device credential. Client FragmentActivity not found.");
            return;
        }
        KeyguardManager keyguardManager = KeyguardUtils.getKeyguardManager(activity);
        if (keyguardManager == null) {
            sendErrorAndDismiss(12, getString(C0161R.string.generic_error_no_keyguard));
            return;
        }
        CharSequence title = this.mViewModel.getTitle();
        CharSequence subtitle = this.mViewModel.getSubtitle();
        CharSequence description = this.mViewModel.getDescription();
        CharSequence credentialDescription = subtitle != null ? subtitle : description;
        Intent intent = Api21Impl.createConfirmDeviceCredentialIntent(keyguardManager, title, credentialDescription);
        if (intent == null) {
            sendErrorAndDismiss(14, getString(C0161R.string.generic_error_no_device_credential));
            return;
        }
        this.mViewModel.setConfirmingDeviceCredential(true);
        if (isUsingFingerprintDialog()) {
            dismissFingerprintDialog();
        }
        intent.setFlags(134742016);
        startActivityForResult(intent, 1);
    }

    private void handleConfirmCredentialResult(int resultCode) {
        if (resultCode == -1) {
            sendSuccessAndDismiss(new BiometricPrompt.AuthenticationResult(null, 1));
        } else {
            sendErrorAndDismiss(10, getString(C0161R.string.generic_error_user_canceled));
        }
    }

    private void showFingerprintErrorMessage(CharSequence errorMessage) {
        CharSequence helpMessage;
        if (errorMessage != null) {
            helpMessage = errorMessage;
        } else {
            helpMessage = getString(C0161R.string.default_error_msg);
        }
        this.mViewModel.setFingerprintDialogState(2);
        this.mViewModel.setFingerprintDialogHelpMessage(helpMessage);
    }

    private void sendSuccessAndDismiss(BiometricPrompt.AuthenticationResult result) {
        sendSuccessToClient(result);
        dismiss();
    }

    void sendErrorAndDismiss(int errorCode, CharSequence errorString) {
        sendErrorToClient(errorCode, errorString);
        dismiss();
    }

    private void sendSuccessToClient(final BiometricPrompt.AuthenticationResult result) {
        if (!this.mViewModel.isAwaitingResult()) {
            Log.w(TAG, "Success not sent to client. Client is not awaiting a result.");
        } else {
            this.mViewModel.setAwaitingResult(false);
            this.mViewModel.getClientExecutor().execute(new Runnable() { // from class: androidx.biometric.BiometricFragment.9
                @Override // java.lang.Runnable
                public void run() {
                    BiometricFragment.this.mViewModel.getClientCallback().onAuthenticationSucceeded(result);
                }
            });
        }
    }

    private void sendErrorToClient(final int errorCode, final CharSequence errorString) {
        if (this.mViewModel.isConfirmingDeviceCredential()) {
            Log.v(TAG, "Error not sent to client. User is confirming their device credential.");
        } else if (!this.mViewModel.isAwaitingResult()) {
            Log.w(TAG, "Error not sent to client. Client is not awaiting a result.");
        } else {
            this.mViewModel.setAwaitingResult(false);
            this.mViewModel.getClientExecutor().execute(new Runnable() { // from class: androidx.biometric.BiometricFragment.10
                @Override // java.lang.Runnable
                public void run() {
                    BiometricFragment.this.mViewModel.getClientCallback().onAuthenticationError(errorCode, errorString);
                }
            });
        }
    }

    private void sendFailureToClient() {
        if (!this.mViewModel.isAwaitingResult()) {
            Log.w(TAG, "Failure not sent to client. Client is not awaiting a result.");
        } else {
            this.mViewModel.getClientExecutor().execute(new Runnable() { // from class: androidx.biometric.BiometricFragment.11
                @Override // java.lang.Runnable
                public void run() {
                    BiometricFragment.this.mViewModel.getClientCallback().onAuthenticationFailed();
                }
            });
        }
    }

    private static int checkForFingerprintPreAuthenticationErrors(FingerprintManagerCompat fingerprintManager) {
        if (!fingerprintManager.isHardwareDetected()) {
            return 12;
        }
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            return 11;
        }
        return 0;
    }

    boolean isManagingDeviceCredentialButton() {
        return Build.VERSION.SDK_INT <= 28 && AuthenticatorUtils.isDeviceCredentialAllowed(this.mViewModel.getAllowedAuthenticators());
    }

    private boolean isUsingFingerprintDialog() {
        return Build.VERSION.SDK_INT < 28 || isFingerprintDialogNeededForCrypto() || isFingerprintDialogNeededForErrorHandling();
    }

    private boolean isFingerprintDialogNeededForCrypto() {
        FragmentActivity activity = getActivity();
        return (activity == null || this.mViewModel.getCryptoObject() == null || !DeviceUtils.shouldUseFingerprintForCrypto(activity, Build.MANUFACTURER, Build.MODEL)) ? false : true;
    }

    private boolean isFingerprintDialogNeededForErrorHandling() {
        return Build.VERSION.SDK_INT == 28 && !PackageUtils.hasSystemFeatureFingerprint(getContext());
    }

    private boolean isChangingConfigurations() {
        FragmentActivity activity = getActivity();
        return activity != null && activity.isChangingConfigurations();
    }

    private int getDismissDialogDelay() {
        Context context = getContext();
        if (context != null && DeviceUtils.shouldHideFingerprintDialog(context, Build.MODEL)) {
            return 0;
        }
        return HIDE_DIALOG_DELAY_MS;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Api30Impl {
        private Api30Impl() {
        }

        static void setAllowedAuthenticators(BiometricPrompt.Builder builder, int allowedAuthenticators) {
            builder.setAllowedAuthenticators(allowedAuthenticators);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Api29Impl {
        private Api29Impl() {
        }

        static void setConfirmationRequired(BiometricPrompt.Builder builder, boolean confirmationRequired) {
            builder.setConfirmationRequired(confirmationRequired);
        }

        static void setDeviceCredentialAllowed(BiometricPrompt.Builder builder, boolean deviceCredentialAllowed) {
            builder.setDeviceCredentialAllowed(deviceCredentialAllowed);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Api28Impl {
        private Api28Impl() {
        }

        static BiometricPrompt.Builder createPromptBuilder(Context context) {
            return new BiometricPrompt.Builder(context);
        }

        static void setTitle(BiometricPrompt.Builder builder, CharSequence title) {
            builder.setTitle(title);
        }

        static void setSubtitle(BiometricPrompt.Builder builder, CharSequence subtitle) {
            builder.setSubtitle(subtitle);
        }

        static void setDescription(BiometricPrompt.Builder builder, CharSequence description) {
            builder.setDescription(description);
        }

        static void setNegativeButton(BiometricPrompt.Builder builder, CharSequence text, Executor executor, DialogInterface.OnClickListener listener) {
            builder.setNegativeButton(text, executor, listener);
        }

        static android.hardware.biometrics.BiometricPrompt buildPrompt(BiometricPrompt.Builder builder) {
            return builder.build();
        }

        static void authenticate(android.hardware.biometrics.BiometricPrompt biometricPrompt, android.os.CancellationSignal cancellationSignal, Executor executor, BiometricPrompt.AuthenticationCallback callback) {
            biometricPrompt.authenticate(cancellationSignal, executor, callback);
        }

        static void authenticate(android.hardware.biometrics.BiometricPrompt biometricPrompt, BiometricPrompt.CryptoObject crypto, android.os.CancellationSignal cancellationSignal, Executor executor, BiometricPrompt.AuthenticationCallback callback) {
            biometricPrompt.authenticate(crypto, cancellationSignal, executor, callback);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Api21Impl {
        private Api21Impl() {
        }

        static Intent createConfirmDeviceCredentialIntent(KeyguardManager keyguardManager, CharSequence title, CharSequence description) {
            return keyguardManager.createConfirmDeviceCredentialIntent(title, description);
        }
    }
}
