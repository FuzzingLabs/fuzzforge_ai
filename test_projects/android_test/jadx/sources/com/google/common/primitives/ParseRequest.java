package com.google.common.primitives;

/* loaded from: classes.dex */
final class ParseRequest {
    final int radix;
    final String rawValue;

    private ParseRequest(String rawValue, int radix) {
        this.rawValue = rawValue;
        this.radix = radix;
    }

    static ParseRequest fromString(String stringValue) {
        String rawValue;
        int radix;
        if (stringValue.length() == 0) {
            throw new NumberFormatException("empty string");
        }
        char firstChar = stringValue.charAt(0);
        if (stringValue.startsWith("0x") || stringValue.startsWith("0X")) {
            rawValue = stringValue.substring(2);
            radix = 16;
        } else if (firstChar == '#') {
            rawValue = stringValue.substring(1);
            radix = 16;
        } else if (firstChar == '0' && stringValue.length() > 1) {
            rawValue = stringValue.substring(1);
            radix = 8;
        } else {
            rawValue = stringValue;
            radix = 10;
        }
        return new ParseRequest(rawValue, radix);
    }
}
