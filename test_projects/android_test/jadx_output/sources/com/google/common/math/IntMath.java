package com.google.common.math;

import androidx.constraintlayout.core.motion.utils.TypedValues;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import java.math.RoundingMode;

/* loaded from: classes.dex */
public final class IntMath {
    static final int FLOOR_SQRT_MAX_INT = 46340;
    static final int MAX_POWER_OF_SQRT2_UNSIGNED = -1257966797;
    static final int MAX_SIGNED_POWER_OF_TWO = 1073741824;
    static final byte[] maxLog10ForLeadingZeros = {9, 9, 9, 8, 8, 8, 7, 7, 7, 6, 6, 6, 6, 5, 5, 5, 4, 4, 4, 3, 3, 3, 3, 2, 2, 2, 1, 1, 1, 0, 0, 0, 0};
    static final int[] powersOf10 = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};
    static final int[] halfPowersOf10 = {3, 31, TypedValues.Attributes.TYPE_PATH_ROTATE, 3162, 31622, 316227, 3162277, 31622776, 316227766, Integer.MAX_VALUE};
    private static final int[] factorials = {1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800, 39916800, 479001600};
    static int[] biggestBinomials = {Integer.MAX_VALUE, Integer.MAX_VALUE, 65536, 2345, 477, 193, 110, 75, 58, 49, 43, 39, 37, 35, 34, 34, 33};

    public static int ceilingPowerOfTwo(int x) {
        MathPreconditions.checkPositive("x", x);
        if (x > 1073741824) {
            throw new ArithmeticException("ceilingPowerOfTwo(" + x + ") not representable as an int");
        }
        return 1 << (-Integer.numberOfLeadingZeros(x - 1));
    }

    public static int floorPowerOfTwo(int x) {
        MathPreconditions.checkPositive("x", x);
        return Integer.highestOneBit(x);
    }

    public static boolean isPowerOfTwo(int x) {
        return (((x + (-1)) & x) == 0) & (x > 0);
    }

    static int lessThanBranchFree(int x, int y) {
        return (~(~(x - y))) >>> 31;
    }

    /* renamed from: com.google.common.math.IntMath$1 */
    /* loaded from: classes.dex */
    static /* synthetic */ class C13351 {
        static final /* synthetic */ int[] $SwitchMap$java$math$RoundingMode;

        static {
            int[] iArr = new int[RoundingMode.values().length];
            $SwitchMap$java$math$RoundingMode = iArr;
            try {
                iArr[RoundingMode.UNNECESSARY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.DOWN.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.FLOOR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.UP.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.CEILING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.HALF_DOWN.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.HALF_UP.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$java$math$RoundingMode[RoundingMode.HALF_EVEN.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    public static int log2(int x, RoundingMode mode) {
        MathPreconditions.checkPositive("x", x);
        switch (C13351.$SwitchMap$java$math$RoundingMode[mode.ordinal()]) {
            case 1:
                MathPreconditions.checkRoundingUnnecessary(isPowerOfTwo(x));
                break;
            case 2:
            case 3:
                break;
            case 4:
            case 5:
                return 32 - Integer.numberOfLeadingZeros(x - 1);
            case 6:
            case 7:
            case 8:
                int leadingZeros = Integer.numberOfLeadingZeros(x);
                int cmp = MAX_POWER_OF_SQRT2_UNSIGNED >>> leadingZeros;
                int logFloor = 31 - leadingZeros;
                return lessThanBranchFree(cmp, x) + logFloor;
            default:
                throw new AssertionError();
        }
        return 31 - Integer.numberOfLeadingZeros(x);
    }

    public static int log10(int x, RoundingMode mode) {
        MathPreconditions.checkPositive("x", x);
        int logFloor = log10Floor(x);
        int floorPow = powersOf10[logFloor];
        switch (C13351.$SwitchMap$java$math$RoundingMode[mode.ordinal()]) {
            case 1:
                MathPreconditions.checkRoundingUnnecessary(x == floorPow);
            case 2:
            case 3:
                return logFloor;
            case 4:
            case 5:
                return lessThanBranchFree(floorPow, x) + logFloor;
            case 6:
            case 7:
            case 8:
                return lessThanBranchFree(halfPowersOf10[logFloor], x) + logFloor;
            default:
                throw new AssertionError();
        }
    }

    private static int log10Floor(int x) {
        int y = maxLog10ForLeadingZeros[Integer.numberOfLeadingZeros(x)];
        return y - lessThanBranchFree(x, powersOf10[y]);
    }

    public static int pow(int b, int k) {
        MathPreconditions.checkNonNegative("exponent", k);
        switch (b) {
            case -2:
                if (k < 32) {
                    return (k & 1) == 0 ? 1 << k : -(1 << k);
                }
                return 0;
            case -1:
                return (k & 1) == 0 ? 1 : -1;
            case 0:
                return k == 0 ? 1 : 0;
            case 1:
                return 1;
            case 2:
                if (k < 32) {
                    return 1 << k;
                }
                return 0;
            default:
                int accum = 1;
                while (true) {
                    switch (k) {
                        case 0:
                            return accum;
                        case 1:
                            return b * accum;
                        default:
                            accum *= (k & 1) == 0 ? 1 : b;
                            b *= b;
                            k >>= 1;
                    }
                }
        }
    }

    public static int sqrt(int x, RoundingMode mode) {
        MathPreconditions.checkNonNegative("x", x);
        int sqrtFloor = sqrtFloor(x);
        switch (C13351.$SwitchMap$java$math$RoundingMode[mode.ordinal()]) {
            case 1:
                MathPreconditions.checkRoundingUnnecessary(sqrtFloor * sqrtFloor == x);
            case 2:
            case 3:
                return sqrtFloor;
            case 4:
            case 5:
                int halfSquare = sqrtFloor * sqrtFloor;
                return lessThanBranchFree(halfSquare, x) + sqrtFloor;
            case 6:
            case 7:
            case 8:
                int halfSquare2 = (sqrtFloor * sqrtFloor) + sqrtFloor;
                return lessThanBranchFree(halfSquare2, x) + sqrtFloor;
            default:
                throw new AssertionError();
        }
    }

    private static int sqrtFloor(int x) {
        return (int) Math.sqrt(x);
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:8:0x001d. Please report as an issue. */
    /* JADX WARN: Removed duplicated region for block: B:27:0x0067  */
    /* JADX WARN: Removed duplicated region for block: B:29:0x006a  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static int divide(int r9, int r10, java.math.RoundingMode r11) {
        /*
            com.google.common.base.Preconditions.checkNotNull(r11)
            if (r10 == 0) goto L6c
            int r0 = r9 / r10
            int r1 = r10 * r0
            int r1 = r9 - r1
            if (r1 != 0) goto Le
            return r0
        Le:
            r2 = r9 ^ r10
            int r2 = r2 >> 31
            r3 = 1
            r2 = r2 | r3
            int[] r4 = com.google.common.math.IntMath.C13351.$SwitchMap$java$math$RoundingMode
            int r5 = r11.ordinal()
            r4 = r4[r5]
            r5 = 0
            switch(r4) {
                case 1: goto L5c;
                case 2: goto L63;
                case 3: goto L57;
                case 4: goto L55;
                case 5: goto L50;
                case 6: goto L26;
                case 7: goto L26;
                case 8: goto L26;
                default: goto L20;
            }
        L20:
            java.lang.AssertionError r3 = new java.lang.AssertionError
            r3.<init>()
            throw r3
        L26:
            int r4 = java.lang.Math.abs(r1)
            int r6 = java.lang.Math.abs(r10)
            int r6 = r6 - r4
            int r6 = r4 - r6
            if (r6 != 0) goto L4b
            java.math.RoundingMode r7 = java.math.RoundingMode.HALF_UP
            if (r11 == r7) goto L4a
            java.math.RoundingMode r7 = java.math.RoundingMode.HALF_EVEN
            if (r11 != r7) goto L3d
            r7 = r3
            goto L3e
        L3d:
            r7 = r5
        L3e:
            r8 = r0 & 1
            if (r8 == 0) goto L44
            r8 = r3
            goto L45
        L44:
            r8 = r5
        L45:
            r7 = r7 & r8
            if (r7 == 0) goto L49
            goto L4a
        L49:
            r3 = r5
        L4a:
            goto L65
        L4b:
            if (r6 <= 0) goto L4e
            goto L4f
        L4e:
            r3 = r5
        L4f:
            goto L65
        L50:
            if (r2 <= 0) goto L53
            goto L54
        L53:
            r3 = r5
        L54:
            goto L65
        L55:
            r3 = 1
            goto L65
        L57:
            if (r2 >= 0) goto L5a
            goto L5b
        L5a:
            r3 = r5
        L5b:
            goto L65
        L5c:
            if (r1 != 0) goto L5f
            goto L60
        L5f:
            r3 = r5
        L60:
            com.google.common.math.MathPreconditions.checkRoundingUnnecessary(r3)
        L63:
            r3 = 0
        L65:
            if (r3 == 0) goto L6a
            int r4 = r0 + r2
            goto L6b
        L6a:
            r4 = r0
        L6b:
            return r4
        L6c:
            java.lang.ArithmeticException r0 = new java.lang.ArithmeticException
            java.lang.String r1 = "/ by zero"
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.common.math.IntMath.divide(int, int, java.math.RoundingMode):int");
    }

    public static int mod(int x, int m) {
        if (m <= 0) {
            throw new ArithmeticException("Modulus " + m + " must be > 0");
        }
        int result = x % m;
        return result >= 0 ? result : result + m;
    }

    public static int gcd(int a, int b) {
        MathPreconditions.checkNonNegative("a", a);
        MathPreconditions.checkNonNegative("b", b);
        if (a == 0) {
            return b;
        }
        if (b == 0) {
            return a;
        }
        int aTwos = Integer.numberOfTrailingZeros(a);
        int a2 = a >> aTwos;
        int bTwos = Integer.numberOfTrailingZeros(b);
        int b2 = b >> bTwos;
        while (a2 != b2) {
            int delta = a2 - b2;
            int minDeltaOrZero = (delta >> 31) & delta;
            int a3 = (delta - minDeltaOrZero) - minDeltaOrZero;
            b2 += minDeltaOrZero;
            a2 = a3 >> Integer.numberOfTrailingZeros(a3);
        }
        return a2 << Math.min(aTwos, bTwos);
    }

    public static int checkedAdd(int a, int b) {
        long result = a + b;
        MathPreconditions.checkNoOverflow(result == ((long) ((int) result)), "checkedAdd", a, b);
        return (int) result;
    }

    public static int checkedSubtract(int a, int b) {
        long result = a - b;
        MathPreconditions.checkNoOverflow(result == ((long) ((int) result)), "checkedSubtract", a, b);
        return (int) result;
    }

    public static int checkedMultiply(int a, int b) {
        long result = a * b;
        MathPreconditions.checkNoOverflow(result == ((long) ((int) result)), "checkedMultiply", a, b);
        return (int) result;
    }

    public static int checkedPow(int b, int k) {
        MathPreconditions.checkNonNegative("exponent", k);
        switch (b) {
            case -2:
                MathPreconditions.checkNoOverflow(k < 32, "checkedPow", b, k);
                return (k & 1) == 0 ? 1 << k : (-1) << k;
            case -1:
                return (k & 1) == 0 ? 1 : -1;
            case 0:
                return k == 0 ? 1 : 0;
            case 1:
                return 1;
            case 2:
                MathPreconditions.checkNoOverflow(k < 31, "checkedPow", b, k);
                return 1 << k;
            default:
                int accum = 1;
                while (true) {
                    switch (k) {
                        case 0:
                            return accum;
                        case 1:
                            return checkedMultiply(accum, b);
                        default:
                            if ((k & 1) != 0) {
                                accum = checkedMultiply(accum, b);
                            }
                            k >>= 1;
                            if (k > 0) {
                                MathPreconditions.checkNoOverflow((-46340 <= b) & (b <= FLOOR_SQRT_MAX_INT), "checkedPow", b, k);
                                b *= b;
                            }
                    }
                }
        }
    }

    public static int saturatedAdd(int a, int b) {
        return Ints.saturatedCast(a + b);
    }

    public static int saturatedSubtract(int a, int b) {
        return Ints.saturatedCast(a - b);
    }

    public static int saturatedMultiply(int a, int b) {
        return Ints.saturatedCast(a * b);
    }

    public static int saturatedPow(int b, int k) {
        MathPreconditions.checkNonNegative("exponent", k);
        switch (b) {
            case -2:
                if (k >= 32) {
                    return (k & 1) + Integer.MAX_VALUE;
                }
                return (k & 1) == 0 ? 1 << k : (-1) << k;
            case -1:
                return (k & 1) == 0 ? 1 : -1;
            case 0:
                return k == 0 ? 1 : 0;
            case 1:
                return 1;
            case 2:
                if (k >= 31) {
                    return Integer.MAX_VALUE;
                }
                return 1 << k;
            default:
                int accum = 1;
                int limit = ((b >>> 31) & k & 1) + Integer.MAX_VALUE;
                while (true) {
                    switch (k) {
                        case 0:
                            return accum;
                        case 1:
                            return saturatedMultiply(accum, b);
                        default:
                            if ((k & 1) != 0) {
                                accum = saturatedMultiply(accum, b);
                            }
                            k >>= 1;
                            if (k > 0) {
                                if ((-46340 > b) | (b > FLOOR_SQRT_MAX_INT)) {
                                    return limit;
                                }
                                b *= b;
                            }
                    }
                }
        }
    }

    public static int factorial(int n) {
        MathPreconditions.checkNonNegative("n", n);
        int[] iArr = factorials;
        if (n < iArr.length) {
            return iArr[n];
        }
        return Integer.MAX_VALUE;
    }

    public static int binomial(int n, int k) {
        MathPreconditions.checkNonNegative("n", n);
        MathPreconditions.checkNonNegative("k", k);
        Preconditions.checkArgument(k <= n, "k (%s) > n (%s)", k, n);
        if (k > (n >> 1)) {
            k = n - k;
        }
        int[] iArr = biggestBinomials;
        if (k < iArr.length && n <= iArr[k]) {
            switch (k) {
                case 0:
                    return 1;
                case 1:
                    return n;
                default:
                    long result = 1;
                    for (int i = 0; i < k; i++) {
                        result = (result * (n - i)) / (i + 1);
                    }
                    int i2 = (int) result;
                    return i2;
            }
        }
        return Integer.MAX_VALUE;
    }

    public static int mean(int x, int y) {
        return (x & y) + ((x ^ y) >> 1);
    }

    public static boolean isPrime(int n) {
        return LongMath.isPrime(n);
    }

    private IntMath() {
    }
}
