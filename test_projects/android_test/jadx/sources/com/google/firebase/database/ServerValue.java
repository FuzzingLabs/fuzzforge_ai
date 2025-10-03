package com.google.firebase.database;

import com.google.firebase.database.core.ServerValues;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class ServerValue {
    public static final Map<String, String> TIMESTAMP = createScalarServerValuePlaceholder(ServerValues.NAME_OP_TIMESTAMP);

    public static final Object increment(long delta) {
        return createParameterizedServerValuePlaceholder(ServerValues.NAME_OP_INCREMENT, Long.valueOf(delta));
    }

    public static final Object increment(double delta) {
        return createParameterizedServerValuePlaceholder(ServerValues.NAME_OP_INCREMENT, Double.valueOf(delta));
    }

    private static Map<String, String> createScalarServerValuePlaceholder(String key) {
        Map<String, String> result = new HashMap<>();
        result.put(ServerValues.NAME_SUBKEY_SERVERVALUE, key);
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, Map<String, Object>> createParameterizedServerValuePlaceholder(String name, Object value) {
        Map<String, Object> op = new HashMap<>();
        op.put(name, value);
        Map<String, Map<String, Object>> result = new HashMap<>();
        result.put(ServerValues.NAME_SUBKEY_SERVERVALUE, Collections.unmodifiableMap(op));
        return Collections.unmodifiableMap(result);
    }
}
