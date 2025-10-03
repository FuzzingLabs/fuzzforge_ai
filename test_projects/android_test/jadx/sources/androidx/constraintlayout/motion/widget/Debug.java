package androidx.constraintlayout.motion.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import app.beetlebug.BuildConfig;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.CharBuffer;

/* loaded from: classes.dex */
public class Debug {
    public static void logStack(String tag, String msg, int n) {
        StackTraceElement[] st = new Throwable().getStackTrace();
        String s = " ";
        int n2 = Math.min(n, st.length - 1);
        for (int i = 1; i <= n2; i++) {
            StackTraceElement stackTraceElement = st[i];
            String fileName = st[i].getFileName();
            int lineNumber = st[i].getLineNumber();
            String methodName = st[i].getMethodName();
            StringBuilder sb = new StringBuilder(String.valueOf(fileName).length() + 16 + String.valueOf(methodName).length());
            sb.append(".(");
            sb.append(fileName);
            sb.append(":");
            sb.append(lineNumber);
            sb.append(") ");
            sb.append(methodName);
            String stack = sb.toString();
            s = String.valueOf(s).concat(" ");
            StringBuilder sb2 = new StringBuilder(String.valueOf(msg).length() + String.valueOf(s).length() + String.valueOf(stack).length() + String.valueOf(s).length());
            sb2.append(msg);
            sb2.append(s);
            sb2.append(stack);
            sb2.append(s);
            Log.v(tag, sb2.toString());
        }
    }

    public static void printStack(String msg, int n) {
        StackTraceElement[] st = new Throwable().getStackTrace();
        String s = " ";
        int n2 = Math.min(n, st.length - 1);
        for (int i = 1; i <= n2; i++) {
            StackTraceElement stackTraceElement = st[i];
            String fileName = st[i].getFileName();
            int lineNumber = st[i].getLineNumber();
            StringBuilder sb = new StringBuilder(String.valueOf(fileName).length() + 16);
            sb.append(".(");
            sb.append(fileName);
            sb.append(":");
            sb.append(lineNumber);
            sb.append(") ");
            String stack = sb.toString();
            s = String.valueOf(s).concat(" ");
            PrintStream printStream = System.out;
            StringBuilder sb2 = new StringBuilder(String.valueOf(msg).length() + String.valueOf(s).length() + String.valueOf(stack).length() + String.valueOf(s).length());
            sb2.append(msg);
            sb2.append(s);
            sb2.append(stack);
            sb2.append(s);
            printStream.println(sb2.toString());
        }
    }

    public static String getName(View view) {
        try {
            Context context = view.getContext();
            return context.getResources().getResourceEntryName(view.getId());
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    public static void dumpPoc(Object obj) {
        StackTraceElement s = new Throwable().getStackTrace()[1];
        String fileName = s.getFileName();
        int lineNumber = s.getLineNumber();
        StringBuilder sb = new StringBuilder(String.valueOf(fileName).length() + 15);
        sb.append(".(");
        sb.append(fileName);
        sb.append(":");
        sb.append(lineNumber);
        sb.append(")");
        String loc = sb.toString();
        Class c = obj.getClass();
        PrintStream printStream = System.out;
        String name = c.getName();
        StringBuilder sb2 = new StringBuilder(String.valueOf(loc).length() + 35 + String.valueOf(name).length());
        sb2.append(loc);
        sb2.append("------------- ");
        sb2.append(name);
        sb2.append(" --------------------");
        printStream.println(sb2.toString());
        Field[] declaredFields = c.getFields();
        for (Field declaredField : declaredFields) {
            try {
                Object value = declaredField.get(obj);
                if (declaredField.getName().startsWith("layout_constraint") && ((!(value instanceof Integer) || !value.toString().equals("-1")) && ((!(value instanceof Integer) || !value.toString().equals("0")) && ((!(value instanceof Float) || !value.toString().equals(BuildConfig.VERSION_NAME)) && (!(value instanceof Float) || !value.toString().equals("0.5")))))) {
                    PrintStream printStream2 = System.out;
                    String name2 = declaredField.getName();
                    String valueOf = String.valueOf(value);
                    StringBuilder sb3 = new StringBuilder(String.valueOf(loc).length() + 5 + String.valueOf(name2).length() + String.valueOf(valueOf).length());
                    sb3.append(loc);
                    sb3.append("    ");
                    sb3.append(name2);
                    sb3.append(" ");
                    sb3.append(valueOf);
                    printStream2.println(sb3.toString());
                }
            } catch (IllegalAccessException e) {
            }
        }
        PrintStream printStream3 = System.out;
        String simpleName = c.getSimpleName();
        StringBuilder sb4 = new StringBuilder(String.valueOf(loc).length() + 35 + String.valueOf(simpleName).length());
        sb4.append(loc);
        sb4.append("------------- ");
        sb4.append(simpleName);
        sb4.append(" --------------------");
        printStream3.println(sb4.toString());
    }

    public static String getName(Context context, int id) {
        if (id != -1) {
            try {
                return context.getResources().getResourceEntryName(id);
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder(12);
                sb.append("?");
                sb.append(id);
                return sb.toString();
            }
        }
        return "UNKNOWN";
    }

    public static String getName(Context context, int[] id) {
        String tmp;
        try {
            int length = id.length;
            StringBuilder sb = new StringBuilder(12);
            sb.append(length);
            sb.append("[");
            String str = sb.toString();
            int i = 0;
            while (i < id.length) {
                String valueOf = String.valueOf(str);
                String valueOf2 = String.valueOf(i == 0 ? "" : " ");
                String str2 = valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf);
                try {
                    tmp = context.getResources().getResourceEntryName(id[i]);
                } catch (Resources.NotFoundException e) {
                    int i2 = id[i];
                    StringBuilder sb2 = new StringBuilder(14);
                    sb2.append("? ");
                    sb2.append(i2);
                    sb2.append(" ");
                    tmp = sb2.toString();
                }
                String valueOf3 = String.valueOf(str2);
                String valueOf4 = String.valueOf(tmp);
                str = valueOf4.length() != 0 ? valueOf3.concat(valueOf4) : new String(valueOf3);
                i++;
            }
            return String.valueOf(str).concat("]");
        } catch (Exception ex) {
            Log.v("DEBUG", ex.toString());
            return "UNKNOWN";
        }
    }

    public static String getState(MotionLayout layout, int stateId) {
        return getState(layout, stateId, -1);
    }

    public static String getState(MotionLayout layout, int stateId, int len) {
        int n;
        if (stateId == -1) {
            return "UNDEFINED";
        }
        Context context = layout.getContext();
        String str = context.getResources().getResourceEntryName(stateId);
        if (len != -1) {
            if (str.length() > len) {
                str = str.replaceAll("([^_])[aeiou]+", "$1");
            }
            if (str.length() > len && (n = str.replaceAll("[^_]", "").length()) > 0) {
                int extra = (str.length() - len) / n;
                String reg = String.valueOf(CharBuffer.allocate(extra).toString().replace((char) 0, '.')).concat("_");
                return str.replaceAll(reg, "_");
            }
            return str;
        }
        return str;
    }

    public static String getActionType(MotionEvent event) {
        int type = event.getAction();
        Field[] fields = MotionEvent.class.getFields();
        for (Field field : fields) {
            try {
                if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(Integer.TYPE) && field.getInt(null) == type) {
                    return field.getName();
                }
            } catch (IllegalAccessException e) {
            }
        }
        return "---";
    }

    public static String getLocation() {
        StackTraceElement s = new Throwable().getStackTrace()[1];
        String fileName = s.getFileName();
        int lineNumber = s.getLineNumber();
        StringBuilder sb = new StringBuilder(String.valueOf(fileName).length() + 15);
        sb.append(".(");
        sb.append(fileName);
        sb.append(":");
        sb.append(lineNumber);
        sb.append(")");
        return sb.toString();
    }

    public static String getLoc() {
        StackTraceElement s = new Throwable().getStackTrace()[1];
        String fileName = s.getFileName();
        int lineNumber = s.getLineNumber();
        String methodName = s.getMethodName();
        StringBuilder sb = new StringBuilder(String.valueOf(fileName).length() + 18 + String.valueOf(methodName).length());
        sb.append(".(");
        sb.append(fileName);
        sb.append(":");
        sb.append(lineNumber);
        sb.append(") ");
        sb.append(methodName);
        sb.append("()");
        return sb.toString();
    }

    public static String getLocation2() {
        StackTraceElement s = new Throwable().getStackTrace()[2];
        String fileName = s.getFileName();
        int lineNumber = s.getLineNumber();
        StringBuilder sb = new StringBuilder(String.valueOf(fileName).length() + 15);
        sb.append(".(");
        sb.append(fileName);
        sb.append(":");
        sb.append(lineNumber);
        sb.append(")");
        return sb.toString();
    }

    public static String getCallFrom(int n) {
        StackTraceElement s = new Throwable().getStackTrace()[n + 2];
        String fileName = s.getFileName();
        int lineNumber = s.getLineNumber();
        StringBuilder sb = new StringBuilder(String.valueOf(fileName).length() + 15);
        sb.append(".(");
        sb.append(fileName);
        sb.append(":");
        sb.append(lineNumber);
        sb.append(")");
        return sb.toString();
    }

    public static void dumpLayoutParams(ViewGroup layout, String str) {
        StackTraceElement s = new Throwable().getStackTrace()[1];
        String fileName = s.getFileName();
        int lineNumber = s.getLineNumber();
        StringBuilder sb = new StringBuilder(String.valueOf(fileName).length() + 18 + String.valueOf(str).length());
        sb.append(".(");
        sb.append(fileName);
        sb.append(":");
        sb.append(lineNumber);
        sb.append(") ");
        sb.append(str);
        sb.append("  ");
        String loc = sb.toString();
        int n = layout.getChildCount();
        PrintStream printStream = System.out;
        StringBuilder sb2 = new StringBuilder(String.valueOf(str).length() + 21);
        sb2.append(str);
        sb2.append(" children ");
        sb2.append(n);
        printStream.println(sb2.toString());
        for (int i = 0; i < n; i++) {
            View v = layout.getChildAt(i);
            PrintStream printStream2 = System.out;
            String name = getName(v);
            StringBuilder sb3 = new StringBuilder(String.valueOf(loc).length() + 5 + String.valueOf(name).length());
            sb3.append(loc);
            sb3.append("     ");
            sb3.append(name);
            printStream2.println(sb3.toString());
            ViewGroup.LayoutParams param = v.getLayoutParams();
            Field[] declaredFields = param.getClass().getFields();
            for (Field declaredField : declaredFields) {
                try {
                    Object value = declaredField.get(param);
                    String name2 = declaredField.getName();
                    if (name2.contains("To") && !value.toString().equals("-1")) {
                        PrintStream printStream3 = System.out;
                        String name3 = declaredField.getName();
                        String valueOf = String.valueOf(value);
                        StringBuilder sb4 = new StringBuilder(String.valueOf(loc).length() + 8 + String.valueOf(name3).length() + String.valueOf(valueOf).length());
                        sb4.append(loc);
                        sb4.append("       ");
                        sb4.append(name3);
                        sb4.append(" ");
                        sb4.append(valueOf);
                        printStream3.println(sb4.toString());
                    }
                } catch (IllegalAccessException e) {
                }
            }
        }
    }

    public static void dumpLayoutParams(ViewGroup.LayoutParams param, String str) {
        StackTraceElement s = new Throwable().getStackTrace()[1];
        String fileName = s.getFileName();
        int lineNumber = s.getLineNumber();
        StringBuilder sb = new StringBuilder(String.valueOf(fileName).length() + 18 + String.valueOf(str).length());
        sb.append(".(");
        sb.append(fileName);
        sb.append(":");
        sb.append(lineNumber);
        sb.append(") ");
        sb.append(str);
        sb.append("  ");
        String loc = sb.toString();
        PrintStream printStream = System.out;
        String name = param.getClass().getName();
        StringBuilder sb2 = new StringBuilder(String.valueOf(loc).length() + 28 + String.valueOf(name).length());
        sb2.append(" >>>>>>>>>>>>>>>>>>. dump ");
        sb2.append(loc);
        sb2.append("  ");
        sb2.append(name);
        printStream.println(sb2.toString());
        Field[] declaredFields = param.getClass().getFields();
        for (Field declaredField : declaredFields) {
            try {
                Object value = declaredField.get(param);
                String name2 = declaredField.getName();
                if (name2.contains("To") && !value.toString().equals("-1")) {
                    PrintStream printStream2 = System.out;
                    String valueOf = String.valueOf(value);
                    StringBuilder sb3 = new StringBuilder(String.valueOf(loc).length() + 8 + String.valueOf(name2).length() + String.valueOf(valueOf).length());
                    sb3.append(loc);
                    sb3.append("       ");
                    sb3.append(name2);
                    sb3.append(" ");
                    sb3.append(valueOf);
                    printStream2.println(sb3.toString());
                }
            } catch (IllegalAccessException e) {
            }
        }
        PrintStream printStream3 = System.out;
        String valueOf2 = String.valueOf(loc);
        printStream3.println(valueOf2.length() != 0 ? " <<<<<<<<<<<<<<<<< dump ".concat(valueOf2) : new String(" <<<<<<<<<<<<<<<<< dump "));
    }
}
