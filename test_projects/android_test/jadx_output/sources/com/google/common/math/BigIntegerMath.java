package com.google.common.math;

import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public final class BigIntegerMath {
    static final int SQRT2_PRECOMPUTE_THRESHOLD = 256;
    static final BigInteger SQRT2_PRECOMPUTED_BITS = new BigInteger("16a09e667f3bcc908b2fb1366ea957d3e3adec17512775099da2f590b0667322a", 16);
    private static final double LN_10 = Math.log(10.0d);
    private static final double LN_2 = Math.log(2.0d);

    public static BigInteger ceilingPowerOfTwo(BigInteger x) {
        return BigInteger.ZERO.setBit(log2(x, RoundingMode.CEILING));
    }

    public static BigInteger floorPowerOfTwo(BigInteger x) {
        return BigInteger.ZERO.setBit(log2(x, RoundingMode.FLOOR));
    }

    public static boolean isPowerOfTwo(BigInteger x) {
        Preconditions.checkNotNull(x);
        return x.signum() > 0 && x.getLowestSetBit() == x.bitLength() - 1;
    }

    public static int log2(BigInteger x, RoundingMode mode) {
        MathPreconditions.checkPositive("x", (BigInteger) Preconditions.checkNotNull(x));
        int logFloor = x.bitLength() - 1;
        switch (C13331.$SwitchMap$java$math$RoundingMode[mode.ordinal()]) {
            case 1:
                MathPreconditions.checkRoundingUnnecessary(isPowerOfTwo(x));
            case 2:
            case 3:
                return logFloor;
            case 4:
            case 5:
                return isPowerOfTwo(x) ? logFloor : logFloor + 1;
            case 6:
            case 7:
            case 8:
                if (logFloor < 256) {
                    BigInteger halfPower = SQRT2_PRECOMPUTED_BITS.shiftRight(256 - logFloor);
                    if (x.compareTo(halfPower) <= 0) {
                        return logFloor;
                    }
                    return logFloor + 1;
                }
                BigInteger x2 = x.pow(2);
                int logX2Floor = x2.bitLength() - 1;
                return logX2Floor < (logFloor * 2) + 1 ? logFloor : logFloor + 1;
            default:
                throw new AssertionError();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.google.common.math.BigIntegerMath$1 */
    /* loaded from: classes.dex */
    public static /* synthetic */ class C13331 {
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

    /* JADX WARN: Removed duplicated region for block: B:13:0x0066  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x006c  */
    /* JADX WARN: Removed duplicated region for block: B:21:0x0086  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x0091  */
    /* JADX WARN: Removed duplicated region for block: B:31:0x0099 A[FALL_THROUGH, RETURN] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static int log10(java.math.BigInteger r9, java.math.RoundingMode r10) {
        /*
            java.lang.String r0 = "x"
            com.google.common.math.MathPreconditions.checkPositive(r0, r9)
            boolean r0 = fitsInLong(r9)
            if (r0 == 0) goto L14
            long r0 = r9.longValue()
            int r0 = com.google.common.math.LongMath.log10(r0, r10)
            return r0
        L14:
            java.math.RoundingMode r0 = java.math.RoundingMode.FLOOR
            int r0 = log2(r9, r0)
            double r0 = (double) r0
            double r2 = com.google.common.math.BigIntegerMath.LN_2
            double r0 = r0 * r2
            double r2 = com.google.common.math.BigIntegerMath.LN_10
            double r0 = r0 / r2
            int r0 = (int) r0
            java.math.BigInteger r1 = java.math.BigInteger.TEN
            java.math.BigInteger r1 = r1.pow(r0)
            int r2 = r1.compareTo(r9)
            if (r2 <= 0) goto L3d
        L2e:
            int r0 = r0 + (-1)
            java.math.BigInteger r3 = java.math.BigInteger.TEN
            java.math.BigInteger r1 = r1.divide(r3)
            int r2 = r1.compareTo(r9)
            if (r2 > 0) goto L2e
            goto L58
        L3d:
            java.math.BigInteger r3 = java.math.BigInteger.TEN
            java.math.BigInteger r3 = r3.multiply(r1)
            int r4 = r3.compareTo(r9)
        L47:
            if (r4 > 0) goto L58
            int r0 = r0 + 1
            r1 = r3
            r2 = r4
            java.math.BigInteger r5 = java.math.BigInteger.TEN
            java.math.BigInteger r3 = r5.multiply(r1)
            int r4 = r3.compareTo(r9)
            goto L47
        L58:
            r3 = r0
            r4 = r1
            r5 = r2
            int[] r6 = com.google.common.math.BigIntegerMath.C13331.$SwitchMap$java$math$RoundingMode
            int r7 = r10.ordinal()
            r6 = r6[r7]
            switch(r6) {
                case 1: goto L91;
                case 2: goto L99;
                case 3: goto L99;
                case 4: goto L86;
                case 5: goto L86;
                case 6: goto L6c;
                case 7: goto L6c;
                case 8: goto L6c;
                default: goto L66;
            }
        L66:
            java.lang.AssertionError r6 = new java.lang.AssertionError
            r6.<init>()
            throw r6
        L6c:
            r6 = 2
            java.math.BigInteger r7 = r9.pow(r6)
            java.math.BigInteger r6 = r4.pow(r6)
            java.math.BigInteger r8 = java.math.BigInteger.TEN
            java.math.BigInteger r6 = r6.multiply(r8)
            int r8 = r7.compareTo(r6)
            if (r8 > 0) goto L83
            r8 = r3
            goto L85
        L83:
            int r8 = r3 + 1
        L85:
            return r8
        L86:
            boolean r6 = r4.equals(r9)
            if (r6 == 0) goto L8e
            r6 = r3
            goto L90
        L8e:
            int r6 = r3 + 1
        L90:
            return r6
        L91:
            if (r5 != 0) goto L95
            r6 = 1
            goto L96
        L95:
            r6 = 0
        L96:
            com.google.common.math.MathPreconditions.checkRoundingUnnecessary(r6)
        L99:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.common.math.BigIntegerMath.log10(java.math.BigInteger, java.math.RoundingMode):int");
    }

    public static BigInteger sqrt(BigInteger x, RoundingMode mode) {
        MathPreconditions.checkNonNegative("x", x);
        if (fitsInLong(x)) {
            return BigInteger.valueOf(LongMath.sqrt(x.longValue(), mode));
        }
        BigInteger sqrtFloor = sqrtFloor(x);
        switch (C13331.$SwitchMap$java$math$RoundingMode[mode.ordinal()]) {
            case 1:
                MathPreconditions.checkRoundingUnnecessary(sqrtFloor.pow(2).equals(x));
            case 2:
            case 3:
                return sqrtFloor;
            case 4:
            case 5:
                int sqrtFloorInt = sqrtFloor.intValue();
                boolean sqrtFloorIsExact = sqrtFloorInt * sqrtFloorInt == x.intValue() && sqrtFloor.pow(2).equals(x);
                return sqrtFloorIsExact ? sqrtFloor : sqrtFloor.add(BigInteger.ONE);
            case 6:
            case 7:
            case 8:
                BigInteger halfSquare = sqrtFloor.pow(2).add(sqrtFloor);
                return halfSquare.compareTo(x) >= 0 ? sqrtFloor : sqrtFloor.add(BigInteger.ONE);
            default:
                throw new AssertionError();
        }
    }

    private static BigInteger sqrtFloor(BigInteger x) {
        BigInteger sqrt0;
        BigInteger sqrt02;
        int log2 = log2(x, RoundingMode.FLOOR);
        if (log2 < 1023) {
            sqrt0 = sqrtApproxWithDoubles(x);
        } else {
            int shift = (log2 - 52) & (-2);
            sqrt0 = sqrtApproxWithDoubles(x.shiftRight(shift)).shiftLeft(shift >> 1);
        }
        BigInteger sqrt1 = sqrt0.add(x.divide(sqrt0)).shiftRight(1);
        if (sqrt0.equals(sqrt1)) {
            return sqrt0;
        }
        do {
            sqrt02 = sqrt1;
            sqrt1 = sqrt02.add(x.divide(sqrt02)).shiftRight(1);
        } while (sqrt1.compareTo(sqrt02) < 0);
        return sqrt02;
    }

    private static BigInteger sqrtApproxWithDoubles(BigInteger x) {
        return DoubleMath.roundToBigInteger(Math.sqrt(DoubleUtils.bigToDouble(x)), RoundingMode.HALF_EVEN);
    }

    public static BigInteger divide(BigInteger p, BigInteger q, RoundingMode mode) {
        BigDecimal pDec = new BigDecimal(p);
        BigDecimal qDec = new BigDecimal(q);
        return pDec.divide(qDec, 0, mode).toBigIntegerExact();
    }

    public static BigInteger factorial(int n) {
        MathPreconditions.checkNonNegative("n", n);
        if (n < LongMath.factorials.length) {
            return BigInteger.valueOf(LongMath.factorials[n]);
        }
        int approxSize = IntMath.divide(IntMath.log2(n, RoundingMode.CEILING) * n, 64, RoundingMode.CEILING);
        ArrayList<BigInteger> bignums = new ArrayList<>(approxSize);
        int startingNumber = LongMath.factorials.length;
        long product = LongMath.factorials[startingNumber - 1];
        int shift = Long.numberOfTrailingZeros(product);
        long product2 = product >> shift;
        int productBits = LongMath.log2(product2, RoundingMode.FLOOR) + 1;
        int bits = LongMath.log2(startingNumber, RoundingMode.FLOOR) + 1;
        int nextPowerOfTwo = 1 << (bits - 1);
        long num = startingNumber;
        while (num <= n) {
            int startingNumber2 = startingNumber;
            if ((nextPowerOfTwo & num) != 0) {
                bits++;
                nextPowerOfTwo <<= 1;
            }
            int tz = Long.numberOfTrailingZeros(num);
            long normalizedNum = num >> tz;
            shift += tz;
            int normalizedBits = bits - tz;
            if (normalizedBits + productBits >= 64) {
                bignums.add(BigInteger.valueOf(product2));
                product2 = 1;
            }
            product2 *= normalizedNum;
            productBits = LongMath.log2(product2, RoundingMode.FLOOR) + 1;
            num++;
            startingNumber = startingNumber2;
        }
        if (product2 > 1) {
            bignums.add(BigInteger.valueOf(product2));
        }
        return listProduct(bignums).shiftLeft(shift);
    }

    static BigInteger listProduct(List<BigInteger> nums) {
        return listProduct(nums, 0, nums.size());
    }

    static BigInteger listProduct(List<BigInteger> nums, int start, int end) {
        switch (end - start) {
            case 0:
                return BigInteger.ONE;
            case 1:
                return nums.get(start);
            case 2:
                return nums.get(start).multiply(nums.get(start + 1));
            case 3:
                return nums.get(start).multiply(nums.get(start + 1)).multiply(nums.get(start + 2));
            default:
                int m = (end + start) >>> 1;
                return listProduct(nums, start, m).multiply(listProduct(nums, m, end));
        }
    }

    public static BigInteger binomial(int n, int k) {
        MathPreconditions.checkNonNegative("n", n);
        MathPreconditions.checkNonNegative("k", k);
        Preconditions.checkArgument(k <= n, "k (%s) > n (%s)", k, n);
        if (k > (n >> 1)) {
            k = n - k;
        }
        if (k < LongMath.biggestBinomials.length && n <= LongMath.biggestBinomials[k]) {
            return BigInteger.valueOf(LongMath.binomial(n, k));
        }
        BigInteger accum = BigInteger.ONE;
        long numeratorAccum = n;
        long denominatorAccum = 1;
        int bits = LongMath.log2(n, RoundingMode.CEILING);
        int numeratorBits = bits;
        for (int i = 1; i < k; i++) {
            int p = n - i;
            int q = i + 1;
            if (numeratorBits + bits >= 63) {
                accum = accum.multiply(BigInteger.valueOf(numeratorAccum)).divide(BigInteger.valueOf(denominatorAccum));
                numeratorAccum = p;
                denominatorAccum = q;
                numeratorBits = bits;
            } else {
                numeratorAccum *= p;
                denominatorAccum *= q;
                numeratorBits += bits;
            }
        }
        return accum.multiply(BigInteger.valueOf(numeratorAccum)).divide(BigInteger.valueOf(denominatorAccum));
    }

    static boolean fitsInLong(BigInteger x) {
        return x.bitLength() <= 63;
    }

    private BigIntegerMath() {
    }
}
