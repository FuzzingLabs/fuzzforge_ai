package com.airbnb.lottie.utils;

import com.airbnb.lottie.LottieLogger;

/* loaded from: classes.dex */
public class Logger {
    private static LottieLogger INSTANCE = new LogcatLogger();

    public static void setInstance(LottieLogger instance) {
        INSTANCE = instance;
    }

    public static void debug(String message) {
        INSTANCE.debug(message);
    }

    public static void debug(String message, Throwable exception) {
        INSTANCE.debug(message, exception);
    }

    public static void warning(String message) {
        INSTANCE.warning(message);
    }

    public static void warning(String message, Throwable exception) {
        INSTANCE.warning(message, exception);
    }

    public static void error(String message, Throwable exception) {
        INSTANCE.error(message, exception);
    }
}
