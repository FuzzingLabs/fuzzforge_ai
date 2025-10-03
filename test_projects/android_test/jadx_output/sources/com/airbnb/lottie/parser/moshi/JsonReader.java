package com.airbnb.lottie.parser.moshi;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;

/* loaded from: classes.dex */
public abstract class JsonReader implements Closeable {
    private static final String[] REPLACEMENT_CHARS = new String[128];
    boolean failOnUnknown;
    boolean lenient;
    int stackSize;
    int[] scopes = new int[32];
    String[] pathNames = new String[32];
    int[] pathIndices = new int[32];

    /* loaded from: classes.dex */
    public enum Token {
        BEGIN_ARRAY,
        END_ARRAY,
        BEGIN_OBJECT,
        END_OBJECT,
        NAME,
        STRING,
        NUMBER,
        BOOLEAN,
        NULL,
        END_DOCUMENT
    }

    public abstract void beginArray() throws IOException;

    public abstract void beginObject() throws IOException;

    public abstract void endArray() throws IOException;

    public abstract void endObject() throws IOException;

    public abstract boolean hasNext() throws IOException;

    public abstract boolean nextBoolean() throws IOException;

    public abstract double nextDouble() throws IOException;

    public abstract int nextInt() throws IOException;

    public abstract String nextName() throws IOException;

    public abstract String nextString() throws IOException;

    public abstract Token peek() throws IOException;

    public abstract int selectName(Options options) throws IOException;

    public abstract void skipName() throws IOException;

    public abstract void skipValue() throws IOException;

    static {
        for (int i = 0; i <= 31; i++) {
            REPLACEMENT_CHARS[i] = String.format("\\u%04x", Integer.valueOf(i));
        }
        String[] strArr = REPLACEMENT_CHARS;
        strArr[34] = "\\\"";
        strArr[92] = "\\\\";
        strArr[9] = "\\t";
        strArr[8] = "\\b";
        strArr[10] = "\\n";
        strArr[13] = "\\r";
        strArr[12] = "\\f";
    }

    /* renamed from: of */
    public static JsonReader m15of(BufferedSource source) {
        return new JsonUtf8Reader(source);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void pushScope(int newTop) {
        int i = this.stackSize;
        int[] iArr = this.scopes;
        if (i == iArr.length) {
            if (i == 256) {
                throw new JsonDataException("Nesting too deep at " + getPath());
            }
            this.scopes = Arrays.copyOf(iArr, iArr.length * 2);
            String[] strArr = this.pathNames;
            this.pathNames = (String[]) Arrays.copyOf(strArr, strArr.length * 2);
            int[] iArr2 = this.pathIndices;
            this.pathIndices = Arrays.copyOf(iArr2, iArr2.length * 2);
        }
        int[] iArr3 = this.scopes;
        int i2 = this.stackSize;
        this.stackSize = i2 + 1;
        iArr3[i2] = newTop;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final JsonEncodingException syntaxError(String message) throws JsonEncodingException {
        throw new JsonEncodingException(message + " at path " + getPath());
    }

    public final String getPath() {
        return JsonScope.getPath(this.stackSize, this.scopes, this.pathNames, this.pathIndices);
    }

    /* loaded from: classes.dex */
    public static final class Options {
        final okio.Options doubleQuoteSuffix;
        final String[] strings;

        private Options(String[] strings, okio.Options doubleQuoteSuffix) {
            this.strings = strings;
            this.doubleQuoteSuffix = doubleQuoteSuffix;
        }

        /* renamed from: of */
        public static Options m16of(String... strings) {
            try {
                ByteString[] result = new ByteString[strings.length];
                Buffer buffer = new Buffer();
                for (int i = 0; i < strings.length; i++) {
                    JsonReader.string(buffer, strings[i]);
                    buffer.readByte();
                    result[i] = buffer.readByteString();
                }
                return new Options((String[]) strings.clone(), okio.Options.m270of(result));
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002b  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static void string(okio.BufferedSink r7, java.lang.String r8) throws java.io.IOException {
        /*
            java.lang.String[] r0 = com.airbnb.lottie.parser.moshi.JsonReader.REPLACEMENT_CHARS
            r1 = 34
            r7.writeByte(r1)
            r2 = 0
            int r3 = r8.length()
            r4 = 0
        Ld:
            if (r4 >= r3) goto L36
            char r5 = r8.charAt(r4)
            r6 = 128(0x80, float:1.8E-43)
            if (r5 >= r6) goto L1c
            r6 = r0[r5]
            if (r6 != 0) goto L29
            goto L33
        L1c:
            r6 = 8232(0x2028, float:1.1535E-41)
            if (r5 != r6) goto L23
            java.lang.String r6 = "\\u2028"
            goto L29
        L23:
            r6 = 8233(0x2029, float:1.1537E-41)
            if (r5 != r6) goto L33
            java.lang.String r6 = "\\u2029"
        L29:
            if (r2 >= r4) goto L2e
            r7.writeUtf8(r8, r2, r4)
        L2e:
            r7.writeUtf8(r6)
            int r2 = r4 + 1
        L33:
            int r4 = r4 + 1
            goto Ld
        L36:
            if (r2 >= r3) goto L3b
            r7.writeUtf8(r8, r2, r3)
        L3b:
            r7.writeByte(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airbnb.lottie.parser.moshi.JsonReader.string(okio.BufferedSink, java.lang.String):void");
    }
}
