package com.google.firebase.database.core.utilities;

import com.google.firebase.database.snapshot.ChildKey;
import java.util.Random;

/* loaded from: classes.dex */
public class PushIdGenerator {
    private static final int MAX_KEY_LEN = 786;
    private static final char MAX_PUSH_CHAR = 'z';
    private static final char MIN_PUSH_CHAR = '-';
    private static final String PUSH_CHARS = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";
    private static final Random randGen = new Random();
    private static long lastPushTime = 0;
    private static final int[] lastRandChars = new int[12];

    public static synchronized String generatePushChildName(long now) {
        String sb;
        synchronized (PushIdGenerator.class) {
            boolean duplicateTime = now == lastPushTime;
            lastPushTime = now;
            char[] timeStampChars = new char[8];
            StringBuilder result = new StringBuilder(20);
            for (int i = 7; i >= 0; i--) {
                timeStampChars[i] = PUSH_CHARS.charAt((int) (now % 64));
                now /= 64;
            }
            Utilities.hardAssert(now == 0);
            result.append(timeStampChars);
            if (!duplicateTime) {
                for (int i2 = 0; i2 < 12; i2++) {
                    lastRandChars[i2] = randGen.nextInt(64);
                }
            } else {
                incrementArray();
            }
            for (int i3 = 0; i3 < 12; i3++) {
                result.append(PUSH_CHARS.charAt(lastRandChars[i3]));
            }
            Utilities.hardAssert(result.length() == 20);
            sb = result.toString();
        }
        return sb;
    }

    public static final String predecessor(String key) {
        Validation.validateNullableKey(key);
        Integer num = Utilities.tryParseInt(key);
        if (num != null) {
            if (num.intValue() != Integer.MIN_VALUE) {
                return String.valueOf(num.intValue() - 1);
            }
            return ChildKey.MIN_KEY_NAME;
        }
        StringBuilder next = new StringBuilder(key);
        if (next.charAt(next.length() - 1) == '-') {
            if (next.length() == 1) {
                return String.valueOf(Integer.MAX_VALUE);
            }
            return next.substring(0, next.length() - 1);
        }
        next.setCharAt(next.length() - 1, PUSH_CHARS.charAt(PUSH_CHARS.indexOf(next.charAt(next.length() - 1)) - 1));
        next.append(new String(new char[786 - next.length()]).replace("\u0000", "z"));
        return next.toString();
    }

    public static final String successor(String key) {
        Validation.validateNullableKey(key);
        Integer num = Utilities.tryParseInt(key);
        if (num != null) {
            if (num.intValue() == Integer.MAX_VALUE) {
                return String.valueOf(MIN_PUSH_CHAR);
            }
            return String.valueOf(num.intValue() + 1);
        }
        StringBuilder next = new StringBuilder(key);
        if (next.length() < MAX_KEY_LEN) {
            next.append(MIN_PUSH_CHAR);
            return next.toString();
        }
        int i = next.length() - 1;
        while (i >= 0 && next.charAt(i) == 'z') {
            i--;
        }
        if (i == -1) {
            return ChildKey.MAX_KEY_NAME;
        }
        char source = next.charAt(i);
        char sourcePlusOne = PUSH_CHARS.charAt(PUSH_CHARS.indexOf(source) + 1);
        next.replace(i, i + 1, String.valueOf(sourcePlusOne));
        return next.substring(0, i + 1);
    }

    private static void incrementArray() {
        for (int i = 11; i >= 0; i--) {
            int[] iArr = lastRandChars;
            if (iArr[i] != 63) {
                iArr[i] = iArr[i] + 1;
                return;
            }
            iArr[i] = 0;
        }
    }
}
