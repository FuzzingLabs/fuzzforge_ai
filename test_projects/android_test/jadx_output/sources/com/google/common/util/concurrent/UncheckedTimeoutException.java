package com.google.common.util.concurrent;

/* loaded from: classes.dex */
public class UncheckedTimeoutException extends RuntimeException {
    private static final long serialVersionUID = 0;

    public UncheckedTimeoutException() {
    }

    public UncheckedTimeoutException(String message) {
        super(message);
    }

    public UncheckedTimeoutException(Throwable cause) {
        super(cause);
    }

    public UncheckedTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
