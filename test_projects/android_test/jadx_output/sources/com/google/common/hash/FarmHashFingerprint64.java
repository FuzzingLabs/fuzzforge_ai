package com.google.common.hash;

import com.google.common.base.Preconditions;

/* loaded from: classes.dex */
final class FarmHashFingerprint64 extends AbstractNonStreamingHashFunction {
    static final HashFunction FARMHASH_FINGERPRINT_64 = new FarmHashFingerprint64();

    /* renamed from: K0 */
    private static final long f202K0 = -4348849565147123417L;

    /* renamed from: K1 */
    private static final long f203K1 = -5435081209227447693L;

    /* renamed from: K2 */
    private static final long f204K2 = -7286425919675154353L;

    FarmHashFingerprint64() {
    }

    @Override // com.google.common.hash.AbstractNonStreamingHashFunction, com.google.common.hash.AbstractHashFunction, com.google.common.hash.HashFunction
    public HashCode hashBytes(byte[] input, int off, int len) {
        Preconditions.checkPositionIndexes(off, off + len, input.length);
        return HashCode.fromLong(fingerprint(input, off, len));
    }

    @Override // com.google.common.hash.HashFunction
    public int bits() {
        return 64;
    }

    public String toString() {
        return "Hashing.farmHashFingerprint64()";
    }

    static long fingerprint(byte[] bytes, int offset, int length) {
        if (length <= 32) {
            if (length <= 16) {
                return hashLength0to16(bytes, offset, length);
            }
            return hashLength17to32(bytes, offset, length);
        }
        if (length <= 64) {
            return hashLength33To64(bytes, offset, length);
        }
        return hashLength65Plus(bytes, offset, length);
    }

    private static long shiftMix(long val) {
        return (val >>> 47) ^ val;
    }

    private static long hashLength16(long u, long v, long mul) {
        long a = (u ^ v) * mul;
        long b = (v ^ (a ^ (a >>> 47))) * mul;
        return (b ^ (b >>> 47)) * mul;
    }

    private static void weakHashLength32WithSeeds(byte[] bytes, int offset, long seedA, long seedB, long[] output) {
        long part1 = LittleEndianByteArray.load64(bytes, offset);
        long part2 = LittleEndianByteArray.load64(bytes, offset + 8);
        long part3 = LittleEndianByteArray.load64(bytes, offset + 16);
        long part4 = LittleEndianByteArray.load64(bytes, offset + 24);
        long seedA2 = seedA + part1;
        long seedB2 = Long.rotateRight(seedB + seedA2 + part4, 21);
        long seedA3 = seedA2 + part2 + part3;
        long seedB3 = seedB2 + Long.rotateRight(seedA3, 44);
        output[0] = seedA3 + part4;
        output[1] = seedB3 + seedA2;
    }

    private static long hashLength0to16(byte[] bytes, int offset, int length) {
        if (length >= 8) {
            long mul = (length * 2) + f204K2;
            long a = f204K2 + LittleEndianByteArray.load64(bytes, offset);
            long b = LittleEndianByteArray.load64(bytes, (offset + length) - 8);
            long c = (Long.rotateRight(b, 37) * mul) + a;
            long d = (Long.rotateRight(a, 25) + b) * mul;
            return hashLength16(c, d, mul);
        }
        if (length >= 4) {
            long a2 = LittleEndianByteArray.load32(bytes, offset) & 4294967295L;
            return hashLength16(length + (a2 << 3), LittleEndianByteArray.load32(bytes, (offset + length) - 4) & 4294967295L, (length * 2) + f204K2);
        }
        if (length <= 0) {
            return f204K2;
        }
        byte a3 = bytes[offset];
        byte b2 = bytes[offset + (length >> 1)];
        byte c2 = bytes[offset + (length - 1)];
        int y = (a3 & 255) + ((b2 & 255) << 8);
        int z = ((c2 & 255) << 2) + length;
        return shiftMix((y * f204K2) ^ (z * f202K0)) * f204K2;
    }

    private static long hashLength17to32(byte[] bytes, int offset, int length) {
        long mul = (length * 2) + f204K2;
        long a = LittleEndianByteArray.load64(bytes, offset) * f203K1;
        long b = LittleEndianByteArray.load64(bytes, offset + 8);
        long c = LittleEndianByteArray.load64(bytes, (offset + length) - 8) * mul;
        long d = LittleEndianByteArray.load64(bytes, (offset + length) - 16) * f204K2;
        return hashLength16(Long.rotateRight(a + b, 43) + Long.rotateRight(c, 30) + d, Long.rotateRight(f204K2 + b, 18) + a + c, mul);
    }

    private static long hashLength33To64(byte[] bytes, int offset, int length) {
        long mul = (length * 2) + f204K2;
        long a = LittleEndianByteArray.load64(bytes, offset) * f204K2;
        long b = LittleEndianByteArray.load64(bytes, offset + 8);
        long c = LittleEndianByteArray.load64(bytes, (offset + length) - 8) * mul;
        long d = LittleEndianByteArray.load64(bytes, (offset + length) - 16) * f204K2;
        long y = Long.rotateRight(a + b, 43) + Long.rotateRight(c, 30) + d;
        long z = hashLength16(y, Long.rotateRight(f204K2 + b, 18) + a + c, mul);
        long e = LittleEndianByteArray.load64(bytes, offset + 16) * mul;
        long f = LittleEndianByteArray.load64(bytes, offset + 24);
        long g = (y + LittleEndianByteArray.load64(bytes, (offset + length) - 32)) * mul;
        long h = (z + LittleEndianByteArray.load64(bytes, (offset + length) - 24)) * mul;
        return hashLength16(Long.rotateRight(e + f, 43) + Long.rotateRight(g, 30) + h, e + Long.rotateRight(f + a, 18) + g, mul);
    }

    private static long hashLength65Plus(byte[] bytes, int offset, int length) {
        long z = 2480279821605975764L;
        long z2 = shiftMix((2480279821605975764L * f204K2) + 113) * f204K2;
        long[] v = new long[2];
        long[] w = new long[2];
        long x = (f204K2 * 81) + LittleEndianByteArray.load64(bytes, offset);
        int end = offset + (((length - 1) / 64) * 64);
        int last64offset = (((length - 1) & 63) + end) - 63;
        long z3 = z2;
        int offset2 = offset;
        while (true) {
            long[] v2 = v;
            long x2 = Long.rotateRight(x + z + v[0] + LittleEndianByteArray.load64(bytes, offset2 + 8), 37) * f203K1;
            long y = Long.rotateRight(v2[1] + z + LittleEndianByteArray.load64(bytes, offset2 + 48), 42) * f203K1;
            long y2 = w[1];
            long x3 = x2 ^ y2;
            long y3 = y + v2[0] + LittleEndianByteArray.load64(bytes, offset2 + 40);
            long z4 = Long.rotateRight(w[0] + z3, 33) * f203K1;
            long z5 = v2[1];
            weakHashLength32WithSeeds(bytes, offset2, z5 * f203K1, x3 + w[0], v2);
            weakHashLength32WithSeeds(bytes, offset2 + 32, z4 + w[1], y3 + LittleEndianByteArray.load64(bytes, offset2 + 16), w);
            x = z4;
            offset2 += 64;
            if (offset2 == end) {
                long mul = ((255 & x3) << 1) + f203K1;
                w[0] = w[0] + ((length - 1) & 63);
                v2[0] = v2[0] + w[0];
                w[0] = w[0] + v2[0];
                long x4 = Long.rotateRight(x + y3 + v2[0] + LittleEndianByteArray.load64(bytes, last64offset + 8), 37) * mul;
                long x5 = v2[1];
                long y4 = Long.rotateRight(y3 + x5 + LittleEndianByteArray.load64(bytes, last64offset + 48), 42) * mul;
                long x6 = (w[1] * 9) ^ x4;
                long y5 = y4 + (v2[0] * 9) + LittleEndianByteArray.load64(bytes, last64offset + 40);
                long y6 = w[0];
                long z6 = Long.rotateRight(y6 + x3, 33) * mul;
                weakHashLength32WithSeeds(bytes, last64offset, v2[1] * mul, w[0] + x6, v2);
                weakHashLength32WithSeeds(bytes, last64offset + 32, z6 + w[1], LittleEndianByteArray.load64(bytes, last64offset + 16) + y5, w);
                return hashLength16(hashLength16(v2[0], w[0], mul) + (shiftMix(y5) * f202K0) + x6, hashLength16(v2[1], w[1], mul) + z6, mul);
            }
            z3 = x3;
            v = v2;
            z = y3;
        }
    }
}
