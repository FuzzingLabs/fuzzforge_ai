package org.checkerframework.checker.formatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatException;
import java.util.Map;
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

    private static class Conversion {
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

    public static ConversionCategory[] formatParameterCategories(String format) throws IllegalFormatException {
        tryFormatSatisfiability(format);
        int last = -1;
        int lasto = -1;
        int maxindex = -1;
        Conversion[] cs = parse(format);
        Map<Integer, ConversionCategory> conv = new HashMap<>();
        for (Conversion c : cs) {
            int index = c.index();
            switch (index) {
                case -1:
                    break;
                case 0:
                    lasto++;
                    last = lasto;
                    break;
                default:
                    last = index - 1;
                    break;
            }
            maxindex = Math.max(maxindex, last);
            conv.put(Integer.valueOf(last), ConversionCategory.intersect(conv.containsKey(Integer.valueOf(last)) ? conv.get(Integer.valueOf(last)) : ConversionCategory.UNUSED, c.category()));
        }
        ConversionCategory[] res = new ConversionCategory[maxindex + 1];
        for (int i = 0; i <= maxindex; i++) {
            res[i] = conv.containsKey(Integer.valueOf(i)) ? conv.get(Integer.valueOf(i)) : ConversionCategory.UNUSED;
        }
        return res;
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
