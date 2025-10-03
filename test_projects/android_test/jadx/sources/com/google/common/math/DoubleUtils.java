package com.google.common.math;

import com.google.common.base.Preconditions;
import java.math.BigInteger;

/* loaded from: classes.dex */
final class DoubleUtils {
    static final int EXPONENT_BIAS = 1023;
    static final long EXPONENT_MASK = 9218868437227405312L;
    static final long IMPLICIT_BIT = 4503599627370496L;
    static final long ONE_BITS = 4607182418800017408L;
    static final int SIGNIFICAND_BITS = 52;
    static final long SIGNIFICAND_MASK = 4503599627370495L;
    static final long SIGN_MASK = Long.MIN_VALUE;

    private DoubleUtils() {
    }

    static double nextDown(double d) {
        return -Math.nextUp(-d);
    }

    static long getSignificand(double d) {
        Preconditions.checkArgument(isFinite(d), "not a normal value");
        int exponent = Math.getExponent(d);
        long bits = Double.doubleToRawLongBits(d) & SIGNIFICAND_MASK;
        return exponent == -1023 ? bits << 1 : IMPLICIT_BIT | bits;
    }

    static boolean isFinite(double d) {
        return Math.getExponent(d) <= EXPONENT_BIAS;
    }

    static boolean isNormal(double d) {
        return Math.getExponent(d) >= -1022;
    }

    static double scaleNormalize(double x) {
        long significand = Double.doubleToRawLongBits(x) & SIGNIFICAND_MASK;
        return Double.longBitsToDouble(ONE_BITS | significand);
    }

    static double bigToDouble(BigInteger x) {
        BigInteger absX = x.abs();
        boolean increment = true;
        int exponent = absX.bitLength() - 1;
        if (exponent < 63) {
            return x.longValue();
        }
        if (exponent > EXPONENT_BIAS) {
            return x.signum() * Double.POSITIVE_INFINITY;
        }
        int shift = (exponent - 52) - 1;
        long twiceSignifFloor = absX.shiftRight(shift).longValue();
        long signifFloor = (twiceSignifFloor >> 1) & SIGNIFICAND_MASK;
        if ((twiceSignifFloor & 1) == 0 || ((signifFloor & 1) == 0 && absX.getLowestSetBit() >= shift)) {
            increment = false;
        }
        long signifRounded = increment ? 1 + signifFloor : signifFloor;
        long bits = (exponent + EXPONENT_BIAS) << 52;
        return Double.longBitsToDouble((bits + signifRounded) | (x.signum() & Long.MIN_VALUE));
    }

    static double ensureNonNegative(double value) {
        Preconditions.checkArgument(!Double.isNaN(value));
        if (value <= 0.0d) {
            return 0.0d;
        }
        return value;
    }
}
