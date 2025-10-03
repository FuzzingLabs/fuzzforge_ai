package org.checkerframework.checker.formatter;

import java.util.ArrayList;
import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatException;
import java.util.MissingFormatArgumentException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kotlin.text.Typography;
import org.checkerframework.checker.formatter.qual.ConversionCategory;
import org.checkerframework.checker.formatter.qual.ReturnsFormat;

/* loaded from: classes11.dex */
public class FormatUtil {
    private static final String formatSpecifier = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
    private static Pattern fsPattern = Pattern.compile(formatSpecifier);

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes11.dex */
    public static class Conversion {
        private final ConversionCategory cath;
        private final int index;

        public Conversion(char c, int index) {
            this.index = index;
            this.cath = ConversionCategory.fromConversionChar(c);
        }

        int index() {
            return this.index;
        }

        ConversionCategory category() {
            return this.cath;
        }
    }

    @ReturnsFormat
    public static String asFormat(String format, ConversionCategory... cc) throws IllegalFormatException {
        ConversionCategory[] fcc = formatParameterCategories(format);
        if (fcc.length != cc.length) {
            throw new ExcessiveOrMissingFormatArgumentException(cc.length, fcc.length);
        }
        for (int i = 0; i < cc.length; i++) {
            if (cc[i] != fcc[i]) {
                throw new IllegalFormatConversionCategoryException(cc[i], fcc[i]);
            }
        }
        return format;
    }

    public static void tryFormatSatisfiability(String format) throws IllegalFormatException {
        String.format(format, null);
    }

    /* JADX WARN: Removed duplicated region for block: B:12:0x0042  */
    /* JADX WARN: Removed duplicated region for block: B:9:0x0037  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static org.checkerframework.checker.formatter.qual.ConversionCategory[] formatParameterCategories(java.lang.String r12) throws java.util.IllegalFormatException {
        /*
            tryFormatSatisfiability(r12)
            r0 = -1
            r1 = -1
            r2 = -1
            org.checkerframework.checker.formatter.FormatUtil$Conversion[] r3 = parse(r12)
            java.util.HashMap r4 = new java.util.HashMap
            r4.<init>()
            int r5 = r3.length
            r6 = 0
        L11:
            if (r6 >= r5) goto L52
            r7 = r3[r6]
            int r8 = r7.index()
            switch(r8) {
                case -1: goto L23;
                case 0: goto L1f;
                default: goto L1c;
            }
        L1c:
            int r0 = r8 + (-1)
            goto L24
        L1f:
            int r1 = r1 + 1
            r0 = r1
            goto L24
        L23:
        L24:
            int r2 = java.lang.Math.max(r2, r0)
            java.lang.Integer r9 = java.lang.Integer.valueOf(r0)
            java.lang.Integer r10 = java.lang.Integer.valueOf(r0)
            boolean r10 = r4.containsKey(r10)
            if (r10 == 0) goto L42
            java.lang.Integer r10 = java.lang.Integer.valueOf(r0)
            java.lang.Object r10 = r4.get(r10)
            org.checkerframework.checker.formatter.qual.ConversionCategory r10 = (org.checkerframework.checker.formatter.qual.ConversionCategory) r10
            goto L44
        L42:
            org.checkerframework.checker.formatter.qual.ConversionCategory r10 = org.checkerframework.checker.formatter.qual.ConversionCategory.UNUSED
        L44:
            org.checkerframework.checker.formatter.qual.ConversionCategory r11 = r7.category()
            org.checkerframework.checker.formatter.qual.ConversionCategory r10 = org.checkerframework.checker.formatter.qual.ConversionCategory.intersect(r10, r11)
            r4.put(r9, r10)
            int r6 = r6 + 1
            goto L11
        L52:
            int r5 = r2 + 1
            org.checkerframework.checker.formatter.qual.ConversionCategory[] r5 = new org.checkerframework.checker.formatter.qual.ConversionCategory[r5]
            r6 = 0
        L57:
            if (r6 > r2) goto L75
            java.lang.Integer r7 = java.lang.Integer.valueOf(r6)
            boolean r7 = r4.containsKey(r7)
            if (r7 == 0) goto L6e
            java.lang.Integer r7 = java.lang.Integer.valueOf(r6)
            java.lang.Object r7 = r4.get(r7)
            org.checkerframework.checker.formatter.qual.ConversionCategory r7 = (org.checkerframework.checker.formatter.qual.ConversionCategory) r7
            goto L70
        L6e:
            org.checkerframework.checker.formatter.qual.ConversionCategory r7 = org.checkerframework.checker.formatter.qual.ConversionCategory.UNUSED
        L70:
            r5[r6] = r7
            int r6 = r6 + 1
            goto L57
        L75:
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: org.checkerframework.checker.formatter.FormatUtil.formatParameterCategories(java.lang.String):org.checkerframework.checker.formatter.qual.ConversionCategory[]");
    }

    private static int indexFromFormat(Matcher m) {
        String s = m.group(1);
        if (s != null) {
            int index = Integer.parseInt(s.substring(0, s.length() - 1));
            return index;
        }
        if (m.group(2) != null && m.group(2).contains(String.valueOf(Typography.less))) {
            return -1;
        }
        return 0;
    }

    private static char conversionCharFromFormat(Matcher m) {
        String dt = m.group(5);
        if (dt == null) {
            return m.group(6).charAt(0);
        }
        return dt.charAt(0);
    }

    private static Conversion[] parse(String format) {
        ArrayList<Conversion> cs = new ArrayList<>();
        Matcher m = fsPattern.matcher(format);
        while (m.find()) {
            char c = conversionCharFromFormat(m);
            switch (c) {
                case '%':
                case 'n':
                    break;
                default:
                    cs.add(new Conversion(c, indexFromFormat(m)));
                    break;
            }
        }
        return (Conversion[]) cs.toArray(new Conversion[cs.size()]);
    }

    /* loaded from: classes11.dex */
    public static class ExcessiveOrMissingFormatArgumentException extends MissingFormatArgumentException {
        private static final long serialVersionUID = 17000126;
        private final int expected;
        private final int found;

        public ExcessiveOrMissingFormatArgumentException(int expected, int found) {
            super("-");
            this.expected = expected;
            this.found = found;
        }

        public int getExpected() {
            return this.expected;
        }

        public int getFound() {
            return this.found;
        }

        @Override // java.util.MissingFormatArgumentException, java.lang.Throwable
        public String getMessage() {
            return String.format("Expected %d arguments but found %d.", Integer.valueOf(this.expected), Integer.valueOf(this.found));
        }
    }

    /* loaded from: classes11.dex */
    public static class IllegalFormatConversionCategoryException extends IllegalFormatConversionException {
        private static final long serialVersionUID = 17000126;
        private final ConversionCategory expected;
        private final ConversionCategory found;

        public IllegalFormatConversionCategoryException(ConversionCategory expected, ConversionCategory found) {
            super(expected.chars.length() == 0 ? '-' : expected.chars.charAt(0), found.types == null ? Object.class : found.types[0]);
            this.expected = expected;
            this.found = found;
        }

        public ConversionCategory getExpected() {
            return this.expected;
        }

        public ConversionCategory getFound() {
            return this.found;
        }

        @Override // java.util.IllegalFormatConversionException, java.lang.Throwable
        public String getMessage() {
            return String.format("Expected category %s but found %s.", this.expected, this.found);
        }
    }
}
