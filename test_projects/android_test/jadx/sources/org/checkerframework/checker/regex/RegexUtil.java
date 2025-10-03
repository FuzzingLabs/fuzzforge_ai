package org.checkerframework.checker.regex;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.checkerframework.checker.regex.qual.Regex;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.framework.qual.EnsuresQualifierIf;

/* loaded from: classes11.dex */
public final class RegexUtil {
    private RegexUtil() {
        throw new Error("do not instantiate");
    }

    public static class CheckedPatternSyntaxException extends Exception {
        private static final long serialVersionUID = 6266881831979001480L;
        private final PatternSyntaxException pse;

        public CheckedPatternSyntaxException(PatternSyntaxException pse) {
            this.pse = pse;
        }

        public CheckedPatternSyntaxException(String desc, String regex, int index) {
            this(new PatternSyntaxException(desc, regex, index));
        }

        public String getDescription() {
            return this.pse.getDescription();
        }

        public int getIndex() {
            return this.pse.getIndex();
        }

        @Override // java.lang.Throwable
        @Pure
        public String getMessage() {
            return this.pse.getMessage();
        }

        public String getPattern() {
            return this.pse.getPattern();
        }
    }

    @EnsuresQualifierIf(expression = {"#1"}, qualifier = Regex.class, result = true)
    @Pure
    public static boolean isRegex(String s) {
        return isRegex(s, 0);
    }

    @EnsuresQualifierIf(expression = {"#1"}, qualifier = Regex.class, result = true)
    @Pure
    public static boolean isRegex(String s, int groups) {
        try {
            Pattern p = Pattern.compile(s);
            return getGroupCount(p) >= groups;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    @EnsuresQualifierIf(expression = {"#1"}, qualifier = Regex.class, result = true)
    @Pure
    public static boolean isRegex(char c) {
        return isRegex(Character.toString(c));
    }

    @SideEffectFree
    public static String regexError(String s) {
        return regexError(s, 0);
    }

    @SideEffectFree
    public static String regexError(String s, int groups) {
        try {
            Pattern p = Pattern.compile(s);
            int actualGroups = getGroupCount(p);
            if (actualGroups < groups) {
                return regexErrorMessage(s, groups, actualGroups);
            }
            return null;
        } catch (PatternSyntaxException e) {
            return e.getMessage();
        }
    }

    @SideEffectFree
    public static PatternSyntaxException regexException(String s) {
        return regexException(s, 0);
    }

    @SideEffectFree
    public static PatternSyntaxException regexException(String s, int groups) {
        try {
            Pattern p = Pattern.compile(s);
            int actualGroups = getGroupCount(p);
            if (actualGroups < groups) {
                return new PatternSyntaxException(regexErrorMessage(s, groups, actualGroups), s, -1);
            }
            return null;
        } catch (PatternSyntaxException pse) {
            return pse;
        }
    }

    @SideEffectFree
    public static String asRegex(String s) {
        return asRegex(s, 0);
    }

    @SideEffectFree
    public static String asRegex(String s, int groups) {
        try {
            Pattern p = Pattern.compile(s);
            int actualGroups = getGroupCount(p);
            if (actualGroups < groups) {
                throw new Error(regexErrorMessage(s, groups, actualGroups));
            }
            return s;
        } catch (PatternSyntaxException e) {
            throw new Error(e);
        }
    }

    @SideEffectFree
    private static String regexErrorMessage(String s, int expectedGroups, int actualGroups) {
        return "regex \"" + s + "\" has " + actualGroups + " groups, but " + expectedGroups + " groups are needed.";
    }

    @Pure
    private static int getGroupCount(Pattern p) {
        return p.matcher("").groupCount();
    }
}
