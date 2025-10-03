package com.airbnb.lottie.parser.moshi;

import com.airbnb.lottie.parser.moshi.JsonReader;
import java.io.EOFException;
import java.io.IOException;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class JsonUtf8Reader extends JsonReader {
    private static final long MIN_INCOMPLETE_INTEGER = -922337203685477580L;
    private static final int NUMBER_CHAR_DECIMAL = 3;
    private static final int NUMBER_CHAR_DIGIT = 2;
    private static final int NUMBER_CHAR_EXP_DIGIT = 7;
    private static final int NUMBER_CHAR_EXP_E = 5;
    private static final int NUMBER_CHAR_EXP_SIGN = 6;
    private static final int NUMBER_CHAR_FRACTION_DIGIT = 4;
    private static final int NUMBER_CHAR_NONE = 0;
    private static final int NUMBER_CHAR_SIGN = 1;
    private static final int PEEKED_BEGIN_ARRAY = 3;
    private static final int PEEKED_BEGIN_OBJECT = 1;
    private static final int PEEKED_BUFFERED = 11;
    private static final int PEEKED_BUFFERED_NAME = 15;
    private static final int PEEKED_DOUBLE_QUOTED = 9;
    private static final int PEEKED_DOUBLE_QUOTED_NAME = 13;
    private static final int PEEKED_END_ARRAY = 4;
    private static final int PEEKED_END_OBJECT = 2;
    private static final int PEEKED_EOF = 18;
    private static final int PEEKED_FALSE = 6;
    private static final int PEEKED_LONG = 16;
    private static final int PEEKED_NONE = 0;
    private static final int PEEKED_NULL = 7;
    private static final int PEEKED_NUMBER = 17;
    private static final int PEEKED_SINGLE_QUOTED = 8;
    private static final int PEEKED_SINGLE_QUOTED_NAME = 12;
    private static final int PEEKED_TRUE = 5;
    private static final int PEEKED_UNQUOTED = 10;
    private static final int PEEKED_UNQUOTED_NAME = 14;
    private final Buffer buffer;
    private int peeked = 0;
    private long peekedLong;
    private int peekedNumberLength;
    private String peekedString;
    private final BufferedSource source;
    private static final ByteString SINGLE_QUOTE_OR_SLASH = ByteString.encodeUtf8("'\\");
    private static final ByteString DOUBLE_QUOTE_OR_SLASH = ByteString.encodeUtf8("\"\\");
    private static final ByteString UNQUOTED_STRING_TERMINALS = ByteString.encodeUtf8("{}[]:, \n\t\r\f/\\;#=");
    private static final ByteString LINEFEED_OR_CARRIAGE_RETURN = ByteString.encodeUtf8("\n\r");
    private static final ByteString CLOSING_BLOCK_COMMENT = ByteString.encodeUtf8("*/");

    /* JADX INFO: Access modifiers changed from: package-private */
    public JsonUtf8Reader(BufferedSource source) {
        if (source == null) {
            throw new NullPointerException("source == null");
        }
        this.source = source;
        this.buffer = source.getBuffer();
        pushScope(6);
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public void beginArray() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 3) {
            pushScope(1);
            this.pathIndices[this.stackSize - 1] = 0;
            this.peeked = 0;
        } else {
            throw new JsonDataException("Expected BEGIN_ARRAY but was " + peek() + " at path " + getPath());
        }
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public void endArray() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 4) {
            this.stackSize--;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            this.peeked = 0;
            return;
        }
        throw new JsonDataException("Expected END_ARRAY but was " + peek() + " at path " + getPath());
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public void beginObject() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 1) {
            pushScope(3);
            this.peeked = 0;
            return;
        }
        throw new JsonDataException("Expected BEGIN_OBJECT but was " + peek() + " at path " + getPath());
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public void endObject() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 2) {
            this.stackSize--;
            this.pathNames[this.stackSize] = null;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            this.peeked = 0;
            return;
        }
        throw new JsonDataException("Expected END_OBJECT but was " + peek() + " at path " + getPath());
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public boolean hasNext() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        return (p == 2 || p == 4 || p == 18) ? false : true;
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public JsonReader.Token peek() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        switch (p) {
            case 1:
                return JsonReader.Token.BEGIN_OBJECT;
            case 2:
                return JsonReader.Token.END_OBJECT;
            case 3:
                return JsonReader.Token.BEGIN_ARRAY;
            case 4:
                return JsonReader.Token.END_ARRAY;
            case 5:
            case 6:
                return JsonReader.Token.BOOLEAN;
            case 7:
                return JsonReader.Token.NULL;
            case 8:
            case 9:
            case 10:
            case 11:
                return JsonReader.Token.STRING;
            case 12:
            case 13:
            case 14:
            case 15:
                return JsonReader.Token.NAME;
            case 16:
            case 17:
                return JsonReader.Token.NUMBER;
            case 18:
                return JsonReader.Token.END_DOCUMENT;
            default:
                throw new AssertionError();
        }
    }

    private int doPeek() throws IOException {
        int peekStack = this.scopes[this.stackSize - 1];
        if (peekStack == 1) {
            this.scopes[this.stackSize - 1] = 2;
        } else if (peekStack == 2) {
            int c = nextNonWhitespace(true);
            this.buffer.readByte();
            switch (c) {
                case 44:
                    break;
                case 59:
                    checkLenient();
                    break;
                case 93:
                    this.peeked = 4;
                    return 4;
                default:
                    throw syntaxError("Unterminated array");
            }
        } else {
            if (peekStack == 3 || peekStack == 5) {
                this.scopes[this.stackSize - 1] = 4;
                if (peekStack == 5) {
                    int c2 = nextNonWhitespace(true);
                    this.buffer.readByte();
                    switch (c2) {
                        case 44:
                            break;
                        case 59:
                            checkLenient();
                            break;
                        case 125:
                            this.peeked = 2;
                            return 2;
                        default:
                            throw syntaxError("Unterminated object");
                    }
                }
                int c3 = nextNonWhitespace(true);
                switch (c3) {
                    case 34:
                        this.buffer.readByte();
                        this.peeked = 13;
                        return 13;
                    case 39:
                        this.buffer.readByte();
                        checkLenient();
                        this.peeked = 12;
                        return 12;
                    case 125:
                        if (peekStack != 5) {
                            this.buffer.readByte();
                            this.peeked = 2;
                            return 2;
                        }
                        throw syntaxError("Expected name");
                    default:
                        checkLenient();
                        if (isLiteral((char) c3)) {
                            this.peeked = 14;
                            return 14;
                        }
                        throw syntaxError("Expected name");
                }
            }
            if (peekStack == 4) {
                this.scopes[this.stackSize - 1] = 5;
                int c4 = nextNonWhitespace(true);
                this.buffer.readByte();
                switch (c4) {
                    case 58:
                        break;
                    case 61:
                        checkLenient();
                        if (this.source.request(1L) && this.buffer.getByte(0L) == 62) {
                            this.buffer.readByte();
                            break;
                        }
                        break;
                    default:
                        throw syntaxError("Expected ':'");
                }
            } else if (peekStack == 6) {
                this.scopes[this.stackSize - 1] = 7;
            } else if (peekStack == 7) {
                if (nextNonWhitespace(false) == -1) {
                    this.peeked = 18;
                    return 18;
                }
                checkLenient();
            } else if (peekStack == 8) {
                throw new IllegalStateException("JsonReader is closed");
            }
        }
        switch (nextNonWhitespace(true)) {
            case 34:
                this.buffer.readByte();
                this.peeked = 9;
                return 9;
            case 39:
                checkLenient();
                this.buffer.readByte();
                this.peeked = 8;
                return 8;
            case 44:
            case 59:
                break;
            case 91:
                this.buffer.readByte();
                this.peeked = 3;
                return 3;
            case 93:
                if (peekStack == 1) {
                    this.buffer.readByte();
                    this.peeked = 4;
                    return 4;
                }
                break;
            case 123:
                this.buffer.readByte();
                this.peeked = 1;
                return 1;
            default:
                int result = peekKeyword();
                if (result != 0) {
                    return result;
                }
                int result2 = peekNumber();
                if (result2 == 0) {
                    if (!isLiteral(this.buffer.getByte(0L))) {
                        throw syntaxError("Expected value");
                    }
                    checkLenient();
                    this.peeked = 10;
                    return 10;
                }
                return result2;
        }
        if (peekStack == 1 || peekStack == 2) {
            checkLenient();
            this.peeked = 7;
            return 7;
        }
        throw syntaxError("Unexpected value");
    }

    private int peekKeyword() throws IOException {
        String keyword;
        String keywordUpper;
        int peeking;
        byte c = this.buffer.getByte(0L);
        if (c == 116 || c == 84) {
            keyword = "true";
            keywordUpper = "TRUE";
            peeking = 5;
        } else if (c == 102 || c == 70) {
            keyword = "false";
            keywordUpper = "FALSE";
            peeking = 6;
        } else {
            if (c != 110 && c != 78) {
                return 0;
            }
            keyword = "null";
            keywordUpper = "NULL";
            peeking = 7;
        }
        int length = keyword.length();
        for (int i = 1; i < length; i++) {
            if (!this.source.request(i + 1)) {
                return 0;
            }
            byte c2 = this.buffer.getByte(i);
            if (c2 != keyword.charAt(i) && c2 != keywordUpper.charAt(i)) {
                return 0;
            }
        }
        if (this.source.request(length + 1) && isLiteral(this.buffer.getByte(length))) {
            return 0;
        }
        this.buffer.skip(length);
        this.peeked = peeking;
        return peeking;
    }

    /* JADX WARN: Code restructure failed: missing block: B:43:0x008a, code lost:
    
        if (isLiteral(r6) != false) goto L77;
     */
    /* JADX WARN: Code restructure failed: missing block: B:44:0x008d, code lost:
    
        if (r4 != 2) goto L69;
     */
    /* JADX WARN: Code restructure failed: missing block: B:45:0x008f, code lost:
    
        if (r3 == false) goto L69;
     */
    /* JADX WARN: Code restructure failed: missing block: B:47:0x0095, code lost:
    
        if (r0 != Long.MIN_VALUE) goto L61;
     */
    /* JADX WARN: Code restructure failed: missing block: B:48:0x0097, code lost:
    
        if (r2 == false) goto L69;
     */
    /* JADX WARN: Code restructure failed: missing block: B:50:0x009b, code lost:
    
        if (r0 != 0) goto L64;
     */
    /* JADX WARN: Code restructure failed: missing block: B:51:0x009d, code lost:
    
        if (r2 != false) goto L69;
     */
    /* JADX WARN: Code restructure failed: missing block: B:52:0x009f, code lost:
    
        if (r2 == false) goto L66;
     */
    /* JADX WARN: Code restructure failed: missing block: B:53:0x00a1, code lost:
    
        r6 = r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:54:0x00a4, code lost:
    
        r14.peekedLong = r6;
        r14.buffer.skip(r5);
        r14.peeked = 16;
     */
    /* JADX WARN: Code restructure failed: missing block: B:55:0x00b0, code lost:
    
        return 16;
     */
    /* JADX WARN: Code restructure failed: missing block: B:56:0x00a3, code lost:
    
        r6 = -r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:57:0x00b1, code lost:
    
        if (r4 == 2) goto L75;
     */
    /* JADX WARN: Code restructure failed: missing block: B:58:0x00b3, code lost:
    
        if (r4 == 4) goto L75;
     */
    /* JADX WARN: Code restructure failed: missing block: B:60:0x00b6, code lost:
    
        if (r4 != 7) goto L74;
     */
    /* JADX WARN: Code restructure failed: missing block: B:61:0x00b9, code lost:
    
        return 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:62:0x00ba, code lost:
    
        r14.peekedNumberLength = r5;
        r14.peeked = 17;
     */
    /* JADX WARN: Code restructure failed: missing block: B:63:0x00c0, code lost:
    
        return 17;
     */
    /* JADX WARN: Code restructure failed: missing block: B:64:0x00c1, code lost:
    
        return 0;
     */
    /* JADX WARN: Failed to find 'out' block for switch in B:5:0x0020. Please report as an issue. */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private int peekNumber() throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 216
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airbnb.lottie.parser.moshi.JsonUtf8Reader.peekNumber():int");
    }

    private boolean isLiteral(int c) throws IOException {
        switch (c) {
            case 9:
            case 10:
            case 12:
            case 13:
            case 32:
            case 44:
            case 58:
            case 91:
            case 93:
            case 123:
            case 125:
                return false;
            case 35:
            case 47:
            case 59:
            case 61:
            case 92:
                checkLenient();
                return false;
            default:
                return true;
        }
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public String nextName() throws IOException {
        String result;
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 14) {
            result = nextUnquotedValue();
        } else if (p == 13) {
            result = nextQuotedValue(DOUBLE_QUOTE_OR_SLASH);
        } else if (p == 12) {
            result = nextQuotedValue(SINGLE_QUOTE_OR_SLASH);
        } else if (p == 15) {
            result = this.peekedString;
        } else {
            throw new JsonDataException("Expected a name but was " + peek() + " at path " + getPath());
        }
        this.peeked = 0;
        this.pathNames[this.stackSize - 1] = result;
        return result;
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public int selectName(JsonReader.Options options) throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p < 12 || p > 15) {
            return -1;
        }
        if (p == 15) {
            return findName(this.peekedString, options);
        }
        int result = this.source.select(options.doubleQuoteSuffix);
        if (result != -1) {
            this.peeked = 0;
            this.pathNames[this.stackSize - 1] = options.strings[result];
            return result;
        }
        String lastPathName = this.pathNames[this.stackSize - 1];
        String nextName = nextName();
        int result2 = findName(nextName, options);
        if (result2 == -1) {
            this.peeked = 15;
            this.peekedString = nextName;
            this.pathNames[this.stackSize - 1] = lastPathName;
        }
        return result2;
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public void skipName() throws IOException {
        if (this.failOnUnknown) {
            throw new JsonDataException("Cannot skip unexpected " + peek() + " at " + getPath());
        }
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 14) {
            skipUnquotedValue();
        } else if (p == 13) {
            skipQuotedValue(DOUBLE_QUOTE_OR_SLASH);
        } else if (p == 12) {
            skipQuotedValue(SINGLE_QUOTE_OR_SLASH);
        } else if (p != 15) {
            throw new JsonDataException("Expected a name but was " + peek() + " at path " + getPath());
        }
        this.peeked = 0;
        this.pathNames[this.stackSize - 1] = "null";
    }

    private int findName(String name, JsonReader.Options options) {
        int size = options.strings.length;
        for (int i = 0; i < size; i++) {
            if (name.equals(options.strings[i])) {
                this.peeked = 0;
                this.pathNames[this.stackSize - 1] = name;
                return i;
            }
        }
        return -1;
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public String nextString() throws IOException {
        String result;
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 10) {
            result = nextUnquotedValue();
        } else if (p == 9) {
            result = nextQuotedValue(DOUBLE_QUOTE_OR_SLASH);
        } else if (p == 8) {
            result = nextQuotedValue(SINGLE_QUOTE_OR_SLASH);
        } else if (p == 11) {
            result = this.peekedString;
            this.peekedString = null;
        } else if (p == 16) {
            result = Long.toString(this.peekedLong);
        } else if (p == 17) {
            result = this.buffer.readUtf8(this.peekedNumberLength);
        } else {
            throw new JsonDataException("Expected a string but was " + peek() + " at path " + getPath());
        }
        this.peeked = 0;
        int[] iArr = this.pathIndices;
        int i = this.stackSize - 1;
        iArr[i] = iArr[i] + 1;
        return result;
    }

    private int findString(String string, JsonReader.Options options) {
        int size = options.strings.length;
        for (int i = 0; i < size; i++) {
            if (string.equals(options.strings[i])) {
                this.peeked = 0;
                int[] iArr = this.pathIndices;
                int i2 = this.stackSize - 1;
                iArr[i2] = iArr[i2] + 1;
                return i;
            }
        }
        return -1;
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public boolean nextBoolean() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 5) {
            this.peeked = 0;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return true;
        }
        if (p == 6) {
            this.peeked = 0;
            int[] iArr2 = this.pathIndices;
            int i2 = this.stackSize - 1;
            iArr2[i2] = iArr2[i2] + 1;
            return false;
        }
        throw new JsonDataException("Expected a boolean but was " + peek() + " at path " + getPath());
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public double nextDouble() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 16) {
            this.peeked = 0;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return this.peekedLong;
        }
        if (p == 17) {
            this.peekedString = this.buffer.readUtf8(this.peekedNumberLength);
        } else if (p == 9) {
            this.peekedString = nextQuotedValue(DOUBLE_QUOTE_OR_SLASH);
        } else if (p == 8) {
            this.peekedString = nextQuotedValue(SINGLE_QUOTE_OR_SLASH);
        } else if (p == 10) {
            this.peekedString = nextUnquotedValue();
        } else if (p != 11) {
            throw new JsonDataException("Expected a double but was " + peek() + " at path " + getPath());
        }
        this.peeked = 11;
        try {
            double result = Double.parseDouble(this.peekedString);
            if (!this.lenient && (Double.isNaN(result) || Double.isInfinite(result))) {
                throw new JsonEncodingException("JSON forbids NaN and infinities: " + result + " at path " + getPath());
            }
            this.peekedString = null;
            this.peeked = 0;
            int[] iArr2 = this.pathIndices;
            int i2 = this.stackSize - 1;
            iArr2[i2] = iArr2[i2] + 1;
            return result;
        } catch (NumberFormatException e) {
            throw new JsonDataException("Expected a double but was " + this.peekedString + " at path " + getPath());
        }
    }

    private String nextQuotedValue(ByteString runTerminator) throws IOException {
        StringBuilder builder = null;
        while (true) {
            long index = this.source.indexOfElement(runTerminator);
            if (index == -1) {
                throw syntaxError("Unterminated string");
            }
            if (this.buffer.getByte(index) == 92) {
                if (builder == null) {
                    builder = new StringBuilder();
                }
                builder.append(this.buffer.readUtf8(index));
                this.buffer.readByte();
                builder.append(readEscapeCharacter());
            } else {
                if (builder == null) {
                    String result = this.buffer.readUtf8(index);
                    this.buffer.readByte();
                    return result;
                }
                builder.append(this.buffer.readUtf8(index));
                this.buffer.readByte();
                return builder.toString();
            }
        }
    }

    private String nextUnquotedValue() throws IOException {
        long i = this.source.indexOfElement(UNQUOTED_STRING_TERMINALS);
        return i != -1 ? this.buffer.readUtf8(i) : this.buffer.readUtf8();
    }

    private void skipQuotedValue(ByteString runTerminator) throws IOException {
        while (true) {
            long index = this.source.indexOfElement(runTerminator);
            if (index == -1) {
                throw syntaxError("Unterminated string");
            }
            if (this.buffer.getByte(index) == 92) {
                this.buffer.skip(1 + index);
                readEscapeCharacter();
            } else {
                this.buffer.skip(1 + index);
                return;
            }
        }
    }

    private void skipUnquotedValue() throws IOException {
        long i = this.source.indexOfElement(UNQUOTED_STRING_TERMINALS);
        Buffer buffer = this.buffer;
        buffer.skip(i != -1 ? i : buffer.size());
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public int nextInt() throws IOException {
        String nextQuotedValue;
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 16) {
            long j = this.peekedLong;
            int result = (int) j;
            if (j != result) {
                throw new JsonDataException("Expected an int but was " + this.peekedLong + " at path " + getPath());
            }
            this.peeked = 0;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return result;
        }
        if (p == 17) {
            this.peekedString = this.buffer.readUtf8(this.peekedNumberLength);
        } else if (p == 9 || p == 8) {
            if (p == 9) {
                nextQuotedValue = nextQuotedValue(DOUBLE_QUOTE_OR_SLASH);
            } else {
                nextQuotedValue = nextQuotedValue(SINGLE_QUOTE_OR_SLASH);
            }
            this.peekedString = nextQuotedValue;
            try {
                int result2 = Integer.parseInt(nextQuotedValue);
                this.peeked = 0;
                int[] iArr2 = this.pathIndices;
                int i2 = this.stackSize - 1;
                iArr2[i2] = iArr2[i2] + 1;
                return result2;
            } catch (NumberFormatException e) {
            }
        } else if (p != 11) {
            throw new JsonDataException("Expected an int but was " + peek() + " at path " + getPath());
        }
        this.peeked = 11;
        try {
            double asDouble = Double.parseDouble(this.peekedString);
            int result3 = (int) asDouble;
            if (result3 != asDouble) {
                throw new JsonDataException("Expected an int but was " + this.peekedString + " at path " + getPath());
            }
            this.peekedString = null;
            this.peeked = 0;
            int[] iArr3 = this.pathIndices;
            int i3 = this.stackSize - 1;
            iArr3[i3] = iArr3[i3] + 1;
            return result3;
        } catch (NumberFormatException e2) {
            throw new JsonDataException("Expected an int but was " + this.peekedString + " at path " + getPath());
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.peeked = 0;
        this.scopes[0] = 8;
        this.stackSize = 1;
        this.buffer.clear();
        this.source.close();
    }

    @Override // com.airbnb.lottie.parser.moshi.JsonReader
    public void skipValue() throws IOException {
        if (this.failOnUnknown) {
            throw new JsonDataException("Cannot skip unexpected " + peek() + " at " + getPath());
        }
        int count = 0;
        do {
            int p = this.peeked;
            if (p == 0) {
                p = doPeek();
            }
            if (p == 3) {
                pushScope(1);
                count++;
            } else if (p == 1) {
                pushScope(3);
                count++;
            } else if (p == 4) {
                count--;
                if (count < 0) {
                    throw new JsonDataException("Expected a value but was " + peek() + " at path " + getPath());
                }
                this.stackSize--;
            } else if (p == 2) {
                count--;
                if (count < 0) {
                    throw new JsonDataException("Expected a value but was " + peek() + " at path " + getPath());
                }
                this.stackSize--;
            } else if (p == 14 || p == 10) {
                skipUnquotedValue();
            } else if (p == 9 || p == 13) {
                skipQuotedValue(DOUBLE_QUOTE_OR_SLASH);
            } else if (p == 8 || p == 12) {
                skipQuotedValue(SINGLE_QUOTE_OR_SLASH);
            } else if (p == 17) {
                this.buffer.skip(this.peekedNumberLength);
            } else if (p == 18) {
                throw new JsonDataException("Expected a value but was " + peek() + " at path " + getPath());
            }
            this.peeked = 0;
        } while (count != 0);
        int[] iArr = this.pathIndices;
        int i = this.stackSize - 1;
        iArr[i] = iArr[i] + 1;
        this.pathNames[this.stackSize - 1] = "null";
    }

    private int nextNonWhitespace(boolean throwOnEof) throws IOException {
        int c = 0;
        while (this.source.request(c + 1)) {
            int p = c + 1;
            int c2 = this.buffer.getByte(c);
            if (c2 == 10 || c2 == 32 || c2 == 13 || c2 == 9) {
                c = p;
            } else {
                this.buffer.skip(p - 1);
                if (c2 == 47) {
                    if (!this.source.request(2L)) {
                        return c2;
                    }
                    checkLenient();
                    byte peek = this.buffer.getByte(1L);
                    switch (peek) {
                        case 42:
                            this.buffer.readByte();
                            this.buffer.readByte();
                            if (!skipToEndOfBlockComment()) {
                                throw syntaxError("Unterminated comment");
                            }
                            c = 0;
                            break;
                        case 47:
                            this.buffer.readByte();
                            this.buffer.readByte();
                            skipToEndOfLine();
                            c = 0;
                            break;
                        default:
                            return c2;
                    }
                } else if (c2 == 35) {
                    checkLenient();
                    skipToEndOfLine();
                    c = 0;
                } else {
                    return c2;
                }
            }
        }
        if (throwOnEof) {
            throw new EOFException("End of input");
        }
        return -1;
    }

    private void checkLenient() throws IOException {
        if (!this.lenient) {
            throw syntaxError("Use JsonReader.setLenient(true) to accept malformed JSON");
        }
    }

    private void skipToEndOfLine() throws IOException {
        long index = this.source.indexOfElement(LINEFEED_OR_CARRIAGE_RETURN);
        Buffer buffer = this.buffer;
        buffer.skip(index != -1 ? 1 + index : buffer.size());
    }

    private boolean skipToEndOfBlockComment() throws IOException {
        long index = this.source.indexOf(CLOSING_BLOCK_COMMENT);
        boolean found = index != -1;
        Buffer buffer = this.buffer;
        buffer.skip(found ? r1.size() + index : buffer.size());
        return found;
    }

    public String toString() {
        return "JsonReader(" + this.source + ")";
    }

    private char readEscapeCharacter() throws IOException {
        int i;
        if (!this.source.request(1L)) {
            throw syntaxError("Unterminated escape sequence");
        }
        byte escaped = this.buffer.readByte();
        switch (escaped) {
            case 10:
            case 34:
            case 39:
            case 47:
            case 92:
                return (char) escaped;
            case 98:
                return '\b';
            case 102:
                return '\f';
            case 110:
                return '\n';
            case 114:
                return '\r';
            case 116:
                return '\t';
            case 117:
                if (!this.source.request(4L)) {
                    throw new EOFException("Unterminated escape sequence at path " + getPath());
                }
                char result = 0;
                int end = 0 + 4;
                for (int i2 = 0; i2 < end; i2++) {
                    byte c = this.buffer.getByte(i2);
                    char result2 = (char) (result << 4);
                    if (c >= 48 && c <= 57) {
                        i = c - 48;
                    } else if (c >= 97 && c <= 102) {
                        i = (c - 97) + 10;
                    } else {
                        if (c < 65 || c > 70) {
                            throw syntaxError("\\u" + this.buffer.readUtf8(4L));
                        }
                        i = (c - 65) + 10;
                    }
                    result = (char) (i + result2);
                }
                this.buffer.skip(4L);
                return result;
            default:
                if (!this.lenient) {
                    throw syntaxError("Invalid escape sequence: \\" + ((char) escaped));
                }
                return (char) escaped;
        }
    }
}
