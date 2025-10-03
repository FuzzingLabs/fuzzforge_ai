package androidx.biometric;

import java.util.Arrays;

/* loaded from: classes.dex */
class BiometricErrorData {
    private final int mErrorCode;
    private final CharSequence mErrorMessage;

    BiometricErrorData(int errorCode, CharSequence errorMessage) {
        this.mErrorCode = errorCode;
        this.mErrorMessage = errorMessage;
    }

    int getErrorCode() {
        return this.mErrorCode;
    }

    CharSequence getErrorMessage() {
        return this.mErrorMessage;
    }

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Integer.valueOf(this.mErrorCode), convertToString(this.mErrorMessage)});
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BiometricErrorData)) {
            return false;
        }
        BiometricErrorData other = (BiometricErrorData) obj;
        return this.mErrorCode == other.mErrorCode && isErrorMessageEqualTo(other.mErrorMessage);
    }

    private boolean isErrorMessageEqualTo(CharSequence otherMessage) {
        String errorString = convertToString(this.mErrorMessage);
        String otherString = convertToString(otherMessage);
        return (errorString == null && otherString == null) || (errorString != null && errorString.equals(otherString));
    }

    private static String convertToString(CharSequence message) {
        if (message != null) {
            return message.toString();
        }
        return null;
    }
}
