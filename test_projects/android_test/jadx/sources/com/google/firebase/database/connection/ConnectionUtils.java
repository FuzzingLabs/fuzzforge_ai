package com.google.firebase.database.connection;

import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class ConnectionUtils {
    public static List<String> stringToPath(String string) {
        List<String> path = new ArrayList<>();
        String[] segments = string.split("/", -1);
        for (int i = 0; i < segments.length; i++) {
            if (!segments[i].isEmpty()) {
                path.add(segments[i]);
            }
        }
        return path;
    }

    public static String pathToString(List<String> segments) {
        if (segments.isEmpty()) {
            return "/";
        }
        StringBuilder path = new StringBuilder();
        boolean first = true;
        for (String segment : segments) {
            if (!first) {
                path.append("/");
            }
            first = false;
            path.append(segment);
        }
        return path.toString();
    }

    public static Long longFromObject(Object o) {
        if (o instanceof Integer) {
            return Long.valueOf(((Integer) o).intValue());
        }
        if (o instanceof Long) {
            return (Long) o;
        }
        return null;
    }

    public static void hardAssert(boolean condition) {
        hardAssert(condition, "", new Object[0]);
    }

    public static void hardAssert(boolean condition, String message, Object... args) {
        if (!condition) {
            throw new AssertionError("hardAssert failed: " + String.format(message, args));
        }
    }
}
