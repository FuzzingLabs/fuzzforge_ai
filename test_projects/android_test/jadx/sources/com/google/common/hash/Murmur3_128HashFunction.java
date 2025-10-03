package com.google.common.hash;

import com.google.common.primitives.UnsignedBytes;
import com.google.errorprone.annotations.Immutable;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Immutable
/* loaded from: classes.dex */
final class Murmur3_128HashFunction extends AbstractHashFunction implements Serializable {
    private static final long serialVersionUID = 0;
    private final int seed;
    static final HashFunction MURMUR3_128 = new Murmur3_128HashFunction(0);
    static final HashFunction GOOD_FAST_HASH_128 = new Murmur3_128HashFunction(Hashing.GOOD_FAST_HASH_SEED);

    Murmur3_128HashFunction(int seed) {
        this.seed = seed;
    }

    @Override // com.google.common.hash.HashFunction
    public int bits() {
        return 128;
    }

    @Override // com.google.common.hash.HashFunction
    public Hasher newHasher() {
        return new Murmur3_128Hasher(this.seed);
    }

    public String toString() {
        return "Hashing.murmur3_128(" + this.seed + ")";
    }

    public boolean equals(Object object) {
        if (!(object instanceof Murmur3_128HashFunction)) {
            return false;
        }
        Murmur3_128HashFunction other = (Murmur3_128HashFunction) object;
        return this.seed == other.seed;
    }

    public int hashCode() {
        return getClass().hashCode() ^ this.seed;
    }

    private static final class Murmur3_128Hasher extends AbstractStreamingHasher {

        /* renamed from: C1 */
        private static final long f205C1 = -8663945395140668459L;

        /* renamed from: C2 */
        private static final long f206C2 = 5545529020109919103L;
        private static final int CHUNK_SIZE = 16;

        /* renamed from: h1 */
        private long f207h1;

        /* renamed from: h2 */
        private long f208h2;
        private int length;

        Murmur3_128Hasher(int seed) {
            super(16);
            this.f207h1 = seed;
            this.f208h2 = seed;
            this.length = 0;
        }

        @Override // com.google.common.hash.AbstractStreamingHasher
        protected void process(ByteBuffer bb) {
            long k1 = bb.getLong();
            long k2 = bb.getLong();
            bmix64(k1, k2);
            this.length += 16;
        }

        private void bmix64(long k1, long k2) {
            long mixK1 = this.f207h1 ^ mixK1(k1);
            this.f207h1 = mixK1;
            long rotateLeft = Long.rotateLeft(mixK1, 27);
            this.f207h1 = rotateLeft;
            long j = this.f208h2;
            long j2 = rotateLeft + j;
            this.f207h1 = j2;
            this.f207h1 = (j2 * 5) + 1390208809;
            long mixK2 = mixK2(k2) ^ j;
            this.f208h2 = mixK2;
            long rotateLeft2 = Long.rotateLeft(mixK2, 31);
            this.f208h2 = rotateLeft2;
            long j3 = rotateLeft2 + this.f207h1;
            this.f208h2 = j3;
            this.f208h2 = (j3 * 5) + 944331445;
        }

        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // com.google.common.hash.AbstractStreamingHasher
        protected void processRemaining(ByteBuffer bb) {
            long k1;
            long k12 = 0;
            long k2 = 0;
            this.length += bb.remaining();
            switch (bb.remaining()) {
                case 1:
                    k1 = k12 ^ UnsignedBytes.toInt(bb.get(0));
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 2:
                    k12 ^= UnsignedBytes.toInt(bb.get(1)) << 8;
                    k1 = k12 ^ UnsignedBytes.toInt(bb.get(0));
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 3:
                    k12 ^= UnsignedBytes.toInt(bb.get(2)) << 16;
                    k12 ^= UnsignedBytes.toInt(bb.get(1)) << 8;
                    k1 = k12 ^ UnsignedBytes.toInt(bb.get(0));
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 4:
                    k12 ^= UnsignedBytes.toInt(bb.get(3)) << 24;
                    k12 ^= UnsignedBytes.toInt(bb.get(2)) << 16;
                    k12 ^= UnsignedBytes.toInt(bb.get(1)) << 8;
                    k1 = k12 ^ UnsignedBytes.toInt(bb.get(0));
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 5:
                    k12 ^= UnsignedBytes.toInt(bb.get(4)) << 32;
                    k12 ^= UnsignedBytes.toInt(bb.get(3)) << 24;
                    k12 ^= UnsignedBytes.toInt(bb.get(2)) << 16;
                    k12 ^= UnsignedBytes.toInt(bb.get(1)) << 8;
                    k1 = k12 ^ UnsignedBytes.toInt(bb.get(0));
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 6:
                    k12 ^= UnsignedBytes.toInt(bb.get(5)) << 40;
                    k12 ^= UnsignedBytes.toInt(bb.get(4)) << 32;
                    k12 ^= UnsignedBytes.toInt(bb.get(3)) << 24;
                    k12 ^= UnsignedBytes.toInt(bb.get(2)) << 16;
                    k12 ^= UnsignedBytes.toInt(bb.get(1)) << 8;
                    k1 = k12 ^ UnsignedBytes.toInt(bb.get(0));
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 7:
                    k12 = 0 ^ (UnsignedBytes.toInt(bb.get(6)) << 48);
                    k12 ^= UnsignedBytes.toInt(bb.get(5)) << 40;
                    k12 ^= UnsignedBytes.toInt(bb.get(4)) << 32;
                    k12 ^= UnsignedBytes.toInt(bb.get(3)) << 24;
                    k12 ^= UnsignedBytes.toInt(bb.get(2)) << 16;
                    k12 ^= UnsignedBytes.toInt(bb.get(1)) << 8;
                    k1 = k12 ^ UnsignedBytes.toInt(bb.get(0));
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 8:
                    k1 = 0 ^ bb.getLong();
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 9:
                    k2 ^= UnsignedBytes.toInt(bb.get(8));
                    k1 = 0 ^ bb.getLong();
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 10:
                    k2 ^= UnsignedBytes.toInt(bb.get(9)) << 8;
                    k2 ^= UnsignedBytes.toInt(bb.get(8));
                    k1 = 0 ^ bb.getLong();
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 11:
                    k2 ^= UnsignedBytes.toInt(bb.get(10)) << 16;
                    k2 ^= UnsignedBytes.toInt(bb.get(9)) << 8;
                    k2 ^= UnsignedBytes.toInt(bb.get(8));
                    k1 = 0 ^ bb.getLong();
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 12:
                    k2 ^= UnsignedBytes.toInt(bb.get(11)) << 24;
                    k2 ^= UnsignedBytes.toInt(bb.get(10)) << 16;
                    k2 ^= UnsignedBytes.toInt(bb.get(9)) << 8;
                    k2 ^= UnsignedBytes.toInt(bb.get(8));
                    k1 = 0 ^ bb.getLong();
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 13:
                    k2 ^= UnsignedBytes.toInt(bb.get(12)) << 32;
                    k2 ^= UnsignedBytes.toInt(bb.get(11)) << 24;
                    k2 ^= UnsignedBytes.toInt(bb.get(10)) << 16;
                    k2 ^= UnsignedBytes.toInt(bb.get(9)) << 8;
                    k2 ^= UnsignedBytes.toInt(bb.get(8));
                    k1 = 0 ^ bb.getLong();
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 14:
                    k2 ^= UnsignedBytes.toInt(bb.get(13)) << 40;
                    k2 ^= UnsignedBytes.toInt(bb.get(12)) << 32;
                    k2 ^= UnsignedBytes.toInt(bb.get(11)) << 24;
                    k2 ^= UnsignedBytes.toInt(bb.get(10)) << 16;
                    k2 ^= UnsignedBytes.toInt(bb.get(9)) << 8;
                    k2 ^= UnsignedBytes.toInt(bb.get(8));
                    k1 = 0 ^ bb.getLong();
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                case 15:
                    k2 = 0 ^ (UnsignedBytes.toInt(bb.get(14)) << 48);
                    k2 ^= UnsignedBytes.toInt(bb.get(13)) << 40;
                    k2 ^= UnsignedBytes.toInt(bb.get(12)) << 32;
                    k2 ^= UnsignedBytes.toInt(bb.get(11)) << 24;
                    k2 ^= UnsignedBytes.toInt(bb.get(10)) << 16;
                    k2 ^= UnsignedBytes.toInt(bb.get(9)) << 8;
                    k2 ^= UnsignedBytes.toInt(bb.get(8));
                    k1 = 0 ^ bb.getLong();
                    this.f207h1 ^= mixK1(k1);
                    this.f208h2 ^= mixK2(k2);
                    return;
                default:
                    throw new AssertionError("Should never get here.");
            }
        }

        @Override // com.google.common.hash.AbstractStreamingHasher
        public HashCode makeHash() {
            long j = this.f207h1;
            int i = this.length;
            long j2 = j ^ i;
            this.f207h1 = j2;
            long j3 = this.f208h2 ^ i;
            this.f208h2 = j3;
            long j4 = j2 + j3;
            this.f207h1 = j4;
            this.f208h2 = j3 + j4;
            this.f207h1 = fmix64(j4);
            long fmix64 = fmix64(this.f208h2);
            this.f208h2 = fmix64;
            long j5 = this.f207h1 + fmix64;
            this.f207h1 = j5;
            this.f208h2 = fmix64 + j5;
            return HashCode.fromBytesNoCopy(ByteBuffer.wrap(new byte[16]).order(ByteOrder.LITTLE_ENDIAN).putLong(this.f207h1).putLong(this.f208h2).array());
        }

        private static long fmix64(long k) {
            long k2 = (k ^ (k >>> 33)) * (-49064778989728563L);
            long k3 = (k2 ^ (k2 >>> 33)) * (-4265267296055464877L);
            return k3 ^ (k3 >>> 33);
        }

        private static long mixK1(long k1) {
            return Long.rotateLeft(k1 * f205C1, 31) * f206C2;
        }

        private static long mixK2(long k2) {
            return Long.rotateLeft(k2 * f206C2, 33) * f205C1;
        }
    }
}
