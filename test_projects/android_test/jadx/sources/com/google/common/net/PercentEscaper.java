package com.google.common.net;

import com.google.common.base.Preconditions;
import com.google.common.escape.UnicodeEscaper;

/* loaded from: classes.dex */
public final class PercentEscaper extends UnicodeEscaper {
    private static final char[] PLUS_SIGN = {'+'};
    private static final char[] UPPER_HEX_DIGITS = "0123456789ABCDEF".toCharArray();
    private final boolean plusForSpace;
    private final boolean[] safeOctets;

    public PercentEscaper(String safeChars, boolean plusForSpace) {
        Preconditions.checkNotNull(safeChars);
        if (safeChars.matches(".*[0-9A-Za-z].*")) {
            throw new IllegalArgumentException("Alphanumeric characters are always 'safe' and should not be explicitly specified");
        }
        String safeChars2 = safeChars + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        if (plusForSpace && safeChars2.contains(" ")) {
            throw new IllegalArgumentException("plusForSpace cannot be specified when space is a 'safe' character");
        }
        this.plusForSpace = plusForSpace;
        this.safeOctets = createSafeOctets(safeChars2);
    }

    private static boolean[] createSafeOctets(String safeChars) {
        int maxChar = -1;
        char[] safeCharArray = safeChars.toCharArray();
        for (char c : safeCharArray) {
            maxChar = Math.max((int) c, maxChar);
        }
        boolean[] octets = new boolean[maxChar + 1];
        for (char c2 : safeCharArray) {
            octets[c2] = true;
        }
        return octets;
    }

    @Override // com.google.common.escape.UnicodeEscaper
    protected int nextEscapeIndex(CharSequence csq, int index, int end) {
        Preconditions.checkNotNull(csq);
        while (index < end) {
            char c = csq.charAt(index);
            boolean[] zArr = this.safeOctets;
            if (c >= zArr.length || !zArr[c]) {
                break;
            }
            index++;
        }
        return index;
    }

    @Override // com.google.common.escape.UnicodeEscaper, com.google.common.escape.Escaper
    public String escape(String s) {
        Preconditions.checkNotNull(s);
        int slen = s.length();
        for (int index = 0; index < slen; index++) {
            char c = s.charAt(index);
            boolean[] zArr = this.safeOctets;
            if (c >= zArr.length || !zArr[c]) {
                return escapeSlow(s, index);
            }
        }
        return s;
    }

    @Override // com.google.common.escape.UnicodeEscaper
    protected char[] escape(int cp) {
        boolean[] zArr = this.safeOctets;
        if (cp < zArr.length && zArr[cp]) {
            return null;
        }
        if (cp == 32 && this.plusForSpace) {
            return PLUS_SIGN;
        }
        if (cp <= 127) {
            char[] cArr = UPPER_HEX_DIGITS;
            char[] dest = {'%', cArr[cp >>> 4], cArr[cp & 15]};
            return dest;
        }
        if (cp <= 2047) {
            char[] cArr2 = UPPER_HEX_DIGITS;
            char[] dest2 = {'%', cArr2[(cp >>> 4) | 12], cArr2[cp & 15], '%', cArr2[(cp & 3) | 8], cArr2[cp & 15]};
            int cp2 = cp >>> 4;
            int cp3 = cp2 >>> 2;
            return dest2;
        }
        if (cp <= 65535) {
            char[] cArr3 = UPPER_HEX_DIGITS;
            char[] dest3 = {'%', 'E', cArr3[cp >>> 2], '%', cArr3[(cp & 3) | 8], cArr3[cp & 15], '%', cArr3[(cp & 3) | 8], cArr3[cp & 15]};
            int cp4 = cp >>> 4;
            int cp5 = cp4 >>> 2;
            int cp6 = cp5 >>> 4;
            return dest3;
        }
        if (cp <= 1114111) {
            char[] cArr4 = UPPER_HEX_DIGITS;
            char[] dest4 = {'%', 'F', cArr4[(cp >>> 2) & 7], '%', cArr4[(cp & 3) | 8], cArr4[cp & 15], '%', cArr4[(cp & 3) | 8], cArr4[cp & 15], '%', cArr4[(cp & 3) | 8], cArr4[cp & 15]};
            int cp7 = cp >>> 4;
            int cp8 = cp7 >>> 2;
            int cp9 = cp8 >>> 4;
            int cp10 = cp9 >>> 2;
            int cp11 = cp10 >>> 4;
            return dest4;
        }
        throw new IllegalArgumentException("Invalid unicode character value " + cp);
    }
}
