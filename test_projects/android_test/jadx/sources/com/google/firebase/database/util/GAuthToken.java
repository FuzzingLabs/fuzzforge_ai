package com.google.firebase.database.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class GAuthToken {
    private static final String AUTH_KEY = "auth";
    private static final String TOKEN_KEY = "token";
    private static final String TOKEN_PREFIX = "gauth|";
    private final Map<String, Object> auth;
    private final String token;

    public GAuthToken(String token, Map<String, Object> auth) {
        this.token = token;
        this.auth = auth;
    }

    public static GAuthToken tryParseFromString(String rawToken) {
        if (!rawToken.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        String gauthToken = rawToken.substring(TOKEN_PREFIX.length());
        try {
            Map<String, Object> tokenMap = JsonMapper.parseJson(gauthToken);
            String token = (String) tokenMap.get(TOKEN_KEY);
            Map<String, Object> auth = (Map) tokenMap.get(AUTH_KEY);
            return new GAuthToken(token, auth);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse gauth token", e);
        }
    }

    public String serializeToString() {
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put(TOKEN_KEY, this.token);
        tokenMap.put(AUTH_KEY, this.auth);
        try {
            String json = JsonMapper.serializeJson(tokenMap);
            return TOKEN_PREFIX + json;
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize gauth token", e);
        }
    }

    public String getToken() {
        return this.token;
    }

    public Map<String, Object> getAuth() {
        return this.auth;
    }
}
