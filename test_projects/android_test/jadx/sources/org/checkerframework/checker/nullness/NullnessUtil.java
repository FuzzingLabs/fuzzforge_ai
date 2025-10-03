package org.checkerframework.checker.nullness;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;

/* loaded from: classes11.dex */
public final class NullnessUtil {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    private NullnessUtil() {
        throw new AssertionError("shouldn't be instantiated");
    }

    @EnsuresNonNull({"#1"})
    public static <T> T castNonNull(T ref) {
        if (ref == null) {
            throw new AssertionError("Misuse of castNonNull: called with a null argument");
        }
        return ref;
    }

    @EnsuresNonNull({"#1"})
    public static <T> T[] castNonNullDeep(T[] tArr) {
        return (T[]) castNonNullArray(tArr);
    }

    @EnsuresNonNull({"#1"})
    public static <T> T[][] castNonNullDeep(T[][] tArr) {
        return (T[][]) ((Object[][]) castNonNullArray(tArr));
    }

    @EnsuresNonNull({"#1"})
    public static <T> T[][][] castNonNullDeep(T[][][] tArr) {
        return (T[][][]) ((Object[][][]) castNonNullArray(tArr));
    }

    @EnsuresNonNull({"#1"})
    public static <T> T[][][][] castNonNullDeep(T[][][][] tArr) {
        return (T[][][][]) ((Object[][][][]) castNonNullArray(tArr));
    }

    @EnsuresNonNull({"#1"})
    public static <T> T[][][][][] castNonNullDeep(T[][][][][] tArr) {
        return (T[][][][][]) ((Object[][][][][]) castNonNullArray(tArr));
    }

    private static <T> T[] castNonNullArray(T[] arr) {
        if (arr == null) {
            throw new AssertionError("Misuse of castNonNullArray: called with a null array argument");
        }
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null) {
                throw new AssertionError("Misuse of castNonNull: called with a null array element");
            }
            checkIfArray(arr[i]);
        }
        return arr;
    }

    private static void checkIfArray(Object ref) {
        if (ref == null) {
            throw new AssertionError("Misuse of checkIfArray: called with a null argument");
        }
        Class<?> comp = ref.getClass().getComponentType();
        if (comp != null && !comp.isPrimitive()) {
            castNonNullArray((Object[]) ref);
        }
    }
}
