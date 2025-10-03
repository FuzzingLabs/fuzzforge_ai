package com.google.common.hash;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.common.primitives.UnsignedBytes;
import com.google.errorprone.annotations.Immutable;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/* JADX INFO: Access modifiers changed from: package-private */
@Immutable
/* loaded from: classes.dex */
public final class Murmur3_32HashFunction extends AbstractHashFunction implements Serializable {

    /* renamed from: C1 */
    private static final int f209C1 = -862048943;

    /* renamed from: C2 */
    private static final int f210C2 = 461845907;
    private static final int CHUNK_SIZE = 4;
    private static final long serialVersionUID = 0;
    private final int seed;
    static final HashFunction MURMUR3_32 = new Murmur3_32HashFunction(0);
    static final HashFunction GOOD_FAST_HASH_32 = new Murmur3_32HashFunction(Hashing.GOOD_FAST_HASH_SEED);

    /* JADX INFO: Access modifiers changed from: package-private */
    public Murmur3_32HashFunction(int seed) {
        this.seed = seed;
    }

    @Override // com.google.common.hash.HashFunction
    public int bits() {
        return 32;
    }

    @Override // com.google.common.hash.HashFunction
    public Hasher newHasher() {
        return new Murmur3_32Hasher(this.seed);
    }

    public String toString() {
        return "Hashing.murmur3_32(" + this.seed + ")";
    }

    public boolean equals(Object object) {
        if (!(object instanceof Murmur3_32HashFunction)) {
            return false;
        }
        Murmur3_32HashFunction other = (Murmur3_32HashFunction) object;
        return this.seed == other.seed;
    }

    public int hashCode() {
        return getClass().hashCode() ^ this.seed;
    }

    @Override // com.google.common.hash.AbstractHashFunction, com.google.common.hash.HashFunction
    public HashCode hashInt(int input) {
        int k1 = mixK1(input);
        int h1 = mixH1(this.seed, k1);
        return fmix(h1, 4);
    }

    @Override // com.google.common.hash.AbstractHashFunction, com.google.common.hash.HashFunction
    public HashCode hashLong(long input) {
        int low = (int) input;
        int high = (int) (input >>> 32);
        int k1 = mixK1(low);
        int h1 = mixH1(this.seed, k1);
        int k12 = mixK1(high);
        return fmix(mixH1(h1, k12), 8);
    }

    @Override // com.google.common.hash.AbstractHashFunction, com.google.common.hash.HashFunction
    public HashCode hashUnencodedChars(CharSequence input) {
        int h1 = this.seed;
        for (int i = 1; i < input.length(); i += 2) {
            int k1 = input.charAt(i - 1) | (input.charAt(i) << 16);
            h1 = mixH1(h1, mixK1(k1));
        }
        int i2 = input.length();
        if ((i2 & 1) == 1) {
            int k12 = input.charAt(input.length() - 1);
            h1 ^= mixK1(k12);
        }
        int k13 = input.length();
        return fmix(h1, k13 * 2);
    }

    @Override // com.google.common.hash.AbstractHashFunction, com.google.common.hash.HashFunction
    public HashCode hashString(CharSequence input, Charset charset) {
        if (Charsets.UTF_8.equals(charset)) {
            int utf16Length = input.length();
            int h1 = this.seed;
            int i = 0;
            int len = 0;
            while (i + 4 <= utf16Length) {
                char c0 = input.charAt(i);
                char c1 = input.charAt(i + 1);
                char c2 = input.charAt(i + 2);
                char c3 = input.charAt(i + 3);
                if (c0 >= 128 || c1 >= 128 || c2 >= 128 || c3 >= 128) {
                    break;
                }
                int k1 = (c1 << '\b') | c0 | (c2 << 16) | (c3 << 24);
                h1 = mixH1(h1, mixK1(k1));
                i += 4;
                len += 4;
            }
            long buffer = 0;
            int shift = 0;
            while (i < utf16Length) {
                char c = input.charAt(i);
                if (c < 128) {
                    buffer |= c << shift;
                    shift += 8;
                    len++;
                } else if (c < 2048) {
                    buffer |= charToTwoUtf8Bytes(c) << shift;
                    shift += 16;
                    len += 2;
                } else if (c < 55296 || c > 57343) {
                    buffer |= charToThreeUtf8Bytes(c) << shift;
                    shift += 24;
                    len += 3;
                } else {
                    int codePoint = Character.codePointAt(input, i);
                    if (codePoint == c) {
                        return hashBytes(input.toString().getBytes(charset));
                    }
                    i++;
                    buffer |= codePointToFourUtf8Bytes(codePoint) << shift;
                    len += 4;
                }
                if (shift >= 32) {
                    int k12 = mixK1((int) buffer);
                    h1 = mixH1(h1, k12);
                    buffer >>>= 32;
                    shift -= 32;
                }
                i++;
            }
            int k13 = mixK1((int) buffer);
            return fmix(h1 ^ k13, len);
        }
        return hashBytes(input.toString().getBytes(charset));
    }

    @Override // com.google.common.hash.AbstractHashFunction, com.google.common.hash.HashFunction
    public HashCode hashBytes(byte[] input, int off, int len) {
        Preconditions.checkPositionIndexes(off, off + len, input.length);
        int h1 = this.seed;
        int i = 0;
        while (i + 4 <= len) {
            int k1 = mixK1(getIntLittleEndian(input, off + i));
            h1 = mixH1(h1, k1);
            i += 4;
        }
        int k12 = 0;
        int shift = 0;
        while (i < len) {
            k12 ^= UnsignedBytes.toInt(input[off + i]) << shift;
            i++;
            shift += 8;
        }
        int shift2 = mixK1(k12);
        return fmix(h1 ^ shift2, len);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int getIntLittleEndian(byte[] input, int offset) {
        return Ints.fromBytes(input[offset + 3], input[offset + 2], input[offset + 1], input[offset]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int mixK1(int k1) {
        return Integer.rotateLeft(k1 * f209C1, 15) * f210C2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int mixH1(int h1, int k1) {
        return (Integer.rotateLeft(h1 ^ k1, 13) * 5) - 430675100;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static HashCode fmix(int h1, int length) {
        int h12 = h1 ^ length;
        int h13 = (h12 ^ (h12 >>> 16)) * (-2048144789);
        int h14 = (h13 ^ (h13 >>> 13)) * (-1028477387);
        return HashCode.fromInt(h14 ^ (h14 >>> 16));
    }

    /* loaded from: classes.dex */
    private static final class Murmur3_32Hasher extends AbstractHasher {
        private long buffer;

        /* renamed from: h1 */
        private int f211h1;
        private int shift;
        private int length = 0;
        private boolean isDone = false;

        Murmur3_32Hasher(int seed) {
            this.f211h1 = seed;
        }

        private void update(int nBytes, long update) {
            long j = this.buffer;
            int i = this.shift;
            long j2 = j | ((4294967295L & update) << i);
            this.buffer = j2;
            int i2 = i + (nBytes * 8);
            this.shift = i2;
            this.length += nBytes;
            if (i2 >= 32) {
                this.f211h1 = Murmur3_32HashFunction.mixH1(this.f211h1, Murmur3_32HashFunction.mixK1((int) j2));
                this.buffer >>>= 32;
                this.shift -= 32;
            }
        }

        @Override // com.google.common.hash.AbstractHasher, com.google.common.hash.Hasher, com.google.common.hash.PrimitiveSink
        public Hasher putByte(byte b) {
            update(1, b & 255);
            return this;
        }

        @Override // com.google.common.hash.AbstractHasher, com.google.common.hash.PrimitiveSink
        public Hasher putBytes(byte[] bytes, int off, int len) {
            Preconditions.checkPositionIndexes(off, off + len, bytes.length);
            int i = 0;
            while (i + 4 <= len) {
                update(4, Murmur3_32HashFunction.getIntLittleEndian(bytes, off + i));
                i += 4;
            }
            while (i < len) {
                putByte(bytes[off + i]);
                i++;
            }
            return this;
        }

        @Override // com.google.common.hash.AbstractHasher, com.google.common.hash.PrimitiveSink
        public Hasher putBytes(ByteBuffer buffer) {
            ByteOrder bo = buffer.order();
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            while (buffer.remaining() >= 4) {
                putInt(buffer.getInt());
            }
            while (buffer.hasRemaining()) {
                putByte(buffer.get());
            }
            buffer.order(bo);
            return this;
        }

        @Override // com.google.common.hash.AbstractHasher, com.google.common.hash.PrimitiveSink
        public Hasher putInt(int i) {
            update(4, i);
            return this;
        }

        @Override // com.google.common.hash.AbstractHasher, com.google.common.hash.PrimitiveSink
        public Hasher putLong(long l) {
            update(4, (int) l);
            update(4, l >>> 32);
            return this;
        }

        @Override // com.google.common.hash.AbstractHasher, com.google.common.hash.PrimitiveSink
        public Hasher putChar(char c) {
            update(2, c);
            return this;
        }

        @Override // com.google.common.hash.AbstractHasher, com.google.common.hash.PrimitiveSink
        public Hasher putString(CharSequence input, Charset charset) {
            if (Charsets.UTF_8.equals(charset)) {
                int utf16Length = input.length();
                int i = 0;
                while (i + 4 <= utf16Length) {
                    char c0 = input.charAt(i);
                    char c1 = input.charAt(i + 1);
                    char c2 = input.charAt(i + 2);
                    char c3 = input.charAt(i + 3);
                    if (c0 >= 128 || c1 >= 128 || c2 >= 128 || c3 >= 128) {
                        break;
                    }
                    update(4, (c1 << '\b') | c0 | (c2 << 16) | (c3 << 24));
                    i += 4;
                }
                while (i < utf16Length) {
                    char c = input.charAt(i);
                    if (c < 128) {
                        update(1, c);
                    } else if (c < 2048) {
                        update(2, Murmur3_32HashFunction.charToTwoUtf8Bytes(c));
                    } else if (c < 55296 || c > 57343) {
                        update(3, Murmur3_32HashFunction.charToThreeUtf8Bytes(c));
                    } else {
                        int codePoint = Character.codePointAt(input, i);
                        if (codePoint == c) {
                            putBytes(input.subSequence(i, utf16Length).toString().getBytes(charset));
                            return this;
                        }
                        i++;
                        update(4, Murmur3_32HashFunction.codePointToFourUtf8Bytes(codePoint));
                    }
                    i++;
                }
                return this;
            }
            return super.putString(input, charset);
        }

        @Override // com.google.common.hash.Hasher
        public HashCode hash() {
            Preconditions.checkState(!this.isDone);
            this.isDone = true;
            int mixK1 = this.f211h1 ^ Murmur3_32HashFunction.mixK1((int) this.buffer);
            this.f211h1 = mixK1;
            return Murmur3_32HashFunction.fmix(mixK1, this.length);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static long codePointToFourUtf8Bytes(int codePoint) {
        return (((codePoint >>> 18) | 240) & 255) | ((((codePoint >>> 12) & 63) | 128) << 8) | ((((codePoint >>> 6) & 63) | 128) << 16) | (((codePoint & 63) | 128) << 24);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static long charToThreeUtf8Bytes(char c) {
        return (((c >>> '\f') | 480) & 255) | ((((c >>> 6) & 63) | 128) << 8) | (((c & '?') | 128) << 16);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static long charToTwoUtf8Bytes(char c) {
        return (((c >>> 6) | 960) & 255) | (((c & '?') | 128) << 8);
    }
}
