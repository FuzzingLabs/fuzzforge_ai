package com.google.firebase.database.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

/* loaded from: classes.dex */
public class JsonMapper {
    public static String serializeJson(Map<String, Object> object) throws IOException {
        return serializeJsonValue(object);
    }

    public static String serializeJsonValue(Object object) throws IOException {
        if (object == null) {
            return "null";
        }
        if (object instanceof String) {
            return JSONObject.quote((String) object);
        }
        if (object instanceof Number) {
            try {
                return JSONObject.numberToString((Number) object);
            } catch (JSONException e) {
                throw new IOException("Could not serialize number", e);
            }
        }
        if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue() ? "true" : "false";
        }
        try {
            JSONStringer stringer = new JSONStringer();
            serializeJsonValue(object, stringer);
            return stringer.toString();
        } catch (JSONException e2) {
            throw new IOException("Failed to serialize JSON", e2);
        }
    }

    private static void serializeJsonValue(Object object, JSONStringer stringer) throws IOException, JSONException {
        if (object instanceof Map) {
            stringer.object();
            Map<String, Object> map = (Map) object;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                stringer.key(entry.getKey());
                serializeJsonValue(entry.getValue(), stringer);
            }
            stringer.endObject();
            return;
        }
        if (object instanceof Collection) {
            Collection<?> collection = (Collection) object;
            stringer.array();
            Iterator<?> it = collection.iterator();
            while (it.hasNext()) {
                serializeJsonValue(it.next(), stringer);
            }
            stringer.endArray();
            return;
        }
        stringer.value(object);
    }

    public static Map<String, Object> parseJson(String json) throws IOException {
        try {
            return unwrapJsonObject(new JSONObject(json));
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public static Object parseJsonValue(String json) throws IOException {
        try {
            return unwrapJson(new JSONTokener(json).nextValue());
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private static Map<String, Object> unwrapJsonObject(JSONObject jsonObject) throws JSONException {
        Map<String, Object> map = new HashMap<>(jsonObject.length());
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, unwrapJson(jsonObject.get(key)));
        }
        return map;
    }

    private static List<Object> unwrapJsonArray(JSONArray jsonArray) throws JSONException {
        List<Object> list = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(unwrapJson(jsonArray.get(i)));
        }
        return list;
    }

    private static Object unwrapJson(Object o) throws JSONException {
        if (o instanceof JSONObject) {
            return unwrapJsonObject((JSONObject) o);
        }
        if (o instanceof JSONArray) {
            return unwrapJsonArray((JSONArray) o);
        }
        if (o.equals(JSONObject.NULL)) {
            return null;
        }
        return o;
    }
}
