package okhttp3.internal.http;

import java.io.IOException;
import java.util.List;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.GzipSource;
import okio.Okio;

/* compiled from: BridgeInterceptor.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u0002J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u000e"}, m238d2 = {"Lokhttp3/internal/http/BridgeInterceptor;", "Lokhttp3/Interceptor;", "cookieJar", "Lokhttp3/CookieJar;", "(Lokhttp3/CookieJar;)V", "cookieHeader", "", "cookies", "", "Lokhttp3/Cookie;", "intercept", "Lokhttp3/Response;", "chain", "Lokhttp3/Interceptor$Chain;", "okhttp"}, m239k = 1, m240mv = {1, 4, 0})
/* loaded from: classes11.dex */
public final class BridgeInterceptor implements Interceptor {
    private final CookieJar cookieJar;

    public BridgeInterceptor(CookieJar cookieJar) {
        Intrinsics.checkNotNullParameter(cookieJar, "cookieJar");
        this.cookieJar = cookieJar;
    }

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        ResponseBody responseBody;
        Intrinsics.checkNotNullParameter(chain, "chain");
        Request userRequest = chain.request();
        Request.Builder requestBuilder = userRequest.newBuilder();
        RequestBody body = userRequest.body();
        if (body != null) {
            MediaType contentType = body.getContentType();
            if (contentType != null) {
                requestBuilder.header(com.google.common.net.HttpHeaders.CONTENT_TYPE, contentType.getMediaType());
            }
            long contentLength = body.contentLength();
            if (contentLength != -1) {
                requestBuilder.header(com.google.common.net.HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
                requestBuilder.removeHeader(com.google.common.net.HttpHeaders.TRANSFER_ENCODING);
            } else {
                requestBuilder.header(com.google.common.net.HttpHeaders.TRANSFER_ENCODING, "chunked");
                requestBuilder.removeHeader(com.google.common.net.HttpHeaders.CONTENT_LENGTH);
            }
        }
        if (userRequest.header(com.google.common.net.HttpHeaders.HOST) == null) {
            requestBuilder.header(com.google.common.net.HttpHeaders.HOST, Util.toHostHeader$default(userRequest.url(), false, 1, null));
        }
        if (userRequest.header(com.google.common.net.HttpHeaders.CONNECTION) == null) {
            requestBuilder.header(com.google.common.net.HttpHeaders.CONNECTION, "Keep-Alive");
        }
        boolean transparentGzip = false;
        if (userRequest.header(com.google.common.net.HttpHeaders.ACCEPT_ENCODING) == null && userRequest.header(com.google.common.net.HttpHeaders.RANGE) == null) {
            transparentGzip = true;
            requestBuilder.header(com.google.common.net.HttpHeaders.ACCEPT_ENCODING, "gzip");
        }
        List cookies = this.cookieJar.loadForRequest(userRequest.url());
        if (!cookies.isEmpty()) {
            requestBuilder.header(com.google.common.net.HttpHeaders.COOKIE, cookieHeader(cookies));
        }
        if (userRequest.header(com.google.common.net.HttpHeaders.USER_AGENT) == null) {
            requestBuilder.header(com.google.common.net.HttpHeaders.USER_AGENT, Util.userAgent);
        }
        Response networkResponse = chain.proceed(requestBuilder.build());
        HttpHeaders.receiveHeaders(this.cookieJar, userRequest.url(), networkResponse.headers());
        Response.Builder responseBuilder = networkResponse.newBuilder().request(userRequest);
        if (transparentGzip && StringsKt.equals("gzip", Response.header$default(networkResponse, com.google.common.net.HttpHeaders.CONTENT_ENCODING, null, 2, null), true) && HttpHeaders.promisesBody(networkResponse) && (responseBody = networkResponse.body()) != null) {
            GzipSource gzipSource = new GzipSource(responseBody.getSource());
            Headers strippedHeaders = networkResponse.headers().newBuilder().removeAll(com.google.common.net.HttpHeaders.CONTENT_ENCODING).removeAll(com.google.common.net.HttpHeaders.CONTENT_LENGTH).build();
            responseBuilder.headers(strippedHeaders);
            responseBuilder.body(new RealResponseBody(Response.header$default(networkResponse, com.google.common.net.HttpHeaders.CONTENT_TYPE, null, 2, null), -1L, Okio.buffer(gzipSource)));
        }
        return responseBuilder.build();
    }

    private final String cookieHeader(List<Cookie> cookies) {
        StringBuilder $this$buildString = new StringBuilder();
        List<Cookie> $this$forEachIndexed$iv = cookies;
        int index = 0;
        for (Object item$iv : $this$forEachIndexed$iv) {
            int index$iv = index + 1;
            if (index < 0) {
                CollectionsKt.throwIndexOverflow();
            }
            Cookie cookie = (Cookie) item$iv;
            if (index > 0) {
                $this$buildString.append("; ");
            }
            $this$buildString.append(cookie.name());
            $this$buildString.append('=');
            $this$buildString.append(cookie.value());
            index = index$iv;
        }
        String sb = $this$buildString.toString();
        Intrinsics.checkNotNullExpressionValue(sb, "StringBuilder().apply(builderAction).toString()");
        return sb;
    }
}
