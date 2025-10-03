package okio.internal;

import com.google.common.base.Ascii;
import java.util.Arrays;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import okio.Utf8;

/* compiled from: -Utf8.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\u0016\n\u0000\n\u0002\u0010\u0012\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\u001a\n\u0010\u0000\u001a\u00020\u0001*\u00020\u0002\u001a\u001e\u0010\u0003\u001a\u00020\u0002*\u00020\u00012\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0005¨\u0006\u0007"}, m238d2 = {"commonAsUtf8ToByteArray", "", "", "commonToUtf8String", "beginIndex", "", "endIndex", "okio"}, m239k = 2, m240mv = {1, 4, 0})
/* loaded from: classes11.dex */
public final class _Utf8Kt {
    public static /* synthetic */ String commonToUtf8String$default(byte[] bArr, int i, int i2, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i = 0;
        }
        if ((i3 & 2) != 0) {
            i2 = bArr.length;
        }
        return commonToUtf8String(bArr, i, i2);
    }

    public static final String commonToUtf8String(byte[] commonToUtf8String, int beginIndex, int endIndex) {
        int $i$f$processUtf16Chars;
        int length;
        int length2;
        int i;
        int length3;
        int length4;
        int length5;
        int length6;
        int i2;
        int length7;
        int length8;
        byte b1$iv$iv;
        Intrinsics.checkNotNullParameter(commonToUtf8String, "$this$commonToUtf8String");
        if (beginIndex < 0 || endIndex > commonToUtf8String.length || beginIndex > endIndex) {
            throw new ArrayIndexOutOfBoundsException("size=" + commonToUtf8String.length + " beginIndex=" + beginIndex + " endIndex=" + endIndex);
        }
        char[] chars = new char[endIndex - beginIndex];
        int length9 = 0;
        int $i$f$processUtf16Chars2 = 0;
        int index$iv = beginIndex;
        while (index$iv < endIndex) {
            byte b0$iv = commonToUtf8String[index$iv];
            if (b0$iv >= 0) {
                char c = (char) b0$iv;
                int length10 = length9 + 1;
                chars[length9] = c;
                index$iv++;
                while (index$iv < endIndex && commonToUtf8String[index$iv] >= 0) {
                    int index$iv2 = index$iv + 1;
                    char c2 = (char) commonToUtf8String[index$iv];
                    chars[length10] = c2;
                    index$iv = index$iv2;
                    length10++;
                }
                $i$f$processUtf16Chars = $i$f$processUtf16Chars2;
                length9 = length10;
            } else {
                int other$iv$iv = b0$iv >> 5;
                if (other$iv$iv == -2) {
                    if (endIndex <= index$iv + 1) {
                        char c3 = (char) Utf8.REPLACEMENT_CODE_POINT;
                        int length11 = length9 + 1;
                        chars[length9] = c3;
                        Unit unit = Unit.INSTANCE;
                        length8 = length11;
                        b1$iv$iv = 1;
                        $i$f$processUtf16Chars = $i$f$processUtf16Chars2;
                    } else {
                        byte b0$iv$iv = commonToUtf8String[index$iv];
                        byte b1$iv$iv2 = commonToUtf8String[index$iv + 1];
                        if ((b1$iv$iv2 & 192) == 128) {
                            int codePoint$iv$iv = (b1$iv$iv2 ^ 3968) ^ (b0$iv$iv << 6);
                            if (codePoint$iv$iv < 128) {
                                $i$f$processUtf16Chars = $i$f$processUtf16Chars2;
                                char c4 = (char) Utf8.REPLACEMENT_CODE_POINT;
                                length7 = length9 + 1;
                                chars[length9] = c4;
                            } else {
                                $i$f$processUtf16Chars = $i$f$processUtf16Chars2;
                                char c5 = (char) codePoint$iv$iv;
                                length7 = length9 + 1;
                                chars[length9] = c5;
                            }
                            Unit unit2 = Unit.INSTANCE;
                            length8 = length7;
                            b1$iv$iv = 2;
                        } else {
                            char c6 = (char) Utf8.REPLACEMENT_CODE_POINT;
                            length8 = length9 + 1;
                            chars[length9] = c6;
                            Unit unit3 = Unit.INSTANCE;
                            $i$f$processUtf16Chars = $i$f$processUtf16Chars2;
                            b1$iv$iv = 1;
                        }
                    }
                    index$iv += b1$iv$iv;
                    length9 = length8;
                } else {
                    $i$f$processUtf16Chars = $i$f$processUtf16Chars2;
                    int other$iv$iv2 = b0$iv >> 4;
                    if (other$iv$iv2 == -2) {
                        if (endIndex <= index$iv + 2) {
                            char c7 = (char) Utf8.REPLACEMENT_CODE_POINT;
                            length6 = length9 + 1;
                            chars[length9] = c7;
                            Unit unit4 = Unit.INSTANCE;
                            if (endIndex > index$iv + 1) {
                                int byte$iv$iv$iv = commonToUtf8String[index$iv + 1];
                                int other$iv$iv$iv$iv = 192 & byte$iv$iv$iv;
                                if (other$iv$iv$iv$iv == 128) {
                                    i2 = 2;
                                }
                            }
                            i2 = 1;
                        } else {
                            byte b0$iv$iv2 = commonToUtf8String[index$iv];
                            byte b1$iv$iv3 = commonToUtf8String[index$iv + 1];
                            if ((b1$iv$iv3 & 192) == 128) {
                                int length12 = index$iv + 2;
                                byte b2$iv$iv = commonToUtf8String[length12];
                                if ((b2$iv$iv & 192) == 128) {
                                    int codePoint$iv$iv2 = (((-123008) ^ b2$iv$iv) ^ (b1$iv$iv3 << 6)) ^ (b0$iv$iv2 << Ascii.f159FF);
                                    if (codePoint$iv$iv2 < 2048) {
                                        char c8 = (char) Utf8.REPLACEMENT_CODE_POINT;
                                        length5 = length9 + 1;
                                        chars[length9] = c8;
                                    } else if (55296 <= codePoint$iv$iv2 && 57343 >= codePoint$iv$iv2) {
                                        char c9 = (char) Utf8.REPLACEMENT_CODE_POINT;
                                        length5 = length9 + 1;
                                        chars[length9] = c9;
                                    } else {
                                        char c10 = (char) codePoint$iv$iv2;
                                        length5 = length9 + 1;
                                        chars[length9] = c10;
                                    }
                                    Unit unit5 = Unit.INSTANCE;
                                    length6 = length5;
                                    i2 = 3;
                                } else {
                                    char c11 = (char) Utf8.REPLACEMENT_CODE_POINT;
                                    int length13 = length9 + 1;
                                    chars[length9] = c11;
                                    Unit unit6 = Unit.INSTANCE;
                                    length6 = length13;
                                    i2 = 2;
                                }
                            } else {
                                char c12 = (char) Utf8.REPLACEMENT_CODE_POINT;
                                int length14 = length9 + 1;
                                chars[length9] = c12;
                                Unit unit7 = Unit.INSTANCE;
                                length6 = length14;
                                i2 = 1;
                            }
                        }
                        index$iv += i2;
                        length9 = length6;
                    } else {
                        int other$iv$iv3 = b0$iv >> 3;
                        if (other$iv$iv3 == -2) {
                            if (endIndex <= index$iv + 3) {
                                if (65533 != 65533) {
                                    char c13 = (char) ((Utf8.REPLACEMENT_CODE_POINT >>> 10) + Utf8.HIGH_SURROGATE_HEADER);
                                    int length15 = length9 + 1;
                                    chars[length9] = c13;
                                    char c14 = (char) ((65533 & 1023) + Utf8.LOG_SURROGATE_HEADER);
                                    length3 = length15 + 1;
                                    chars[length15] = c14;
                                } else {
                                    chars[length9] = Utf8.REPLACEMENT_CHARACTER;
                                    length3 = length9 + 1;
                                }
                                Unit unit8 = Unit.INSTANCE;
                                if (endIndex > index$iv + 1) {
                                    int byte$iv$iv$iv2 = commonToUtf8String[index$iv + 1];
                                    int other$iv$iv$iv$iv2 = 192 & byte$iv$iv$iv2;
                                    if (other$iv$iv$iv$iv2 == 128) {
                                        if (endIndex > index$iv + 2) {
                                            int byte$iv$iv$iv3 = commonToUtf8String[index$iv + 2];
                                            int other$iv$iv$iv$iv3 = 192 & byte$iv$iv$iv3;
                                            if (other$iv$iv$iv$iv3 == 128) {
                                                i = 3;
                                            }
                                        }
                                        i = 2;
                                    }
                                }
                                i = 1;
                            } else {
                                byte b0$iv$iv3 = commonToUtf8String[index$iv];
                                byte b1$iv$iv4 = commonToUtf8String[index$iv + 1];
                                if ((b1$iv$iv4 & 192) == 128) {
                                    byte b2$iv$iv2 = commonToUtf8String[index$iv + 2];
                                    if ((b2$iv$iv2 & 192) == 128) {
                                        int length16 = index$iv + 3;
                                        byte b3$iv$iv = commonToUtf8String[length16];
                                        if ((b3$iv$iv & 192) == 128) {
                                            int codePoint$iv$iv3 = (((3678080 ^ b3$iv$iv) ^ (b2$iv$iv2 << 6)) ^ (b1$iv$iv4 << Ascii.f159FF)) ^ (b0$iv$iv3 << Ascii.DC2);
                                            if (codePoint$iv$iv3 > 1114111) {
                                                if (65533 != 65533) {
                                                    char c15 = (char) ((Utf8.REPLACEMENT_CODE_POINT >>> 10) + Utf8.HIGH_SURROGATE_HEADER);
                                                    int length17 = length9 + 1;
                                                    chars[length9] = c15;
                                                    char c16 = (char) ((65533 & 1023) + Utf8.LOG_SURROGATE_HEADER);
                                                    length2 = length17 + 1;
                                                    chars[length17] = c16;
                                                    Unit unit9 = Unit.INSTANCE;
                                                    i = 4;
                                                    length3 = length2;
                                                } else {
                                                    length = length9 + 1;
                                                    chars[length9] = Utf8.REPLACEMENT_CHARACTER;
                                                    length2 = length;
                                                    Unit unit92 = Unit.INSTANCE;
                                                    i = 4;
                                                    length3 = length2;
                                                }
                                            } else if (55296 <= codePoint$iv$iv3 && 57343 >= codePoint$iv$iv3) {
                                                if (65533 != 65533) {
                                                    char c17 = (char) ((Utf8.REPLACEMENT_CODE_POINT >>> 10) + Utf8.HIGH_SURROGATE_HEADER);
                                                    int length18 = length9 + 1;
                                                    chars[length9] = c17;
                                                    char c18 = (char) ((65533 & 1023) + Utf8.LOG_SURROGATE_HEADER);
                                                    length2 = length18 + 1;
                                                    chars[length18] = c18;
                                                    Unit unit922 = Unit.INSTANCE;
                                                    i = 4;
                                                    length3 = length2;
                                                } else {
                                                    length = length9 + 1;
                                                    chars[length9] = Utf8.REPLACEMENT_CHARACTER;
                                                    length2 = length;
                                                    Unit unit9222 = Unit.INSTANCE;
                                                    i = 4;
                                                    length3 = length2;
                                                }
                                            } else if (codePoint$iv$iv3 < 65536) {
                                                if (65533 != 65533) {
                                                    char c19 = (char) ((Utf8.REPLACEMENT_CODE_POINT >>> 10) + Utf8.HIGH_SURROGATE_HEADER);
                                                    int length19 = length9 + 1;
                                                    chars[length9] = c19;
                                                    char c20 = (char) ((65533 & 1023) + Utf8.LOG_SURROGATE_HEADER);
                                                    length2 = length19 + 1;
                                                    chars[length19] = c20;
                                                    Unit unit92222 = Unit.INSTANCE;
                                                    i = 4;
                                                    length3 = length2;
                                                } else {
                                                    length = length9 + 1;
                                                    chars[length9] = Utf8.REPLACEMENT_CHARACTER;
                                                    length2 = length;
                                                    Unit unit922222 = Unit.INSTANCE;
                                                    i = 4;
                                                    length3 = length2;
                                                }
                                            } else if (codePoint$iv$iv3 != 65533) {
                                                char c21 = (char) ((codePoint$iv$iv3 >>> 10) + Utf8.HIGH_SURROGATE_HEADER);
                                                int length20 = length9 + 1;
                                                chars[length9] = c21;
                                                char c22 = (char) ((codePoint$iv$iv3 & 1023) + Utf8.LOG_SURROGATE_HEADER);
                                                length2 = length20 + 1;
                                                chars[length20] = c22;
                                                Unit unit9222222 = Unit.INSTANCE;
                                                i = 4;
                                                length3 = length2;
                                            } else {
                                                length = length9 + 1;
                                                chars[length9] = Utf8.REPLACEMENT_CHARACTER;
                                                length2 = length;
                                                Unit unit92222222 = Unit.INSTANCE;
                                                i = 4;
                                                length3 = length2;
                                            }
                                        } else {
                                            if (65533 != 65533) {
                                                char c23 = (char) ((Utf8.REPLACEMENT_CODE_POINT >>> 10) + Utf8.HIGH_SURROGATE_HEADER);
                                                int length21 = length9 + 1;
                                                chars[length9] = c23;
                                                char c24 = (char) ((65533 & 1023) + Utf8.LOG_SURROGATE_HEADER);
                                                length4 = length21 + 1;
                                                chars[length21] = c24;
                                            } else {
                                                chars[length9] = Utf8.REPLACEMENT_CHARACTER;
                                                length4 = length9 + 1;
                                            }
                                            Unit unit10 = Unit.INSTANCE;
                                            length3 = length4;
                                            i = 3;
                                        }
                                    } else {
                                        if (65533 != 65533) {
                                            char c25 = (char) ((Utf8.REPLACEMENT_CODE_POINT >>> 10) + Utf8.HIGH_SURROGATE_HEADER);
                                            int length22 = length9 + 1;
                                            chars[length9] = c25;
                                            char c26 = (char) ((65533 & 1023) + Utf8.LOG_SURROGATE_HEADER);
                                            chars[length22] = c26;
                                            length3 = length22 + 1;
                                        } else {
                                            chars[length9] = Utf8.REPLACEMENT_CHARACTER;
                                            length3 = length9 + 1;
                                        }
                                        Unit unit11 = Unit.INSTANCE;
                                        i = 2;
                                    }
                                } else {
                                    if (65533 != 65533) {
                                        char c27 = (char) ((Utf8.REPLACEMENT_CODE_POINT >>> 10) + Utf8.HIGH_SURROGATE_HEADER);
                                        int length23 = length9 + 1;
                                        chars[length9] = c27;
                                        char c28 = (char) ((65533 & 1023) + Utf8.LOG_SURROGATE_HEADER);
                                        length3 = length23 + 1;
                                        chars[length23] = c28;
                                    } else {
                                        chars[length9] = Utf8.REPLACEMENT_CHARACTER;
                                        length3 = length9 + 1;
                                    }
                                    Unit unit12 = Unit.INSTANCE;
                                    i = 1;
                                }
                            }
                            index$iv += i;
                            length9 = length3;
                        } else {
                            chars[length9] = Utf8.REPLACEMENT_CHARACTER;
                            index$iv++;
                            length9++;
                        }
                    }
                }
            }
            $i$f$processUtf16Chars2 = $i$f$processUtf16Chars;
        }
        return new String(chars, 0, length9);
    }

    public static final byte[] commonAsUtf8ToByteArray(String commonAsUtf8ToByteArray) {
        char charAt;
        Intrinsics.checkNotNullParameter(commonAsUtf8ToByteArray, "$this$commonAsUtf8ToByteArray");
        byte[] bytes = new byte[commonAsUtf8ToByteArray.length() * 4];
        int length = commonAsUtf8ToByteArray.length();
        for (int index = 0; index < length; index++) {
            char b0 = commonAsUtf8ToByteArray.charAt(index);
            if (Intrinsics.compare((int) b0, 128) >= 0) {
                int size = index;
                int endIndex$iv = commonAsUtf8ToByteArray.length();
                int index$iv = index;
                while (index$iv < endIndex$iv) {
                    char c$iv = commonAsUtf8ToByteArray.charAt(index$iv);
                    if (Intrinsics.compare((int) c$iv, 128) < 0) {
                        byte c = (byte) c$iv;
                        int size2 = size + 1;
                        bytes[size] = c;
                        index$iv++;
                        while (index$iv < endIndex$iv && Intrinsics.compare((int) commonAsUtf8ToByteArray.charAt(index$iv), 128) < 0) {
                            int index$iv2 = index$iv + 1;
                            byte c2 = (byte) commonAsUtf8ToByteArray.charAt(index$iv);
                            bytes[size2] = c2;
                            index$iv = index$iv2;
                            size2++;
                        }
                        size = size2;
                    } else if (Intrinsics.compare((int) c$iv, 2048) < 0) {
                        byte c3 = (byte) ((c$iv >> 6) | 192);
                        int size3 = size + 1;
                        bytes[size] = c3;
                        byte c4 = (byte) ((c$iv & '?') | 128);
                        bytes[size3] = c4;
                        index$iv++;
                        size = size3 + 1;
                    } else if (55296 > c$iv || 57343 < c$iv) {
                        byte c5 = (byte) ((c$iv >> '\f') | 224);
                        int size4 = size + 1;
                        bytes[size] = c5;
                        byte c6 = (byte) (((c$iv >> 6) & 63) | 128);
                        int size5 = size4 + 1;
                        bytes[size4] = c6;
                        byte c7 = (byte) ((c$iv & '?') | 128);
                        bytes[size5] = c7;
                        index$iv++;
                        size = size5 + 1;
                    } else if (Intrinsics.compare((int) c$iv, 56319) > 0 || endIndex$iv <= index$iv + 1 || 56320 > (charAt = commonAsUtf8ToByteArray.charAt(index$iv + 1)) || 57343 < charAt) {
                        bytes[size] = Utf8.REPLACEMENT_BYTE;
                        index$iv++;
                        size++;
                    } else {
                        int codePoint$iv = ((c$iv << '\n') + commonAsUtf8ToByteArray.charAt(index$iv + 1)) - 56613888;
                        byte c8 = (byte) ((codePoint$iv >> 18) | 240);
                        int size6 = size + 1;
                        bytes[size] = c8;
                        byte c9 = (byte) (((codePoint$iv >> 12) & 63) | 128);
                        int size7 = size6 + 1;
                        bytes[size6] = c9;
                        byte c10 = (byte) (((codePoint$iv >> 6) & 63) | 128);
                        int size8 = size7 + 1;
                        bytes[size7] = c10;
                        byte c11 = (byte) ((codePoint$iv & 63) | 128);
                        bytes[size8] = c11;
                        index$iv += 2;
                        size = size8 + 1;
                    }
                }
                byte[] copyOf = Arrays.copyOf(bytes, size);
                Intrinsics.checkNotNullExpressionValue(copyOf, "java.util.Arrays.copyOf(this, newSize)");
                return copyOf;
            }
            bytes[index] = (byte) b0;
        }
        byte[] copyOf2 = Arrays.copyOf(bytes, commonAsUtf8ToByteArray.length());
        Intrinsics.checkNotNullExpressionValue(copyOf2, "java.util.Arrays.copyOf(this, newSize)");
        return copyOf2;
    }
}
