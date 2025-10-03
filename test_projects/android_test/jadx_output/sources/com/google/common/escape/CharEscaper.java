package com.google.common.escape;

import com.google.common.base.Preconditions;

/* loaded from: classes.dex */
public abstract class CharEscaper extends Escaper {
    private static final int DEST_PAD_MULTIPLIER = 2;

    /* JADX INFO: Access modifiers changed from: protected */
    public abstract char[] escape(char c);

    @Override // com.google.common.escape.Escaper
    public String escape(String string) {
        Preconditions.checkNotNull(string);
        int length = string.length();
        for (int index = 0; index < length; index++) {
            if (escape(string.charAt(index)) != null) {
                return escapeSlow(string, index);
            }
        }
        return string;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final String escapeSlow(String s, int index) {
        int slen = s.length();
        char[] dest = Platform.charBufferFromThreadLocal();
        int destSize = dest.length;
        int destIndex = 0;
        int lastEscape = 0;
        while (index < slen) {
            char[] r = escape(s.charAt(index));
            if (r != null) {
                int rlen = r.length;
                int charsSkipped = index - lastEscape;
                int sizeNeeded = destIndex + charsSkipped + rlen;
                if (destSize < sizeNeeded) {
                    destSize = sizeNeeded + ((slen - index) * 2);
                    dest = growBuffer(dest, destIndex, destSize);
                }
                if (charsSkipped > 0) {
                    s.getChars(lastEscape, index, dest, destIndex);
                    destIndex += charsSkipped;
                }
                if (rlen > 0) {
                    System.arraycopy(r, 0, dest, destIndex, rlen);
                    destIndex += rlen;
                }
                lastEscape = index + 1;
            }
            index++;
        }
        int charsLeft = slen - lastEscape;
        if (charsLeft > 0) {
            int sizeNeeded2 = destIndex + charsLeft;
            if (destSize < sizeNeeded2) {
                dest = growBuffer(dest, destIndex, sizeNeeded2);
            }
            s.getChars(lastEscape, slen, dest, destIndex);
            destIndex = sizeNeeded2;
        }
        return new String(dest, 0, destIndex);
    }

    private static char[] growBuffer(char[] dest, int index, int size) {
        if (size < 0) {
            throw new AssertionError("Cannot increase internal buffer any further");
        }
        char[] copy = new char[size];
        if (index > 0) {
            System.arraycopy(dest, 0, copy, 0, index);
        }
        return copy;
    }
}
