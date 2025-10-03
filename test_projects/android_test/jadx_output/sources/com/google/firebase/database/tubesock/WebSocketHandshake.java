package com.google.firebase.database.tubesock;

import android.util.Base64;
import com.google.common.net.HttpHeaders;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/* loaded from: classes.dex */
class WebSocketHandshake {
    private static final String WEBSOCKET_VERSION = "13";
    private Map<String, String> extraHeaders;
    private String nonce;
    private String protocol;
    private URI url;

    public WebSocketHandshake(URI url, String protocol, Map<String, String> extraHeaders) {
        this.url = null;
        this.protocol = null;
        this.nonce = null;
        this.extraHeaders = null;
        this.url = url;
        this.protocol = protocol;
        this.extraHeaders = extraHeaders;
        this.nonce = createNonce();
    }

    public byte[] getHandshake() {
        String path = this.url.getPath();
        String query = this.url.getQuery();
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        sb.append(query == null ? "" : "?" + query);
        String path2 = sb.toString();
        String host = this.url.getHost();
        if (this.url.getPort() != -1) {
            host = host + ":" + this.url.getPort();
        }
        LinkedHashMap<String, String> header = new LinkedHashMap<>();
        header.put(HttpHeaders.HOST, host);
        header.put(HttpHeaders.UPGRADE, "websocket");
        header.put(HttpHeaders.CONNECTION, HttpHeaders.UPGRADE);
        header.put("Sec-WebSocket-Version", WEBSOCKET_VERSION);
        header.put("Sec-WebSocket-Key", this.nonce);
        String str = this.protocol;
        if (str != null) {
            header.put("Sec-WebSocket-Protocol", str);
        }
        Map<String, String> map = this.extraHeaders;
        if (map != null) {
            for (String fieldName : map.keySet()) {
                if (!header.containsKey(fieldName)) {
                    header.put(fieldName, this.extraHeaders.get(fieldName));
                }
            }
        }
        String handshake = "GET " + path2 + " HTTP/1.1\r\n";
        byte[] tmpHandShakeBytes = ((handshake + generateHeader(header)) + "\r\n").getBytes(Charset.defaultCharset());
        byte[] handshakeBytes = new byte[tmpHandShakeBytes.length];
        System.arraycopy(tmpHandShakeBytes, 0, handshakeBytes, 0, tmpHandShakeBytes.length);
        return handshakeBytes;
    }

    private String generateHeader(LinkedHashMap<String, String> headers) {
        String header = new String();
        for (String fieldName : headers.keySet()) {
            header = header + fieldName + ": " + headers.get(fieldName) + "\r\n";
        }
        return header;
    }

    private String createNonce() {
        byte[] nonce = new byte[16];
        for (int i = 0; i < 16; i++) {
            nonce[i] = (byte) rand(0, 255);
        }
        return Base64.encodeToString(nonce, 2);
    }

    public void verifyServerStatusLine(String statusLine) {
        int statusCode = Integer.parseInt(statusLine.substring(9, 12));
        if (statusCode == 407) {
            throw new WebSocketException("connection failed: proxy authentication not supported");
        }
        if (statusCode == 404) {
            throw new WebSocketException("connection failed: 404 not found");
        }
        if (statusCode != 101) {
            throw new WebSocketException("connection failed: unknown status code " + statusCode);
        }
    }

    public void verifyServerHandshakeHeaders(HashMap<String, String> lowercaseHeaders) {
        if ("websocket".equals(lowercaseHeaders.get("upgrade"))) {
            if (!"upgrade".equals(lowercaseHeaders.get("connection"))) {
                throw new WebSocketException("connection failed: missing header field in server handshake: Connection");
            }
            return;
        }
        throw new WebSocketException("connection failed: missing header field in server handshake: Upgrade");
    }

    private int rand(int min, int max) {
        int rand = (int) ((Math.random() * max) + min);
        return rand;
    }
}
