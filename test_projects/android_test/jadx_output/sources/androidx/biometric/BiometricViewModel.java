package androidx.biometric;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import androidx.biometric.AuthenticationCallbackProvider;
import androidx.biometric.BiometricPrompt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class BiometricViewModel extends ViewModel {
    private AuthenticationCallbackProvider mAuthenticationCallbackProvider;
    private MutableLiveData<BiometricErrorData> mAuthenticationError;
    private MutableLiveData<CharSequence> mAuthenticationHelpMessage;
    private MutableLiveData<BiometricPrompt.AuthenticationResult> mAuthenticationResult;
    private CancellationSignalProvider mCancellationSignalProvider;
    private BiometricPrompt.AuthenticationCallback mClientCallback;
    private Executor mClientExecutor;
    private BiometricPrompt.CryptoObject mCryptoObject;
    private MutableLiveData<CharSequence> mFingerprintDialogHelpMessage;
    private MutableLiveData<Integer> mFingerprintDialogState;
    private MutableLiveData<Boolean> mIsAuthenticationFailurePending;
    private boolean mIsAwaitingResult;
    private boolean mIsConfirmingDeviceCredential;
    private boolean mIsDelayingPrompt;
    private MutableLiveData<Boolean> mIsFingerprintDialogCancelPending;
    private boolean mIsIgnoringCancel;
    private MutableLiveData<Boolean> mIsNegativeButtonPressPending;
    private boolean mIsPromptShowing;
    private DialogInterface.OnClickListener mNegativeButtonListener;
    private CharSequence mNegativeButtonTextOverride;
    private BiometricPrompt.PromptInfo mPromptInfo;
    private int mCanceledFrom = 0;
    private boolean mIsFingerprintDialogDismissedInstantly = true;
    private int mFingerprintDialogPreviousState = 0;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class DefaultExecutor implements Executor {
        private final Handler mHandler = new Handler(Looper.getMainLooper());

        DefaultExecutor() {
        }

        @Override // java.util.concurrent.Executor
        public void execute(Runnable runnable) {
            this.mHandler.post(runnable);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class CallbackListener extends AuthenticationCallbackProvider.Listener {
        private final WeakReference<BiometricViewModel> mViewModelRef;

        CallbackListener(BiometricViewModel viewModel) {
            this.mViewModelRef = new WeakReference<>(viewModel);
        }

        @Override // androidx.biometric.AuthenticationCallbackProvider.Listener
        void onSuccess(BiometricPrompt.AuthenticationResult result) {
            if (this.mViewModelRef.get() != null && this.mViewModelRef.get().isAwaitingResult()) {
                if (result.getAuthenticationType() == -1) {
                    result = new BiometricPrompt.AuthenticationResult(result.getCryptoObject(), this.mViewModelRef.get().getInferredAuthenticationResultType());
                }
                this.mViewModelRef.get().setAuthenticationResult(result);
            }
        }

        @Override // androidx.biometric.AuthenticationCallbackProvider.Listener
        void onError(int errorCode, CharSequence errorMessage) {
            if (this.mViewModelRef.get() != null && !this.mViewModelRef.get().isConfirmingDeviceCredential() && this.mViewModelRef.get().isAwaitingResult()) {
                this.mViewModelRef.get().setAuthenticationError(new BiometricErrorData(errorCode, errorMessage));
            }
        }

        @Override // androidx.biometric.AuthenticationCallbackProvider.Listener
        void onHelp(CharSequence helpMessage) {
            if (this.mViewModelRef.get() != null) {
                this.mViewModelRef.get().setAuthenticationHelpMessage(helpMessage);
            }
        }

        @Override // androidx.biometric.AuthenticationCallbackProvider.Listener
        void onFailure() {
            if (this.mViewModelRef.get() != null && this.mViewModelRef.get().isAwaitingResult()) {
                this.mViewModelRef.get().setAuthenticationFailurePending(true);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class NegativeButtonListener implements DialogInterface.OnClickListener {
        private final WeakReference<BiometricViewModel> mViewModelRef;

        NegativeButtonListener(BiometricViewModel viewModel) {
            this.mViewModelRef = new WeakReference<>(viewModel);
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int which) {
            if (this.mViewModelRef.get() != null) {
                this.mViewModelRef.get().setNegativeButtonPressPending(true);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Executor getClientExecutor() {
        Executor executor = this.mClientExecutor;
        return executor != null ? executor : new DefaultExecutor();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setClientExecutor(Executor clientExecutor) {
        this.mClientExecutor = clientExecutor;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public BiometricPrompt.AuthenticationCallback getClientCallback() {
        if (this.mClientCallback == null) {
            this.mClientCallback = new BiometricPrompt.AuthenticationCallback() { // from class: androidx.biometric.BiometricViewModel.1
            };
        }
        return this.mClientCallback;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setClientCallback(BiometricPrompt.AuthenticationCallback clientCallback) {
        this.mClientCallback = clientCallback;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void resetClientCallback() {
        this.mClientCallback = null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setPromptInfo(BiometricPrompt.PromptInfo promptInfo) {
        this.mPromptInfo = promptInfo;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public CharSequence getTitle() {
        BiometricPrompt.PromptInfo promptInfo = this.mPromptInfo;
        if (promptInfo != null) {
            return promptInfo.getTitle();
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public CharSequence getSubtitle() {
        BiometricPrompt.PromptInfo promptInfo = this.mPromptInfo;
        if (promptInfo != null) {
            return promptInfo.getSubtitle();
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public CharSequence getDescription() {
        BiometricPrompt.PromptInfo promptInfo = this.mPromptInfo;
        if (promptInfo != null) {
            return promptInfo.getDescription();
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public CharSequence getNegativeButtonText() {
        CharSequence charSequence = this.mNegativeButtonTextOverride;
        if (charSequence != null) {
            return charSequence;
        }
        BiometricPrompt.PromptInfo promptInfo = this.mPromptInfo;
        if (promptInfo != null) {
            return promptInfo.getNegativeButtonText();
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isConfirmationRequired() {
        BiometricPrompt.PromptInfo promptInfo = this.mPromptInfo;
        return promptInfo == null || promptInfo.isConfirmationRequired();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getAllowedAuthenticators() {
        BiometricPrompt.PromptInfo promptInfo = this.mPromptInfo;
        if (promptInfo != null) {
            return AuthenticatorUtils.getConsolidatedAuthenticators(promptInfo, this.mCryptoObject);
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public BiometricPrompt.CryptoObject getCryptoObject() {
        return this.mCryptoObject;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setCryptoObject(BiometricPrompt.CryptoObject cryptoObject) {
        this.mCryptoObject = cryptoObject;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AuthenticationCallbackProvider getAuthenticationCallbackProvider() {
        if (this.mAuthenticationCallbackProvider == null) {
            this.mAuthenticationCallbackProvider = new AuthenticationCallbackProvider(new CallbackListener(this));
        }
        return this.mAuthenticationCallbackProvider;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public CancellationSignalProvider getCancellationSignalProvider() {
        if (this.mCancellationSignalProvider == null) {
            this.mCancellationSignalProvider = new CancellationSignalProvider();
        }
        return this.mCancellationSignalProvider;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DialogInterface.OnClickListener getNegativeButtonListener() {
        if (this.mNegativeButtonListener == null) {
            this.mNegativeButtonListener = new NegativeButtonListener(this);
        }
        return this.mNegativeButtonListener;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setNegativeButtonTextOverride(CharSequence negativeButtonTextOverride) {
        this.mNegativeButtonTextOverride = negativeButtonTextOverride;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getCanceledFrom() {
        return this.mCanceledFrom;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setCanceledFrom(int canceledFrom) {
        this.mCanceledFrom = canceledFrom;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isPromptShowing() {
        return this.mIsPromptShowing;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setPromptShowing(boolean promptShowing) {
        this.mIsPromptShowing = promptShowing;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isAwaitingResult() {
        return this.mIsAwaitingResult;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setAwaitingResult(boolean awaitingResult) {
        this.mIsAwaitingResult = awaitingResult;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isConfirmingDeviceCredential() {
        return this.mIsConfirmingDeviceCredential;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setConfirmingDeviceCredential(boolean confirmingDeviceCredential) {
        this.mIsConfirmingDeviceCredential = confirmingDeviceCredential;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isDelayingPrompt() {
        return this.mIsDelayingPrompt;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setDelayingPrompt(boolean delayingPrompt) {
        this.mIsDelayingPrompt = delayingPrompt;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isIgnoringCancel() {
        return this.mIsIgnoringCancel;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setIgnoringCancel(boolean ignoringCancel) {
        this.mIsIgnoringCancel = ignoringCancel;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LiveData<BiometricPrompt.AuthenticationResult> getAuthenticationResult() {
        if (this.mAuthenticationResult == null) {
            this.mAuthenticationResult = new MutableLiveData<>();
        }
        return this.mAuthenticationResult;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setAuthenticationResult(BiometricPrompt.AuthenticationResult authenticationResult) {
        if (this.mAuthenticationResult == null) {
            this.mAuthenticationResult = new MutableLiveData<>();
        }
        updateValue(this.mAuthenticationResult, authenticationResult);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public MutableLiveData<BiometricErrorData> getAuthenticationError() {
        if (this.mAuthenticationError == null) {
            this.mAuthenticationError = new MutableLiveData<>();
        }
        return this.mAuthenticationError;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setAuthenticationError(BiometricErrorData authenticationError) {
        if (this.mAuthenticationError == null) {
            this.mAuthenticationError = new MutableLiveData<>();
        }
        updateValue(this.mAuthenticationError, authenticationError);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LiveData<CharSequence> getAuthenticationHelpMessage() {
        if (this.mAuthenticationHelpMessage == null) {
            this.mAuthenticationHelpMessage = new MutableLiveData<>();
        }
        return this.mAuthenticationHelpMessage;
    }

    void setAuthenticationHelpMessage(CharSequence authenticationHelpMessage) {
        if (this.mAuthenticationHelpMessage == null) {
            this.mAuthenticationHelpMessage = new MutableLiveData<>();
        }
        updateValue(this.mAuthenticationHelpMessage, authenticationHelpMessage);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LiveData<Boolean> isAuthenticationFailurePending() {
        if (this.mIsAuthenticationFailurePending == null) {
            this.mIsAuthenticationFailurePending = new MutableLiveData<>();
        }
        return this.mIsAuthenticationFailurePending;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setAuthenticationFailurePending(boolean authenticationFailurePending) {
        if (this.mIsAuthenticationFailurePending == null) {
            this.mIsAuthenticationFailurePending = new MutableLiveData<>();
        }
        updateValue(this.mIsAuthenticationFailurePending, Boolean.valueOf(authenticationFailurePending));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LiveData<Boolean> isNegativeButtonPressPending() {
        if (this.mIsNegativeButtonPressPending == null) {
            this.mIsNegativeButtonPressPending = new MutableLiveData<>();
        }
        return this.mIsNegativeButtonPressPending;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setNegativeButtonPressPending(boolean negativeButtonPressPending) {
        if (this.mIsNegativeButtonPressPending == null) {
            this.mIsNegativeButtonPressPending = new MutableLiveData<>();
        }
        updateValue(this.mIsNegativeButtonPressPending, Boolean.valueOf(negativeButtonPressPending));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isFingerprintDialogDismissedInstantly() {
        return this.mIsFingerprintDialogDismissedInstantly;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setFingerprintDialogDismissedInstantly(boolean fingerprintDialogDismissedInstantly) {
        this.mIsFingerprintDialogDismissedInstantly = fingerprintDialogDismissedInstantly;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LiveData<Boolean> isFingerprintDialogCancelPending() {
        if (this.mIsFingerprintDialogCancelPending == null) {
            this.mIsFingerprintDialogCancelPending = new MutableLiveData<>();
        }
        return this.mIsFingerprintDialogCancelPending;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setFingerprintDialogCancelPending(boolean fingerprintDialogCancelPending) {
        if (this.mIsFingerprintDialogCancelPending == null) {
            this.mIsFingerprintDialogCancelPending = new MutableLiveData<>();
        }
        updateValue(this.mIsFingerprintDialogCancelPending, Boolean.valueOf(fingerprintDialogCancelPending));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getFingerprintDialogPreviousState() {
        return this.mFingerprintDialogPreviousState;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setFingerprintDialogPreviousState(int fingerprintDialogPreviousState) {
        this.mFingerprintDialogPreviousState = fingerprintDialogPreviousState;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LiveData<Integer> getFingerprintDialogState() {
        if (this.mFingerprintDialogState == null) {
            this.mFingerprintDialogState = new MutableLiveData<>();
        }
        return this.mFingerprintDialogState;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setFingerprintDialogState(int fingerprintDialogState) {
        if (this.mFingerprintDialogState == null) {
            this.mFingerprintDialogState = new MutableLiveData<>();
        }
        updateValue(this.mFingerprintDialogState, Integer.valueOf(fingerprintDialogState));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LiveData<CharSequence> getFingerprintDialogHelpMessage() {
        if (this.mFingerprintDialogHelpMessage == null) {
            this.mFingerprintDialogHelpMessage = new MutableLiveData<>();
        }
        return this.mFingerprintDialogHelpMessage;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setFingerprintDialogHelpMessage(CharSequence fingerprintDialogHelpMessage) {
        if (this.mFingerprintDialogHelpMessage == null) {
            this.mFingerprintDialogHelpMessage = new MutableLiveData<>();
        }
        updateValue(this.mFingerprintDialogHelpMessage, fingerprintDialogHelpMessage);
    }

    int getInferredAuthenticationResultType() {
        int authenticators = getAllowedAuthenticators();
        if (AuthenticatorUtils.isSomeBiometricAllowed(authenticators) && !AuthenticatorUtils.isDeviceCredentialAllowed(authenticators)) {
            return 2;
        }
        return -1;
    }

    private static <T> void updateValue(MutableLiveData<T> liveData, T value) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            liveData.setValue(value);
        } else {
            liveData.postValue(value);
        }
    }
}
