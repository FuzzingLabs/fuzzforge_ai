package com.airbnb.lottie;

import androidx.core.os.TraceCompat;

/* renamed from: com.airbnb.lottie.L */
/* loaded from: classes.dex */
public class C0633L {
    private static final int MAX_DEPTH = 20;
    public static final String TAG = "LOTTIE";
    private static String[] sections;
    private static long[] startTimeNs;
    public static boolean DBG = false;
    private static boolean traceEnabled = false;
    private static int traceDepth = 0;
    private static int depthPastMaxDepth = 0;

    public static void setTraceEnabled(boolean enabled) {
        if (traceEnabled == enabled) {
            return;
        }
        traceEnabled = enabled;
        if (enabled) {
            sections = new String[20];
            startTimeNs = new long[20];
        }
    }

    public static void beginSection(String section) {
        if (!traceEnabled) {
            return;
        }
        int i = traceDepth;
        if (i == 20) {
            depthPastMaxDepth++;
            return;
        }
        sections[i] = section;
        startTimeNs[i] = System.nanoTime();
        TraceCompat.beginSection(section);
        traceDepth++;
    }

    public static float endSection(String section) {
        int i = depthPastMaxDepth;
        if (i > 0) {
            depthPastMaxDepth = i - 1;
            return 0.0f;
        }
        if (!traceEnabled) {
            return 0.0f;
        }
        int i2 = traceDepth - 1;
        traceDepth = i2;
        if (i2 == -1) {
            throw new IllegalStateException("Can't end trace section. There are none.");
        }
        if (!section.equals(sections[i2])) {
            throw new IllegalStateException("Unbalanced trace call " + section + ". Expected " + sections[traceDepth] + ".");
        }
        TraceCompat.endSection();
        return ((float) (System.nanoTime() - startTimeNs[traceDepth])) / 1000000.0f;
    }
}
