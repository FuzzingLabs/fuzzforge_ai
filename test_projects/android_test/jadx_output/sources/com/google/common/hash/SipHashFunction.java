package com.google.common.hash;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import java.io.Serializable;
import java.nio.ByteBuffer;

@Immutable
/* loaded from: classes.dex */
final class SipHashFunction extends AbstractHashFunction implements Serializable {
    static final HashFunction SIP_HASH_24 = new SipHashFunction(2, 4, 506097522914230528L, 1084818905618843912L);
    private static final long serialVersionUID = 0;

    /* renamed from: c */
    private final int f212c;

    /* renamed from: d */
    private final int f213d;

    /* renamed from: k0 */
    private final long f214k0;

    /* renamed from: k1 */
    private final long f215k1;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SipHashFunction(int c, int d, long k0, long k1) {
        Preconditions.checkArgument(c > 0, "The number of SipRound iterations (c=%s) during Compression must be positive.", c);
        Preconditions.checkArgument(d > 0, "The number of SipRound iterations (d=%s) during Finalization must be positive.", d);
        this.f212c = c;
        this.f213d = d;
        this.f214k0 = k0;
        this.f215k1 = k1;
    }

    @Override // com.google.common.hash.HashFunction
    public int bits() {
        return 64;
    }

    @Override // com.google.common.hash.HashFunction
    public Hasher newHasher() {
        return new SipHasher(this.f212c, this.f213d, this.f214k0, this.f215k1);
    }

    public String toString() {
        return "Hashing.sipHash" + this.f212c + "" + this.f213d + "(" + this.f214k0 + ", " + this.f215k1 + ")";
    }

    public boolean equals(Object object) {
        if (!(object instanceof SipHashFunction)) {
            return false;
        }
        SipHashFunction other = (SipHashFunction) object;
        return this.f212c == other.f212c && this.f213d == other.f213d && this.f214k0 == other.f214k0 && this.f215k1 == other.f215k1;
    }

    public int hashCode() {
        return (int) ((((getClass().hashCode() ^ this.f212c) ^ this.f213d) ^ this.f214k0) ^ this.f215k1);
    }

    /* loaded from: classes.dex */
    private static final class SipHasher extends AbstractStreamingHasher {
        private static final int CHUNK_SIZE = 8;

        /* renamed from: b */
        private long f216b;

        /* renamed from: c */
        private final int f217c;

        /* renamed from: d */
        private final int f218d;
        private long finalM;

        /* renamed from: v0 */
        private long f219v0;

        /* renamed from: v1 */
        private long f220v1;

        /* renamed from: v2 */
        private long f221v2;

        /* renamed from: v3 */
        private long f222v3;

        SipHasher(int c, int d, long k0, long k1) {
            super(8);
            this.f219v0 = 8317987319222330741L;
            this.f220v1 = 7237128888997146477L;
            this.f221v2 = 7816392313619706465L;
            this.f222v3 = 8387220255154660723L;
            this.f216b = 0L;
            this.finalM = 0L;
            this.f217c = c;
            this.f218d = d;
            this.f219v0 = 8317987319222330741L ^ k0;
            this.f220v1 = 7237128888997146477L ^ k1;
            this.f221v2 = 7816392313619706465L ^ k0;
            this.f222v3 = 8387220255154660723L ^ k1;
        }

        @Override // com.google.common.hash.AbstractStreamingHasher
        protected void process(ByteBuffer buffer) {
            this.f216b += 8;
            processM(buffer.getLong());
        }

        @Override // com.google.common.hash.AbstractStreamingHasher
        protected void processRemaining(ByteBuffer buffer) {
            this.f216b += buffer.remaining();
            int i = 0;
            while (buffer.hasRemaining()) {
                this.finalM ^= (buffer.get() & 255) << i;
                i += 8;
            }
        }

        @Override // com.google.common.hash.AbstractStreamingHasher
        public HashCode makeHash() {
            long j = this.finalM ^ (this.f216b << 56);
            this.finalM = j;
            processM(j);
            this.f221v2 ^= 255;
            sipRound(this.f218d);
            return HashCode.fromLong(((this.f219v0 ^ this.f220v1) ^ this.f221v2) ^ this.f222v3);
        }

        private void processM(long m) {
            this.f222v3 ^= m;
            sipRound(this.f217c);
            this.f219v0 ^= m;
        }

        private void sipRound(int iterations) {
            for (int i = 0; i < iterations; i++) {
                long j = this.f219v0;
                long j2 = this.f220v1;
                this.f219v0 = j + j2;
                this.f221v2 += this.f222v3;
                this.f220v1 = Long.rotateLeft(j2, 13);
                long rotateLeft = Long.rotateLeft(this.f222v3, 16);
                this.f222v3 = rotateLeft;
                long j3 = this.f220v1;
                long j4 = this.f219v0;
                this.f220v1 = j3 ^ j4;
                this.f222v3 = rotateLeft ^ this.f221v2;
                long rotateLeft2 = Long.rotateLeft(j4, 32);
                this.f219v0 = rotateLeft2;
                long j5 = this.f221v2;
                long j6 = this.f220v1;
                this.f221v2 = j5 + j6;
                this.f219v0 = rotateLeft2 + this.f222v3;
                this.f220v1 = Long.rotateLeft(j6, 17);
                long rotateLeft3 = Long.rotateLeft(this.f222v3, 21);
                this.f222v3 = rotateLeft3;
                long j7 = this.f220v1;
                long j8 = this.f221v2;
                this.f220v1 = j7 ^ j8;
                this.f222v3 = rotateLeft3 ^ this.f219v0;
                this.f221v2 = Long.rotateLeft(j8, 32);
            }
        }
    }
}
