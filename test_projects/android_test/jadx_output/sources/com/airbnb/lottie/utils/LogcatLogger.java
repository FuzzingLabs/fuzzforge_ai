package com.airbnb.lottie.utils;

import android.util.Log;
import com.airbnb.lottie.C0633L;
import com.airbnb.lottie.LottieLogger;
import java.util.HashSet;
import java.util.Set;

/* loaded from: classes.dex */
public class LogcatLogger implements LottieLogger {
    private static final Set<String> loggedMessages = new HashSet();

    @Override // com.airbnb.lottie.LottieLogger
    public void debug(String message) {
        debug(message, null);
    }

    @Override // com.airbnb.lottie.LottieLogger
    public void debug(String message, Throwable exception) {
        if (C0633L.DBG) {
            Log.d(C0633L.TAG, message, exception);
        }
    }

    @Override // com.airbnb.lottie.LottieLogger
    public void warning(String message) {
        warning(message, null);
    }

    @Override // com.airbnb.lottie.LottieLogger
    public void warning(String message, Throwable exception) {
        Set<String> set = loggedMessages;
        if (set.contains(message)) {
            return;
        }
        Log.w(C0633L.TAG, message, exception);
        set.add(message);
    }

    @Override // com.airbnb.lottie.LottieLogger
    public void error(String message, Throwable exception) {
        if (C0633L.DBG) {
            Log.d(C0633L.TAG, message, exception);
        }
    }
}
