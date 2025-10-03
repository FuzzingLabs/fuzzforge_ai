package com.google.gson.internal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/* loaded from: classes.dex */
public class PreJava9DateFormatProvider {
    public static DateFormat getUSDateFormat(int style) {
        return new SimpleDateFormat(getDateFormatPattern(style), Locale.US);
    }

    public static DateFormat getUSDateTimeFormat(int dateStyle, int timeStyle) {
        String pattern = getDatePartOfDateTimePattern(dateStyle) + " " + getTimePartOfDateTimePattern(timeStyle);
        return new SimpleDateFormat(pattern, Locale.US);
    }

    private static String getDateFormatPattern(int style) {
        switch (style) {
            case 0:
                return "EEEE, MMMM d, y";
            case 1:
                return "MMMM d, y";
            case 2:
                return "MMM d, y";
            case 3:
                return "M/d/yy";
            default:
                throw new IllegalArgumentException("Unknown DateFormat style: " + style);
        }
    }

    private static String getDatePartOfDateTimePattern(int dateStyle) {
        switch (dateStyle) {
            case 0:
                return "EEEE, MMMM d, yyyy";
            case 1:
                return "MMMM d, yyyy";
            case 2:
                return "MMM d, yyyy";
            case 3:
                return "M/d/yy";
            default:
                throw new IllegalArgumentException("Unknown DateFormat style: " + dateStyle);
        }
    }

    private static String getTimePartOfDateTimePattern(int timeStyle) {
        switch (timeStyle) {
            case 0:
            case 1:
                return "h:mm:ss a z";
            case 2:
                return "h:mm:ss a";
            case 3:
                return "h:mm a";
            default:
                throw new IllegalArgumentException("Unknown DateFormat style: " + timeStyle);
        }
    }
}
