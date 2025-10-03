package okhttp3.internal.http;

import com.google.android.gms.common.internal.ImagesContract;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import kotlin.Deprecated;
import kotlin.DeprecationLevel;
import kotlin.Metadata;
import kotlin.ReplaceWith;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
import okhttp3.Challenge;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.ByteString;

/* compiled from: HttpHeaders.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000R\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0005\n\u0000\u001a\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u001a\u0018\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b*\u00020\n2\u0006\u0010\u000b\u001a\u00020\f\u001a\n\u0010\r\u001a\u00020\u0004*\u00020\u0006\u001a\u001a\u0010\u000e\u001a\u00020\u000f*\u00020\u00102\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\t0\u0012H\u0002\u001a\u000e\u0010\u0013\u001a\u0004\u0018\u00010\f*\u00020\u0010H\u0002\u001a\u000e\u0010\u0014\u001a\u0004\u0018\u00010\f*\u00020\u0010H\u0002\u001a\u001a\u0010\u0015\u001a\u00020\u000f*\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\n\u001a\f\u0010\u001a\u001a\u00020\u0004*\u00020\u0010H\u0002\u001a\u0014\u0010\u001b\u001a\u00020\u0004*\u00020\u00102\u0006\u0010\u001c\u001a\u00020\u001dH\u0002\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082\u0004¢\u0006\u0002\n\u0000\"\u000e\u0010\u0002\u001a\u00020\u0001X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u001e"}, m238d2 = {"QUOTED_STRING_DELIMITERS", "Lokio/ByteString;", "TOKEN_DELIMITERS", "hasBody", "", "response", "Lokhttp3/Response;", "parseChallenges", "", "Lokhttp3/Challenge;", "Lokhttp3/Headers;", "headerName", "", "promisesBody", "readChallengeHeader", "", "Lokio/Buffer;", "result", "", "readQuotedString", "readToken", "receiveHeaders", "Lokhttp3/CookieJar;", ImagesContract.URL, "Lokhttp3/HttpUrl;", "headers", "skipCommasAndWhitespace", "startsWith", "prefix", "", "okhttp"}, m239k = 2, m240mv = {1, 4, 0})
/* loaded from: classes11.dex */
public final class HttpHeaders {
    private static final ByteString QUOTED_STRING_DELIMITERS = ByteString.INSTANCE.encodeUtf8("\"\\");
    private static final ByteString TOKEN_DELIMITERS = ByteString.INSTANCE.encodeUtf8("\t ,=");

    public static final List<Challenge> parseChallenges(Headers parseChallenges, String headerName) {
        Intrinsics.checkNotNullParameter(parseChallenges, "$this$parseChallenges");
        Intrinsics.checkNotNullParameter(headerName, "headerName");
        List result = new ArrayList();
        int size = parseChallenges.size();
        for (int h = 0; h < size; h++) {
            if (StringsKt.equals(headerName, parseChallenges.name(h), true)) {
                Buffer header = new Buffer().writeUtf8(parseChallenges.value(h));
                try {
                    readChallengeHeader(header, result);
                } catch (EOFException e) {
                    Platform.INSTANCE.get().log("Unable to parse challenge", 5, e);
                }
            }
        }
        return result;
    }

    /* JADX WARN: Code restructure failed: missing block: B:22:0x0091, code lost:
    
        r12.add(new okhttp3.Challenge(r2, (java.util.Map<java.lang.String, java.lang.String>) r7));
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private static final void readChallengeHeader(okio.Buffer r11, java.util.List<okhttp3.Challenge> r12) throws java.io.EOFException {
        /*
            r0 = 0
            r1 = r0
            java.lang.String r1 = (java.lang.String) r1
        L4:
            if (r1 != 0) goto L11
            skipCommasAndWhitespace(r11)
            java.lang.String r1 = readToken(r11)
            if (r1 != 0) goto L11
            return
        L11:
            r2 = r1
            boolean r3 = skipCommasAndWhitespace(r11)
            java.lang.String r1 = readToken(r11)
            if (r1 != 0) goto L30
            boolean r0 = r11.exhausted()
            if (r0 != 0) goto L23
            return
        L23:
            okhttp3.Challenge r0 = new okhttp3.Challenge
            java.util.Map r4 = kotlin.collections.MapsKt.emptyMap()
            r0.<init>(r2, r4)
            r12.add(r0)
            return
        L30:
            r4 = 61
            byte r4 = (byte) r4
            int r5 = okhttp3.internal.Util.skipAll(r11, r4)
            boolean r6 = skipCommasAndWhitespace(r11)
            if (r3 != 0) goto L71
            if (r6 != 0) goto L45
            boolean r7 = r11.exhausted()
            if (r7 == 0) goto L71
        L45:
            okhttp3.Challenge r4 = new okhttp3.Challenge
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r1)
            java.lang.String r8 = "="
            java.lang.CharSequence r8 = (java.lang.CharSequence) r8
            java.lang.String r8 = kotlin.text.StringsKt.repeat(r8, r5)
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            java.util.Map r7 = java.util.Collections.singletonMap(r0, r7)
            java.lang.String r8 = "Collections.singletonMap…ek + \"=\".repeat(eqCount))"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r7, r8)
            r4.<init>(r2, r7)
            r12.add(r4)
            r1 = r0
            java.lang.String r1 = (java.lang.String) r1
            goto L4
        L71:
            java.util.LinkedHashMap r7 = new java.util.LinkedHashMap
            r7.<init>()
            java.util.Map r7 = (java.util.Map) r7
            int r8 = okhttp3.internal.Util.skipAll(r11, r4)
            int r5 = r5 + r8
        L7d:
            if (r1 != 0) goto L8f
            java.lang.String r1 = readToken(r11)
            boolean r8 = skipCommasAndWhitespace(r11)
            if (r8 == 0) goto L8b
            goto L91
        L8b:
            int r5 = okhttp3.internal.Util.skipAll(r11, r4)
        L8f:
            if (r5 != 0) goto L9b
        L91:
            okhttp3.Challenge r4 = new okhttp3.Challenge
            r4.<init>(r2, r7)
            r12.add(r4)
            goto L4
        L9b:
            r8 = 1
            if (r5 <= r8) goto L9f
            return
        L9f:
            boolean r8 = skipCommasAndWhitespace(r11)
            if (r8 == 0) goto La6
            return
        La6:
            r8 = 34
            byte r8 = (byte) r8
            boolean r8 = startsWith(r11, r8)
            if (r8 == 0) goto Lb5
            java.lang.String r8 = readQuotedString(r11)
            goto Lb9
        Lb5:
            java.lang.String r8 = readToken(r11)
        Lb9:
            if (r8 == 0) goto Ld5
            java.lang.Object r9 = r7.put(r1, r8)
            java.lang.String r9 = (java.lang.String) r9
            r1 = r0
            java.lang.String r1 = (java.lang.String) r1
            if (r9 == 0) goto Lc7
            return
        Lc7:
            boolean r10 = skipCommasAndWhitespace(r11)
            if (r10 != 0) goto Ld4
            boolean r10 = r11.exhausted()
            if (r10 != 0) goto Ld4
            return
        Ld4:
            goto L7d
        Ld5:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.http.HttpHeaders.readChallengeHeader(okio.Buffer, java.util.List):void");
    }

    private static final boolean skipCommasAndWhitespace(Buffer $this$skipCommasAndWhitespace) {
        boolean commaFound = false;
        while (!$this$skipCommasAndWhitespace.exhausted()) {
            switch ($this$skipCommasAndWhitespace.getByte(0L)) {
                case 9:
                case 32:
                    $this$skipCommasAndWhitespace.readByte();
                    break;
                case 44:
                    $this$skipCommasAndWhitespace.readByte();
                    commaFound = true;
                    break;
                default:
                    return commaFound;
            }
        }
        return commaFound;
    }

    private static final boolean startsWith(Buffer $this$startsWith, byte prefix) {
        return !$this$startsWith.exhausted() && $this$startsWith.getByte(0L) == prefix;
    }

    private static final String readQuotedString(Buffer $this$readQuotedString) throws EOFException {
        byte b = (byte) 34;
        if (!($this$readQuotedString.readByte() == b)) {
            throw new IllegalArgumentException("Failed requirement.".toString());
        }
        Buffer result = new Buffer();
        while (true) {
            long i = $this$readQuotedString.indexOfElement(QUOTED_STRING_DELIMITERS);
            if (i == -1) {
                return null;
            }
            if ($this$readQuotedString.getByte(i) == b) {
                result.write($this$readQuotedString, i);
                $this$readQuotedString.readByte();
                return result.readUtf8();
            }
            if ($this$readQuotedString.size() == i + 1) {
                return null;
            }
            result.write($this$readQuotedString, i);
            $this$readQuotedString.readByte();
            result.write($this$readQuotedString, 1L);
        }
    }

    private static final String readToken(Buffer $this$readToken) {
        long tokenSize = $this$readToken.indexOfElement(TOKEN_DELIMITERS);
        if (tokenSize == -1) {
            tokenSize = $this$readToken.size();
        }
        if (tokenSize != 0) {
            return $this$readToken.readUtf8(tokenSize);
        }
        return null;
    }

    public static final void receiveHeaders(CookieJar receiveHeaders, HttpUrl url, Headers headers) {
        Intrinsics.checkNotNullParameter(receiveHeaders, "$this$receiveHeaders");
        Intrinsics.checkNotNullParameter(url, "url");
        Intrinsics.checkNotNullParameter(headers, "headers");
        if (receiveHeaders == CookieJar.NO_COOKIES) {
            return;
        }
        List cookies = Cookie.INSTANCE.parseAll(url, headers);
        if (cookies.isEmpty()) {
            return;
        }
        receiveHeaders.saveFromResponse(url, cookies);
    }

    public static final boolean promisesBody(Response promisesBody) {
        Intrinsics.checkNotNullParameter(promisesBody, "$this$promisesBody");
        if (Intrinsics.areEqual(promisesBody.request().method(), "HEAD")) {
            return false;
        }
        int responseCode = promisesBody.code();
        return (((responseCode >= 100 && responseCode < 200) || responseCode == 204 || responseCode == 304) && Util.headersContentLength(promisesBody) == -1 && !StringsKt.equals("chunked", Response.header$default(promisesBody, com.google.common.net.HttpHeaders.TRANSFER_ENCODING, null, 2, null), true)) ? false : true;
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "No longer supported", replaceWith = @ReplaceWith(expression = "response.promisesBody()", imports = {}))
    public static final boolean hasBody(Response response) {
        Intrinsics.checkNotNullParameter(response, "response");
        return promisesBody(response);
    }
}
