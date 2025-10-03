package androidx.biometric;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

/* loaded from: classes.dex */
public class FingerprintDialogFragment extends DialogFragment {
    private static final int MESSAGE_DISPLAY_TIME_MS = 2000;
    static final int STATE_FINGERPRINT = 1;
    static final int STATE_FINGERPRINT_AUTHENTICATED = 3;
    static final int STATE_FINGERPRINT_ERROR = 2;
    static final int STATE_NONE = 0;
    private static final String TAG = "FingerprintFragment";
    private int mErrorTextColor;
    private ImageView mFingerprintIcon;
    TextView mHelpMessageView;
    private int mNormalTextColor;
    BiometricViewModel mViewModel;
    final Handler mHandler = new Handler(Looper.getMainLooper());
    final Runnable mResetDialogRunnable = new Runnable() { // from class: androidx.biometric.FingerprintDialogFragment.1
        @Override // java.lang.Runnable
        public void run() {
            FingerprintDialogFragment.this.resetDialog();
        }
    };

    /* JADX INFO: Access modifiers changed from: package-private */
    public static FingerprintDialogFragment newInstance() {
        return new FingerprintDialogFragment();
    }

    @Override // androidx.fragment.app.DialogFragment, androidx.fragment.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        int i;
        super.onCreate(savedInstanceState);
        connectViewModel();
        if (Build.VERSION.SDK_INT >= 26) {
            this.mErrorTextColor = getThemedColorFor(Api26Impl.getColorErrorAttr());
        } else {
            Context context = getContext();
            if (context != null) {
                i = ContextCompat.getColor(context, C0161R.color.biometric_error_color);
            } else {
                i = 0;
            }
            this.mErrorTextColor = i;
        }
        this.mNormalTextColor = getThemedColorFor(android.R.attr.textColorSecondary);
    }

    @Override // androidx.fragment.app.DialogFragment
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence negativeButtonText;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(this.mViewModel.getTitle());
        View layout = LayoutInflater.from(builder.getContext()).inflate(C0161R.layout.fingerprint_dialog_layout, (ViewGroup) null);
        TextView subtitleView = (TextView) layout.findViewById(C0161R.id.fingerprint_subtitle);
        if (subtitleView != null) {
            CharSequence subtitle = this.mViewModel.getSubtitle();
            if (TextUtils.isEmpty(subtitle)) {
                subtitleView.setVisibility(8);
            } else {
                subtitleView.setVisibility(0);
                subtitleView.setText(subtitle);
            }
        }
        TextView descriptionView = (TextView) layout.findViewById(C0161R.id.fingerprint_description);
        if (descriptionView != null) {
            CharSequence description = this.mViewModel.getDescription();
            if (TextUtils.isEmpty(description)) {
                descriptionView.setVisibility(8);
            } else {
                descriptionView.setVisibility(0);
                descriptionView.setText(description);
            }
        }
        this.mFingerprintIcon = (ImageView) layout.findViewById(C0161R.id.fingerprint_icon);
        this.mHelpMessageView = (TextView) layout.findViewById(C0161R.id.fingerprint_error);
        if (AuthenticatorUtils.isDeviceCredentialAllowed(this.mViewModel.getAllowedAuthenticators())) {
            negativeButtonText = getString(C0161R.string.confirm_device_credential_password);
        } else {
            negativeButtonText = this.mViewModel.getNegativeButtonText();
        }
        builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() { // from class: androidx.biometric.FingerprintDialogFragment.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                FingerprintDialogFragment.this.mViewModel.setNegativeButtonPressPending(true);
            }
        });
        builder.setView(layout);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        this.mViewModel.setFingerprintDialogPreviousState(0);
        this.mViewModel.setFingerprintDialogState(1);
        this.mViewModel.setFingerprintDialogHelpMessage(getString(C0161R.string.fingerprint_dialog_touch_sensor));
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
        this.mHandler.removeCallbacksAndMessages(null);
    }

    @Override // androidx.fragment.app.DialogFragment, android.content.DialogInterface.OnCancelListener
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        this.mViewModel.setFingerprintDialogCancelPending(true);
    }

    private void connectViewModel() {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        BiometricViewModel biometricViewModel = (BiometricViewModel) new ViewModelProvider(activity).get(BiometricViewModel.class);
        this.mViewModel = biometricViewModel;
        biometricViewModel.getFingerprintDialogState().observe(this, new Observer<Integer>() { // from class: androidx.biometric.FingerprintDialogFragment.3
            @Override // androidx.lifecycle.Observer
            public void onChanged(Integer state) {
                FingerprintDialogFragment.this.mHandler.removeCallbacks(FingerprintDialogFragment.this.mResetDialogRunnable);
                FingerprintDialogFragment.this.updateFingerprintIcon(state.intValue());
                FingerprintDialogFragment.this.updateHelpMessageColor(state.intValue());
                FingerprintDialogFragment.this.mHandler.postDelayed(FingerprintDialogFragment.this.mResetDialogRunnable, 2000L);
            }
        });
        this.mViewModel.getFingerprintDialogHelpMessage().observe(this, new Observer<CharSequence>() { // from class: androidx.biometric.FingerprintDialogFragment.4
            @Override // androidx.lifecycle.Observer
            public void onChanged(CharSequence helpMessage) {
                FingerprintDialogFragment.this.mHandler.removeCallbacks(FingerprintDialogFragment.this.mResetDialogRunnable);
                FingerprintDialogFragment.this.updateHelpMessageText(helpMessage);
                FingerprintDialogFragment.this.mHandler.postDelayed(FingerprintDialogFragment.this.mResetDialogRunnable, 2000L);
            }
        });
    }

    void updateFingerprintIcon(int state) {
        int previousState;
        Drawable icon;
        if (this.mFingerprintIcon == null || Build.VERSION.SDK_INT < 23 || (icon = getAssetForTransition((previousState = this.mViewModel.getFingerprintDialogPreviousState()), state)) == null) {
            return;
        }
        this.mFingerprintIcon.setImageDrawable(icon);
        if (shouldAnimateForTransition(previousState, state)) {
            Api21Impl.startAnimation(icon);
        }
        this.mViewModel.setFingerprintDialogPreviousState(state);
    }

    void updateHelpMessageColor(int state) {
        TextView textView = this.mHelpMessageView;
        if (textView != null) {
            boolean isError = state == 2;
            textView.setTextColor(isError ? this.mErrorTextColor : this.mNormalTextColor);
        }
    }

    void updateHelpMessageText(CharSequence helpMessage) {
        TextView textView = this.mHelpMessageView;
        if (textView != null) {
            textView.setText(helpMessage);
        }
    }

    void resetDialog() {
        Context context = getContext();
        if (context == null) {
            Log.w(TAG, "Not resetting the dialog. Context is null.");
        } else {
            this.mViewModel.setFingerprintDialogState(1);
            this.mViewModel.setFingerprintDialogHelpMessage(context.getString(C0161R.string.fingerprint_dialog_touch_sensor));
        }
    }

    private int getThemedColorFor(int attr) {
        Context context = getContext();
        FragmentActivity activity = getActivity();
        if (context == null || activity == null) {
            Log.w(TAG, "Unable to get themed color. Context or activity is null.");
            return 0;
        }
        TypedValue tv = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, tv, true);
        TypedArray arr = activity.obtainStyledAttributes(tv.data, new int[]{attr});
        int color = arr.getColor(0, 0);
        arr.recycle();
        return color;
    }

    private boolean shouldAnimateForTransition(int previousState, int state) {
        if (previousState == 0 && state == 1) {
            return false;
        }
        if (previousState == 1 && state == 2) {
            return true;
        }
        return previousState == 2 && state == 1;
    }

    private Drawable getAssetForTransition(int previousState, int state) {
        int iconRes;
        Context context = getContext();
        if (context == null) {
            Log.w(TAG, "Unable to get asset. Context is null.");
            return null;
        }
        if (previousState != 0 || state != 1) {
            if (previousState == 1 && state == 2) {
                iconRes = C0161R.drawable.fingerprint_dialog_error;
            } else if (previousState == 2 && state == 1) {
                iconRes = C0161R.drawable.fingerprint_dialog_fp_icon;
            } else {
                if (previousState != 1 || state != 3) {
                    return null;
                }
                iconRes = C0161R.drawable.fingerprint_dialog_fp_icon;
            }
        } else {
            iconRes = C0161R.drawable.fingerprint_dialog_fp_icon;
        }
        return ContextCompat.getDrawable(context, iconRes);
    }

    /* loaded from: classes.dex */
    private static class Api26Impl {
        private Api26Impl() {
        }

        static int getColorErrorAttr() {
            return C0161R.attr.colorError;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Api21Impl {
        private Api21Impl() {
        }

        static void startAnimation(Drawable icon) {
            if (icon instanceof AnimatedVectorDrawable) {
                ((AnimatedVectorDrawable) icon).start();
            }
        }
    }
}
