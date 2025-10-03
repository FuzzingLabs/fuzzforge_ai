package com.google.firebase.database.connection;

import java.net.URI;

/* loaded from: classes.dex */
public class HostInfo {
    private static final String LAST_SESSION_ID_PARAM = "ls";
    private static final String VERSION_PARAM = "v";
    private final String host;
    private final String namespace;
    private final boolean secure;

    public HostInfo(String host, String namespace, boolean secure) {
        this.host = host;
        this.namespace = namespace;
        this.secure = secure;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("http");
        sb.append(this.secure ? "s" : "");
        sb.append("://");
        sb.append(this.host);
        return sb.toString();
    }

    public static URI getConnectionUrl(String host, boolean secure, String namespace, String optLastSessionId) {
        String scheme = secure ? "wss" : "ws";
        String url = scheme + "://" + host + "/.ws?ns=" + namespace + "&" + VERSION_PARAM + "=5";
        if (optLastSessionId != null) {
            url = url + "&ls=" + optLastSessionId;
        }
        return URI.create(url);
    }

    public String getHost() {
        return this.host;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public boolean isSecure() {
        return this.secure;
    }
}
