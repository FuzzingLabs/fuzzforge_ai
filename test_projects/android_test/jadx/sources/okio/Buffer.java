package okio;

import androidx.constraintlayout.core.motion.utils.TypedValues;
import com.google.common.base.Ascii;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import kotlin.Deprecated;
import kotlin.DeprecationLevel;
import kotlin.Metadata;
import kotlin.ReplaceWith;
import kotlin.collections.ArraysKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.LongCompanionObject;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.text.Charsets;
import kotlin.text.Typography;
import okhttp3.internal.connection.RealConnection;
import okio.internal.BufferKt;

/* compiled from: Buffer.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000ª\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u001a\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0005\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\u0010\u0012\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\n\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0017\u0018\u00002\u00020\u00012\u00020\u00022\u00020\u00032\u00020\u0004:\u0002\u0090\u0001B\u0005¢\u0006\u0002\u0010\u0005J\b\u0010\u0006\u001a\u00020\u0000H\u0016J\u0006\u0010\u0011\u001a\u00020\u0012J\b\u0010\u0013\u001a\u00020\u0000H\u0016J\b\u0010\u0014\u001a\u00020\u0012H\u0016J\u0006\u0010\u0015\u001a\u00020\fJ\u0006\u0010\u0016\u001a\u00020\u0000J$\u0010\u0017\u001a\u00020\u00002\u0006\u0010\u0018\u001a\u00020\u00192\b\b\u0002\u0010\u001a\u001a\u00020\f2\b\b\u0002\u0010\u001b\u001a\u00020\fH\u0007J\u0018\u0010\u0017\u001a\u00020\u00002\u0006\u0010\u0018\u001a\u00020\u00002\b\b\u0002\u0010\u001a\u001a\u00020\fJ \u0010\u0017\u001a\u00020\u00002\u0006\u0010\u0018\u001a\u00020\u00002\b\b\u0002\u0010\u001a\u001a\u00020\f2\u0006\u0010\u001b\u001a\u00020\fJ\u0010\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\b\u0010 \u001a\u00020\u0000H\u0016J\b\u0010!\u001a\u00020\u0000H\u0016J\u0013\u0010\"\u001a\u00020#2\b\u0010$\u001a\u0004\u0018\u00010%H\u0096\u0002J\b\u0010&\u001a\u00020#H\u0016J\b\u0010'\u001a\u00020\u0012H\u0016J\u0016\u0010(\u001a\u00020)2\u0006\u0010*\u001a\u00020\fH\u0087\u0002¢\u0006\u0002\b+J\u0015\u0010+\u001a\u00020)2\u0006\u0010,\u001a\u00020\fH\u0007¢\u0006\u0002\b-J\b\u0010.\u001a\u00020/H\u0016J\u0018\u00100\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u00101\u001a\u00020\u001dH\u0002J\u000e\u00102\u001a\u00020\u001d2\u0006\u00101\u001a\u00020\u001dJ\u000e\u00103\u001a\u00020\u001d2\u0006\u00101\u001a\u00020\u001dJ\u000e\u00104\u001a\u00020\u001d2\u0006\u00101\u001a\u00020\u001dJ\u0010\u00105\u001a\u00020\f2\u0006\u00106\u001a\u00020)H\u0016J\u0018\u00105\u001a\u00020\f2\u0006\u00106\u001a\u00020)2\u0006\u00107\u001a\u00020\fH\u0016J \u00105\u001a\u00020\f2\u0006\u00106\u001a\u00020)2\u0006\u00107\u001a\u00020\f2\u0006\u00108\u001a\u00020\fH\u0016J\u0010\u00105\u001a\u00020\f2\u0006\u00109\u001a\u00020\u001dH\u0016J\u0018\u00105\u001a\u00020\f2\u0006\u00109\u001a\u00020\u001d2\u0006\u00107\u001a\u00020\fH\u0016J\u0010\u0010:\u001a\u00020\f2\u0006\u0010;\u001a\u00020\u001dH\u0016J\u0018\u0010:\u001a\u00020\f2\u0006\u0010;\u001a\u00020\u001d2\u0006\u00107\u001a\u00020\fH\u0016J\b\u0010<\u001a\u00020=H\u0016J\b\u0010>\u001a\u00020#H\u0016J\u0006\u0010?\u001a\u00020\u001dJ\b\u0010@\u001a\u00020\u0019H\u0016J\b\u0010A\u001a\u00020\u0001H\u0016J\u0018\u0010B\u001a\u00020#2\u0006\u0010\u001a\u001a\u00020\f2\u0006\u00109\u001a\u00020\u001dH\u0016J(\u0010B\u001a\u00020#2\u0006\u0010\u001a\u001a\u00020\f2\u0006\u00109\u001a\u00020\u001d2\u0006\u0010C\u001a\u00020/2\u0006\u0010\u001b\u001a\u00020/H\u0016J\u0010\u0010D\u001a\u00020/2\u0006\u0010E\u001a\u00020FH\u0016J\u0010\u0010D\u001a\u00020/2\u0006\u0010E\u001a\u00020GH\u0016J \u0010D\u001a\u00020/2\u0006\u0010E\u001a\u00020G2\u0006\u0010\u001a\u001a\u00020/2\u0006\u0010\u001b\u001a\u00020/H\u0016J\u0018\u0010D\u001a\u00020\f2\u0006\u0010E\u001a\u00020\u00002\u0006\u0010\u001b\u001a\u00020\fH\u0016J\u0010\u0010H\u001a\u00020\f2\u0006\u0010E\u001a\u00020IH\u0016J\u0012\u0010J\u001a\u00020K2\b\b\u0002\u0010L\u001a\u00020KH\u0007J\b\u0010M\u001a\u00020)H\u0016J\b\u0010N\u001a\u00020GH\u0016J\u0010\u0010N\u001a\u00020G2\u0006\u0010\u001b\u001a\u00020\fH\u0016J\b\u0010O\u001a\u00020\u001dH\u0016J\u0010\u0010O\u001a\u00020\u001d2\u0006\u0010\u001b\u001a\u00020\fH\u0016J\b\u0010P\u001a\u00020\fH\u0016J\u000e\u0010Q\u001a\u00020\u00002\u0006\u0010R\u001a\u00020=J\u0016\u0010Q\u001a\u00020\u00002\u0006\u0010R\u001a\u00020=2\u0006\u0010\u001b\u001a\u00020\fJ \u0010Q\u001a\u00020\u00122\u0006\u0010R\u001a\u00020=2\u0006\u0010\u001b\u001a\u00020\f2\u0006\u0010S\u001a\u00020#H\u0002J\u0010\u0010T\u001a\u00020\u00122\u0006\u0010E\u001a\u00020GH\u0016J\u0018\u0010T\u001a\u00020\u00122\u0006\u0010E\u001a\u00020\u00002\u0006\u0010\u001b\u001a\u00020\fH\u0016J\b\u0010U\u001a\u00020\fH\u0016J\b\u0010V\u001a\u00020/H\u0016J\b\u0010W\u001a\u00020/H\u0016J\b\u0010X\u001a\u00020\fH\u0016J\b\u0010Y\u001a\u00020\fH\u0016J\b\u0010Z\u001a\u00020[H\u0016J\b\u0010\\\u001a\u00020[H\u0016J\u0010\u0010]\u001a\u00020\u001f2\u0006\u0010^\u001a\u00020_H\u0016J\u0018\u0010]\u001a\u00020\u001f2\u0006\u0010\u001b\u001a\u00020\f2\u0006\u0010^\u001a\u00020_H\u0016J\u0012\u0010`\u001a\u00020K2\b\b\u0002\u0010L\u001a\u00020KH\u0007J\b\u0010a\u001a\u00020\u001fH\u0016J\u0010\u0010a\u001a\u00020\u001f2\u0006\u0010\u001b\u001a\u00020\fH\u0016J\b\u0010b\u001a\u00020/H\u0016J\n\u0010c\u001a\u0004\u0018\u00010\u001fH\u0016J\b\u0010d\u001a\u00020\u001fH\u0016J\u0010\u0010d\u001a\u00020\u001f2\u0006\u0010e\u001a\u00020\fH\u0016J\u0010\u0010f\u001a\u00020#2\u0006\u0010\u001b\u001a\u00020\fH\u0016J\u0010\u0010g\u001a\u00020\u00122\u0006\u0010\u001b\u001a\u00020\fH\u0016J\u0010\u0010h\u001a\u00020/2\u0006\u0010i\u001a\u00020jH\u0016J\u0006\u0010k\u001a\u00020\u001dJ\u0006\u0010l\u001a\u00020\u001dJ\u0006\u0010m\u001a\u00020\u001dJ\r\u0010\r\u001a\u00020\fH\u0007¢\u0006\u0002\bnJ\u0010\u0010o\u001a\u00020\u00122\u0006\u0010\u001b\u001a\u00020\fH\u0016J\u0006\u0010p\u001a\u00020\u001dJ\u000e\u0010p\u001a\u00020\u001d2\u0006\u0010\u001b\u001a\u00020/J\b\u0010q\u001a\u00020rH\u0016J\b\u0010s\u001a\u00020\u001fH\u0016J\u0015\u0010t\u001a\u00020\n2\u0006\u0010u\u001a\u00020/H\u0000¢\u0006\u0002\bvJ\u0010\u0010w\u001a\u00020/2\u0006\u0010x\u001a\u00020FH\u0016J\u0010\u0010w\u001a\u00020\u00002\u0006\u0010x\u001a\u00020GH\u0016J \u0010w\u001a\u00020\u00002\u0006\u0010x\u001a\u00020G2\u0006\u0010\u001a\u001a\u00020/2\u0006\u0010\u001b\u001a\u00020/H\u0016J\u0018\u0010w\u001a\u00020\u00122\u0006\u0010x\u001a\u00020\u00002\u0006\u0010\u001b\u001a\u00020\fH\u0016J\u0010\u0010w\u001a\u00020\u00002\u0006\u0010y\u001a\u00020\u001dH\u0016J \u0010w\u001a\u00020\u00002\u0006\u0010y\u001a\u00020\u001d2\u0006\u0010\u001a\u001a\u00020/2\u0006\u0010\u001b\u001a\u00020/H\u0016J\u0018\u0010w\u001a\u00020\u00002\u0006\u0010x\u001a\u00020z2\u0006\u0010\u001b\u001a\u00020\fH\u0016J\u0010\u0010{\u001a\u00020\f2\u0006\u0010x\u001a\u00020zH\u0016J\u0010\u0010|\u001a\u00020\u00002\u0006\u00106\u001a\u00020/H\u0016J\u0010\u0010}\u001a\u00020\u00002\u0006\u0010~\u001a\u00020\fH\u0016J\u0010\u0010\u007f\u001a\u00020\u00002\u0006\u0010~\u001a\u00020\fH\u0016J\u0012\u0010\u0080\u0001\u001a\u00020\u00002\u0007\u0010\u0081\u0001\u001a\u00020/H\u0016J\u0012\u0010\u0082\u0001\u001a\u00020\u00002\u0007\u0010\u0081\u0001\u001a\u00020/H\u0016J\u0011\u0010\u0083\u0001\u001a\u00020\u00002\u0006\u0010~\u001a\u00020\fH\u0016J\u0011\u0010\u0084\u0001\u001a\u00020\u00002\u0006\u0010~\u001a\u00020\fH\u0016J\u0012\u0010\u0085\u0001\u001a\u00020\u00002\u0007\u0010\u0086\u0001\u001a\u00020/H\u0016J\u0012\u0010\u0087\u0001\u001a\u00020\u00002\u0007\u0010\u0086\u0001\u001a\u00020/H\u0016J\u001a\u0010\u0088\u0001\u001a\u00020\u00002\u0007\u0010\u0089\u0001\u001a\u00020\u001f2\u0006\u0010^\u001a\u00020_H\u0016J,\u0010\u0088\u0001\u001a\u00020\u00002\u0007\u0010\u0089\u0001\u001a\u00020\u001f2\u0007\u0010\u008a\u0001\u001a\u00020/2\u0007\u0010\u008b\u0001\u001a\u00020/2\u0006\u0010^\u001a\u00020_H\u0016J\u001b\u0010\u008c\u0001\u001a\u00020\u00002\u0006\u0010\u0018\u001a\u00020\u00192\b\b\u0002\u0010\u001b\u001a\u00020\fH\u0007J\u0012\u0010\u008d\u0001\u001a\u00020\u00002\u0007\u0010\u0089\u0001\u001a\u00020\u001fH\u0016J$\u0010\u008d\u0001\u001a\u00020\u00002\u0007\u0010\u0089\u0001\u001a\u00020\u001f2\u0007\u0010\u008a\u0001\u001a\u00020/2\u0007\u0010\u008b\u0001\u001a\u00020/H\u0016J\u0012\u0010\u008e\u0001\u001a\u00020\u00002\u0007\u0010\u008f\u0001\u001a\u00020/H\u0016R\u0014\u0010\u0006\u001a\u00020\u00008VX\u0096\u0004¢\u0006\u0006\u001a\u0004\b\u0007\u0010\bR\u0014\u0010\t\u001a\u0004\u0018\u00010\n8\u0000@\u0000X\u0081\u000e¢\u0006\u0002\n\u0000R&\u0010\r\u001a\u00020\f2\u0006\u0010\u000b\u001a\u00020\f8G@@X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010¨\u0006\u0091\u0001"}, m238d2 = {"Lokio/Buffer;", "Lokio/BufferedSource;", "Lokio/BufferedSink;", "", "Ljava/nio/channels/ByteChannel;", "()V", "buffer", "getBuffer", "()Lokio/Buffer;", "head", "Lokio/Segment;", "<set-?>", "", "size", "()J", "setSize$okio", "(J)V", "clear", "", "clone", "close", "completeSegmentByteCount", "copy", "copyTo", "out", "Ljava/io/OutputStream;", TypedValues.Cycle.S_WAVE_OFFSET, "byteCount", "digest", "Lokio/ByteString;", "algorithm", "", "emit", "emitCompleteSegments", "equals", "", "other", "", "exhausted", "flush", "get", "", "pos", "getByte", "index", "-deprecated_getByte", "hashCode", "", "hmac", "key", "hmacSha1", "hmacSha256", "hmacSha512", "indexOf", "b", "fromIndex", "toIndex", "bytes", "indexOfElement", "targetBytes", "inputStream", "Ljava/io/InputStream;", "isOpen", "md5", "outputStream", "peek", "rangeEquals", "bytesOffset", "read", "sink", "Ljava/nio/ByteBuffer;", "", "readAll", "Lokio/Sink;", "readAndWriteUnsafe", "Lokio/Buffer$UnsafeCursor;", "unsafeCursor", "readByte", "readByteArray", "readByteString", "readDecimalLong", "readFrom", "input", "forever", "readFully", "readHexadecimalUnsignedLong", "readInt", "readIntLe", "readLong", "readLongLe", "readShort", "", "readShortLe", "readString", "charset", "Ljava/nio/charset/Charset;", "readUnsafe", "readUtf8", "readUtf8CodePoint", "readUtf8Line", "readUtf8LineStrict", "limit", "request", "require", "select", "options", "Lokio/Options;", "sha1", "sha256", "sha512", "-deprecated_size", "skip", "snapshot", "timeout", "Lokio/Timeout;", "toString", "writableSegment", "minimumCapacity", "writableSegment$okio", "write", "source", "byteString", "Lokio/Source;", "writeAll", "writeByte", "writeDecimalLong", "v", "writeHexadecimalUnsignedLong", "writeInt", "i", "writeIntLe", "writeLong", "writeLongLe", "writeShort", "s", "writeShortLe", "writeString", TypedValues.Custom.S_STRING, "beginIndex", "endIndex", "writeTo", "writeUtf8", "writeUtf8CodePoint", "codePoint", "UnsafeCursor", "okio"}, m239k = 1, m240mv = {1, 4, 0})
/* loaded from: classes11.dex */
public final class Buffer implements BufferedSource, BufferedSink, Cloneable, ByteChannel {
    public Segment head;
    private long size;

    public static /* synthetic */ Buffer copyTo$default(Buffer buffer, Buffer buffer2, long j, int i, Object obj) {
        if ((i & 2) != 0) {
            j = 0;
        }
        return buffer.copyTo(buffer2, j);
    }

    public static /* synthetic */ Buffer copyTo$default(Buffer buffer, Buffer buffer2, long j, long j2, int i, Object obj) {
        if ((i & 2) != 0) {
            j = 0;
        }
        return buffer.copyTo(buffer2, j, j2);
    }

    public final Buffer copyTo(OutputStream outputStream) throws IOException {
        return copyTo$default(this, outputStream, 0L, 0L, 6, (Object) null);
    }

    public final Buffer copyTo(OutputStream outputStream, long j) throws IOException {
        return copyTo$default(this, outputStream, j, 0L, 4, (Object) null);
    }

    public final UnsafeCursor readAndWriteUnsafe() {
        return readAndWriteUnsafe$default(this, null, 1, null);
    }

    public final UnsafeCursor readUnsafe() {
        return readUnsafe$default(this, null, 1, null);
    }

    public final Buffer writeTo(OutputStream outputStream) throws IOException {
        return writeTo$default(this, outputStream, 0L, 2, null);
    }

    public final void setSize$okio(long j) {
        this.size = j;
    }

    public final long size() {
        return this.size;
    }

    @Override // okio.BufferedSource, okio.BufferedSink
    /* renamed from: buffer */
    public Buffer getBufferField() {
        return this;
    }

    @Override // okio.BufferedSource, okio.BufferedSink
    public Buffer getBuffer() {
        return this;
    }

    @Override // okio.BufferedSink
    public OutputStream outputStream() {
        return new OutputStream() { // from class: okio.Buffer$outputStream$1
            @Override // java.io.OutputStream
            public void write(int b) {
                Buffer.this.writeByte(b);
            }

            @Override // java.io.OutputStream
            public void write(byte[] data, int offset, int byteCount) {
                Intrinsics.checkNotNullParameter(data, "data");
                Buffer.this.write(data, offset, byteCount);
            }

            @Override // java.io.OutputStream, java.io.Flushable
            public void flush() {
            }

            @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
            public void close() {
            }

            public String toString() {
                return Buffer.this + ".outputStream()";
            }
        };
    }

    @Override // okio.BufferedSink
    public Buffer emitCompleteSegments() {
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer emit() {
        return this;
    }

    @Override // okio.BufferedSource
    public boolean exhausted() {
        return this.size == 0;
    }

    @Override // okio.BufferedSource
    public void require(long byteCount) throws EOFException {
        if (this.size < byteCount) {
            throw new EOFException();
        }
    }

    @Override // okio.BufferedSource
    public boolean request(long byteCount) {
        return this.size >= byteCount;
    }

    @Override // okio.BufferedSource
    public BufferedSource peek() {
        return Okio.buffer(new PeekSource(this));
    }

    @Override // okio.BufferedSource
    public InputStream inputStream() {
        return new InputStream() { // from class: okio.Buffer$inputStream$1
            @Override // java.io.InputStream
            public int read() {
                if (Buffer.this.size() > 0) {
                    byte $this$and$iv = Buffer.this.readByte();
                    return $this$and$iv & 255;
                }
                return -1;
            }

            @Override // java.io.InputStream
            public int read(byte[] sink, int offset, int byteCount) {
                Intrinsics.checkNotNullParameter(sink, "sink");
                return Buffer.this.read(sink, offset, byteCount);
            }

            @Override // java.io.InputStream
            public int available() {
                long a$iv = Buffer.this.size();
                return (int) Math.min(a$iv, Integer.MAX_VALUE);
            }

            @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
            public void close() {
            }

            public String toString() {
                return Buffer.this + ".inputStream()";
            }
        };
    }

    public static /* synthetic */ Buffer copyTo$default(Buffer buffer, OutputStream outputStream, long j, long j2, int i, Object obj) throws IOException {
        long j3 = (i & 2) != 0 ? 0L : j;
        return buffer.copyTo(outputStream, j3, (i & 4) != 0 ? buffer.size - j3 : j2);
    }

    public final Buffer copyTo(OutputStream out, long offset, long byteCount) throws IOException {
        Intrinsics.checkNotNullParameter(out, "out");
        long offset2 = offset;
        long byteCount2 = byteCount;
        Util.checkOffsetAndCount(this.size, offset2, byteCount2);
        if (byteCount2 == 0) {
            return this;
        }
        Segment s = this.head;
        while (true) {
            Intrinsics.checkNotNull(s);
            if (offset2 < s.limit - s.pos) {
                break;
            }
            offset2 -= s.limit - s.pos;
            s = s.next;
        }
        while (byteCount2 > 0) {
            Intrinsics.checkNotNull(s);
            int pos = (int) (s.pos + offset2);
            int a$iv = (int) Math.min(s.limit - pos, byteCount2);
            out.write(s.data, pos, a$iv);
            byteCount2 -= a$iv;
            offset2 = 0;
            s = s.next;
        }
        return this;
    }

    public final Buffer copyTo(Buffer out, long offset, long byteCount) {
        Intrinsics.checkNotNullParameter(out, "out");
        long offset$iv = offset;
        long byteCount$iv = byteCount;
        Util.checkOffsetAndCount(size(), offset$iv, byteCount$iv);
        if (byteCount$iv != 0) {
            out.setSize$okio(out.size() + byteCount$iv);
            Segment s$iv = this.head;
            while (true) {
                Intrinsics.checkNotNull(s$iv);
                if (offset$iv < s$iv.limit - s$iv.pos) {
                    break;
                }
                offset$iv -= s$iv.limit - s$iv.pos;
                s$iv = s$iv.next;
            }
            while (byteCount$iv > 0) {
                Intrinsics.checkNotNull(s$iv);
                Segment copy$iv = s$iv.sharedCopy();
                copy$iv.pos += (int) offset$iv;
                copy$iv.limit = Math.min(copy$iv.pos + ((int) byteCount$iv), copy$iv.limit);
                Segment segment = out.head;
                if (segment == null) {
                    copy$iv.prev = copy$iv;
                    copy$iv.next = copy$iv.prev;
                    out.head = copy$iv.next;
                } else {
                    Intrinsics.checkNotNull(segment);
                    Segment segment2 = segment.prev;
                    Intrinsics.checkNotNull(segment2);
                    segment2.push(copy$iv);
                }
                byteCount$iv -= copy$iv.limit - copy$iv.pos;
                offset$iv = 0;
                s$iv = s$iv.next;
            }
        }
        return this;
    }

    public final Buffer copyTo(Buffer out, long offset) {
        Intrinsics.checkNotNullParameter(out, "out");
        return copyTo(out, offset, this.size - offset);
    }

    public static /* synthetic */ Buffer writeTo$default(Buffer buffer, OutputStream outputStream, long j, int i, Object obj) throws IOException {
        if ((i & 2) != 0) {
            j = buffer.size;
        }
        return buffer.writeTo(outputStream, j);
    }

    public final Buffer writeTo(OutputStream out, long byteCount) throws IOException {
        Intrinsics.checkNotNullParameter(out, "out");
        long byteCount2 = byteCount;
        Util.checkOffsetAndCount(this.size, 0L, byteCount2);
        Segment s = this.head;
        while (byteCount2 > 0) {
            Intrinsics.checkNotNull(s);
            int b$iv = (int) Math.min(byteCount2, s.limit - s.pos);
            out.write(s.data, s.pos, b$iv);
            s.pos += b$iv;
            this.size -= b$iv;
            byteCount2 -= b$iv;
            if (s.pos == s.limit) {
                Segment toRecycle = s;
                s = toRecycle.pop();
                this.head = s;
                SegmentPool.recycle(toRecycle);
            }
        }
        return this;
    }

    public final Buffer readFrom(InputStream input) throws IOException {
        Intrinsics.checkNotNullParameter(input, "input");
        readFrom(input, LongCompanionObject.MAX_VALUE, true);
        return this;
    }

    public final Buffer readFrom(InputStream input, long byteCount) throws IOException {
        Intrinsics.checkNotNullParameter(input, "input");
        if (!(byteCount >= 0)) {
            throw new IllegalArgumentException(("byteCount < 0: " + byteCount).toString());
        }
        readFrom(input, byteCount, false);
        return this;
    }

    private final void readFrom(InputStream input, long byteCount, boolean forever) throws IOException {
        long byteCount2 = byteCount;
        while (true) {
            if (byteCount2 > 0 || forever) {
                Segment tail = writableSegment$okio(1);
                int b$iv = 8192 - tail.limit;
                int bytesRead = input.read(tail.data, tail.limit, (int) Math.min(byteCount2, b$iv));
                if (bytesRead == -1) {
                    if (tail.pos == tail.limit) {
                        this.head = tail.pop();
                        SegmentPool.recycle(tail);
                    }
                    if (!forever) {
                        throw new EOFException();
                    }
                    return;
                }
                tail.limit += bytesRead;
                this.size += bytesRead;
                byteCount2 -= bytesRead;
            } else {
                return;
            }
        }
    }

    public final long completeSegmentByteCount() {
        long result$iv = size();
        if (result$iv == 0) {
            return 0L;
        }
        Segment segment = this.head;
        Intrinsics.checkNotNull(segment);
        Segment tail$iv = segment.prev;
        Intrinsics.checkNotNull(tail$iv);
        if (tail$iv.limit < 8192 && tail$iv.owner) {
            result$iv -= tail$iv.limit - tail$iv.pos;
        }
        return result$iv;
    }

    @Override // okio.BufferedSource
    public byte readByte() throws EOFException {
        if (size() == 0) {
            throw new EOFException();
        }
        Segment segment$iv = this.head;
        Intrinsics.checkNotNull(segment$iv);
        int pos$iv = segment$iv.pos;
        int limit$iv = segment$iv.limit;
        byte[] data$iv = segment$iv.data;
        int pos$iv2 = pos$iv + 1;
        byte b$iv = data$iv[pos$iv];
        setSize$okio(size() - 1);
        if (pos$iv2 == limit$iv) {
            this.head = segment$iv.pop();
            SegmentPool.recycle(segment$iv);
        } else {
            segment$iv.pos = pos$iv2;
        }
        return b$iv;
    }

    public final byte getByte(long pos) {
        Util.checkOffsetAndCount(size(), pos, 1L);
        Segment s$iv$iv = this.head;
        if (s$iv$iv == null) {
            Segment s$iv = (Segment) null;
            Intrinsics.checkNotNull(s$iv);
            return s$iv.data[(int) ((s$iv.pos + pos) - (-1))];
        }
        if (size() - pos < pos) {
            long offset$iv$iv = size();
            while (offset$iv$iv > pos) {
                Segment segment = s$iv$iv.prev;
                Intrinsics.checkNotNull(segment);
                s$iv$iv = segment;
                offset$iv$iv -= s$iv$iv.limit - s$iv$iv.pos;
            }
            Segment s$iv2 = s$iv$iv;
            long offset$iv = offset$iv$iv;
            Intrinsics.checkNotNull(s$iv2);
            return s$iv2.data[(int) ((s$iv2.pos + pos) - offset$iv)];
        }
        long offset$iv$iv2 = 0;
        while (true) {
            long nextOffset$iv$iv = (s$iv$iv.limit - s$iv$iv.pos) + offset$iv$iv2;
            if (nextOffset$iv$iv > pos) {
                Segment s$iv3 = s$iv$iv;
                long offset$iv2 = offset$iv$iv2;
                Intrinsics.checkNotNull(s$iv3);
                return s$iv3.data[(int) ((s$iv3.pos + pos) - offset$iv2)];
            }
            Segment s$iv4 = s$iv$iv.next;
            Intrinsics.checkNotNull(s$iv4);
            s$iv$iv = s$iv4;
            offset$iv$iv2 = nextOffset$iv$iv;
        }
    }

    @Override // okio.BufferedSource
    public short readShort() throws EOFException {
        if (size() < 2) {
            throw new EOFException();
        }
        Segment segment$iv = this.head;
        Intrinsics.checkNotNull(segment$iv);
        int pos$iv = segment$iv.pos;
        int limit$iv = segment$iv.limit;
        if (limit$iv - pos$iv < 2) {
            byte $this$and$iv$iv = readByte();
            byte $this$and$iv$iv2 = readByte();
            int s$iv = (($this$and$iv$iv & 255) << 8) | ($this$and$iv$iv2 & 255);
            return (short) s$iv;
        }
        byte[] data$iv = segment$iv.data;
        int pos$iv2 = pos$iv + 1;
        byte $this$and$iv$iv3 = data$iv[pos$iv];
        int pos$iv3 = pos$iv2 + 1;
        byte $this$and$iv$iv4 = data$iv[pos$iv2];
        int s$iv2 = (($this$and$iv$iv3 & 255) << 8) | ($this$and$iv$iv4 & 255);
        setSize$okio(size() - 2);
        if (pos$iv3 == limit$iv) {
            this.head = segment$iv.pop();
            SegmentPool.recycle(segment$iv);
        } else {
            segment$iv.pos = pos$iv3;
        }
        return (short) s$iv2;
    }

    @Override // okio.BufferedSource
    public int readInt() throws EOFException {
        if (size() < 4) {
            throw new EOFException();
        }
        Segment segment$iv = this.head;
        Intrinsics.checkNotNull(segment$iv);
        int pos$iv = segment$iv.pos;
        int limit$iv = segment$iv.limit;
        if (limit$iv - pos$iv < 4) {
            byte $this$and$iv$iv = readByte();
            byte $this$and$iv$iv2 = readByte();
            int i = (($this$and$iv$iv & 255) << 24) | (($this$and$iv$iv2 & 255) << 16);
            byte $this$and$iv$iv3 = readByte();
            int i2 = i | (($this$and$iv$iv3 & 255) << 8);
            byte $this$and$iv$iv4 = readByte();
            return i2 | ($this$and$iv$iv4 & 255);
        }
        byte[] data$iv = segment$iv.data;
        int pos$iv2 = pos$iv + 1;
        byte $this$and$iv$iv5 = data$iv[pos$iv];
        int pos$iv3 = pos$iv2 + 1;
        byte $this$and$iv$iv6 = data$iv[pos$iv2];
        int i3 = (($this$and$iv$iv5 & 255) << 24) | (($this$and$iv$iv6 & 255) << 16);
        int pos$iv4 = pos$iv3 + 1;
        byte $this$and$iv$iv7 = data$iv[pos$iv3];
        int i4 = i3 | (($this$and$iv$iv7 & 255) << 8);
        int pos$iv5 = pos$iv4 + 1;
        byte $this$and$iv$iv8 = data$iv[pos$iv4];
        int i$iv = i4 | ($this$and$iv$iv8 & 255);
        setSize$okio(size() - 4);
        if (pos$iv5 == limit$iv) {
            this.head = segment$iv.pop();
            SegmentPool.recycle(segment$iv);
        } else {
            segment$iv.pos = pos$iv5;
        }
        return i$iv;
    }

    @Override // okio.BufferedSource
    public long readLong() throws EOFException {
        if (size() < 8) {
            throw new EOFException();
        }
        Segment segment$iv = this.head;
        Intrinsics.checkNotNull(segment$iv);
        int pos$iv = segment$iv.pos;
        int limit$iv = segment$iv.limit;
        if (limit$iv - pos$iv < 8) {
            int $this$and$iv$iv = readInt();
            int $this$and$iv$iv2 = readInt();
            return (($this$and$iv$iv & 4294967295L) << 32) | ($this$and$iv$iv2 & 4294967295L);
        }
        byte[] data$iv = segment$iv.data;
        int pos$iv2 = pos$iv + 1;
        byte $this$and$iv$iv3 = data$iv[pos$iv];
        long other$iv$iv = 255 & $this$and$iv$iv3;
        int pos$iv3 = pos$iv2 + 1;
        byte $this$and$iv$iv4 = data$iv[pos$iv2];
        long j = (($this$and$iv$iv4 & 255) << 48) | (other$iv$iv << 56);
        int pos$iv4 = pos$iv3 + 1;
        byte $this$and$iv$iv5 = data$iv[pos$iv3];
        long other$iv$iv2 = 255 & $this$and$iv$iv5;
        int pos$iv5 = pos$iv4 + 1;
        byte $this$and$iv$iv6 = data$iv[pos$iv4];
        long j2 = j | (other$iv$iv2 << 40) | (($this$and$iv$iv6 & 255) << 32);
        int pos$iv6 = pos$iv5 + 1;
        byte $this$and$iv$iv7 = data$iv[pos$iv5];
        long other$iv$iv3 = 255 & $this$and$iv$iv7;
        int pos$iv7 = pos$iv6 + 1;
        byte $this$and$iv$iv8 = data$iv[pos$iv6];
        long j3 = j2 | (other$iv$iv3 << 24) | (($this$and$iv$iv8 & 255) << 16);
        int pos$iv8 = pos$iv7 + 1;
        byte $this$and$iv$iv9 = data$iv[pos$iv7];
        long other$iv$iv4 = 255 & $this$and$iv$iv9;
        int pos$iv9 = pos$iv8 + 1;
        byte $this$and$iv$iv10 = data$iv[pos$iv8];
        long v$iv = j3 | (other$iv$iv4 << 8) | ($this$and$iv$iv10 & 255);
        setSize$okio(size() - 8);
        if (pos$iv9 == limit$iv) {
            this.head = segment$iv.pop();
            SegmentPool.recycle(segment$iv);
        } else {
            segment$iv.pos = pos$iv9;
        }
        return v$iv;
    }

    @Override // okio.BufferedSource
    public short readShortLe() throws EOFException {
        return Util.reverseBytes(readShort());
    }

    @Override // okio.BufferedSource
    public int readIntLe() throws EOFException {
        return Util.reverseBytes(readInt());
    }

    @Override // okio.BufferedSource
    public long readLongLe() throws EOFException {
        return Util.reverseBytes(readLong());
    }

    /* JADX WARN: Code restructure failed: missing block: B:44:0x00ee, code lost:
    
        r1.setSize$okio(r1.size() - r4);
     */
    /* JADX WARN: Code restructure failed: missing block: B:45:0x00f7, code lost:
    
        if (r5 == false) goto L48;
     */
    /* JADX WARN: Code restructure failed: missing block: B:47:0x00fc, code lost:
    
        return -r2;
     */
    /* JADX WARN: Code restructure failed: missing block: B:48:?, code lost:
    
        return r2;
     */
    /* JADX WARN: Removed duplicated region for block: B:38:0x00d3  */
    /* JADX WARN: Removed duplicated region for block: B:50:0x00de  */
    @Override // okio.BufferedSource
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public long readDecimalLong() throws java.io.EOFException {
        /*
            Method dump skipped, instructions count: 264
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: okio.Buffer.readDecimalLong():long");
    }

    /* JADX WARN: Removed duplicated region for block: B:32:0x00aa  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x00b9  */
    /* JADX WARN: Removed duplicated region for block: B:40:0x00bd A[EDGE_INSN: B:40:0x00bd->B:37:0x00bd BREAK  A[LOOP:0: B:4:0x0011->B:39:?], SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:41:0x00b4  */
    @Override // okio.BufferedSource
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public long readHexadecimalUnsignedLong() throws java.io.EOFException {
        /*
            r16 = this;
            r0 = r16
            r1 = 0
            long r2 = r0.size()
            r4 = 0
            int r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r2 == 0) goto Lc8
            r2 = 0
            r6 = 0
            r7 = 0
        L11:
            okio.Segment r8 = r0.head
            kotlin.jvm.internal.Intrinsics.checkNotNull(r8)
            byte[] r9 = r8.data
            int r10 = r8.pos
            int r11 = r8.limit
        L1d:
            if (r10 >= r11) goto La8
            r12 = 0
            r13 = r9[r10]
            r14 = 48
            byte r14 = (byte) r14
            if (r13 < r14) goto L2f
            r15 = 57
            byte r15 = (byte) r15
            if (r13 > r15) goto L2f
            int r12 = r13 - r14
            goto L4c
        L2f:
            r14 = 97
            byte r14 = (byte) r14
            if (r13 < r14) goto L3e
            r15 = 102(0x66, float:1.43E-43)
            byte r15 = (byte) r15
            if (r13 > r15) goto L3e
            int r14 = r13 - r14
            int r12 = r14 + 10
            goto L4c
        L3e:
            r14 = 65
            byte r14 = (byte) r14
            if (r13 < r14) goto L87
            r15 = 70
            byte r15 = (byte) r15
            if (r13 > r15) goto L87
            int r14 = r13 - r14
            int r12 = r14 + 10
        L4c:
            r14 = -1152921504606846976(0xf000000000000000, double:-3.105036184601418E231)
            long r14 = r14 & r2
            int r14 = (r14 > r4 ? 1 : (r14 == r4 ? 0 : -1))
            if (r14 != 0) goto L5d
            r14 = 4
            long r2 = r2 << r14
            long r14 = (long) r12
            long r2 = r2 | r14
            int r10 = r10 + 1
            int r6 = r6 + 1
            goto L1d
        L5d:
            okio.Buffer r4 = new okio.Buffer
            r4.<init>()
            okio.Buffer r4 = r4.writeHexadecimalUnsignedLong(r2)
            okio.Buffer r4 = r4.writeByte(r13)
            java.lang.NumberFormatException r5 = new java.lang.NumberFormatException
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            java.lang.String r15 = "Number too large: "
            r14.append(r15)
            java.lang.String r15 = r4.readUtf8()
            r14.append(r15)
            java.lang.String r14 = r14.toString()
            r5.<init>(r14)
            java.lang.Throwable r5 = (java.lang.Throwable) r5
            throw r5
        L87:
            if (r6 == 0) goto L8b
            r7 = 1
            goto La8
        L8b:
            java.lang.NumberFormatException r4 = new java.lang.NumberFormatException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r14 = "Expected leading [0-9a-fA-F] character but was 0x"
            r5.append(r14)
            java.lang.String r14 = okio.Util.toHexString(r13)
            r5.append(r14)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5)
            java.lang.Throwable r4 = (java.lang.Throwable) r4
            throw r4
        La8:
            if (r10 != r11) goto Lb4
            okio.Segment r12 = r8.pop()
            r0.head = r12
            okio.SegmentPool.recycle(r8)
            goto Lb6
        Lb4:
            r8.pos = r10
        Lb6:
            if (r7 != 0) goto Lbd
            okio.Segment r12 = r0.head
            if (r12 != 0) goto L11
        Lbd:
            long r4 = r0.size()
            long r8 = (long) r6
            long r4 = r4 - r8
            r0.setSize$okio(r4)
            return r2
        Lc8:
            java.io.EOFException r2 = new java.io.EOFException
            r2.<init>()
            java.lang.Throwable r2 = (java.lang.Throwable) r2
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: okio.Buffer.readHexadecimalUnsignedLong():long");
    }

    @Override // okio.BufferedSource
    public ByteString readByteString() {
        return readByteString(size());
    }

    @Override // okio.BufferedSource
    public ByteString readByteString(long byteCount) throws EOFException {
        if (!(byteCount >= 0 && byteCount <= ((long) Integer.MAX_VALUE))) {
            throw new IllegalArgumentException(("byteCount: " + byteCount).toString());
        }
        if (size() < byteCount) {
            throw new EOFException();
        }
        if (byteCount >= 4096) {
            ByteString snapshot = snapshot((int) byteCount);
            skip(byteCount);
            return snapshot;
        }
        return new ByteString(readByteArray(byteCount));
    }

    @Override // okio.BufferedSource
    public int select(Options options) {
        Intrinsics.checkNotNullParameter(options, "options");
        int index$iv = BufferKt.selectPrefix$default(this, options, false, 2, null);
        if (index$iv == -1) {
            return -1;
        }
        int selectedSize$iv = options.getByteStrings()[index$iv].size();
        skip(selectedSize$iv);
        return index$iv;
    }

    @Override // okio.BufferedSource
    public void readFully(Buffer sink, long byteCount) throws EOFException {
        Intrinsics.checkNotNullParameter(sink, "sink");
        if (size() < byteCount) {
            sink.write(this, size());
            throw new EOFException();
        }
        sink.write(this, byteCount);
    }

    @Override // okio.BufferedSource
    public long readAll(Sink sink) throws IOException {
        Intrinsics.checkNotNullParameter(sink, "sink");
        long byteCount$iv = size();
        if (byteCount$iv > 0) {
            sink.write(this, byteCount$iv);
        }
        return byteCount$iv;
    }

    @Override // okio.BufferedSource
    public String readUtf8() {
        return readString(this.size, Charsets.UTF_8);
    }

    @Override // okio.BufferedSource
    public String readUtf8(long byteCount) throws EOFException {
        return readString(byteCount, Charsets.UTF_8);
    }

    @Override // okio.BufferedSource
    public String readString(Charset charset) {
        Intrinsics.checkNotNullParameter(charset, "charset");
        return readString(this.size, charset);
    }

    @Override // okio.BufferedSource
    public String readString(long byteCount, Charset charset) throws EOFException {
        Intrinsics.checkNotNullParameter(charset, "charset");
        if (!(byteCount >= 0 && byteCount <= ((long) Integer.MAX_VALUE))) {
            throw new IllegalArgumentException(("byteCount: " + byteCount).toString());
        }
        if (this.size < byteCount) {
            throw new EOFException();
        }
        if (byteCount == 0) {
            return "";
        }
        Segment s = this.head;
        Intrinsics.checkNotNull(s);
        if (s.pos + byteCount > s.limit) {
            return new String(readByteArray(byteCount), charset);
        }
        String result = new String(s.data, s.pos, (int) byteCount, charset);
        s.pos += (int) byteCount;
        this.size -= byteCount;
        if (s.pos == s.limit) {
            this.head = s.pop();
            SegmentPool.recycle(s);
        }
        return result;
    }

    @Override // okio.BufferedSource
    public String readUtf8Line() throws EOFException {
        long newline$iv = indexOf((byte) 10);
        if (newline$iv != -1) {
            return BufferKt.readUtf8Line(this, newline$iv);
        }
        if (size() != 0) {
            return readUtf8(size());
        }
        return null;
    }

    @Override // okio.BufferedSource
    public String readUtf8LineStrict() throws EOFException {
        return readUtf8LineStrict(LongCompanionObject.MAX_VALUE);
    }

    @Override // okio.BufferedSource
    public String readUtf8LineStrict(long limit) throws EOFException {
        if (!(limit >= 0)) {
            throw new IllegalArgumentException(("limit < 0: " + limit).toString());
        }
        long j = LongCompanionObject.MAX_VALUE;
        if (limit != LongCompanionObject.MAX_VALUE) {
            j = limit + 1;
        }
        long scanLength$iv = j;
        byte b = (byte) 10;
        long newline$iv = indexOf(b, 0L, scanLength$iv);
        if (newline$iv != -1) {
            return BufferKt.readUtf8Line(this, newline$iv);
        }
        if (scanLength$iv < size() && getByte(scanLength$iv - 1) == ((byte) 13) && getByte(scanLength$iv) == b) {
            return BufferKt.readUtf8Line(this, scanLength$iv);
        }
        Buffer data$iv = new Buffer();
        long b$iv$iv = size();
        copyTo(data$iv, 0L, Math.min(32, b$iv$iv));
        throw new EOFException("\\n not found: limit=" + Math.min(size(), limit) + " content=" + data$iv.readByteString().hex() + Typography.ellipsis);
    }

    @Override // okio.BufferedSource
    public int readUtf8CodePoint() throws EOFException {
        int codePoint$iv;
        int byteCount$iv;
        int min$iv;
        if (size() == 0) {
            throw new EOFException();
        }
        byte b0$iv = getByte(0L);
        int other$iv$iv = 128 & b0$iv;
        if (other$iv$iv == 0) {
            codePoint$iv = b0$iv & Byte.MAX_VALUE;
            byteCount$iv = 1;
            min$iv = 0;
        } else {
            int other$iv$iv2 = 224 & b0$iv;
            if (other$iv$iv2 == 192) {
                codePoint$iv = b0$iv & Ascii.f169US;
                byteCount$iv = 2;
                min$iv = 128;
            } else {
                int other$iv$iv3 = 240 & b0$iv;
                if (other$iv$iv3 == 224) {
                    codePoint$iv = b0$iv & Ascii.f166SI;
                    byteCount$iv = 3;
                    min$iv = 2048;
                } else {
                    int other$iv$iv4 = 248 & b0$iv;
                    if (other$iv$iv4 != 240) {
                        skip(1L);
                        return Utf8.REPLACEMENT_CODE_POINT;
                    }
                    codePoint$iv = b0$iv & 7;
                    byteCount$iv = 4;
                    min$iv = 65536;
                }
            }
        }
        if (size() < byteCount$iv) {
            throw new EOFException("size < " + byteCount$iv + ": " + size() + " (to read code point prefixed 0x" + Util.toHexString(b0$iv) + ')');
        }
        for (int i$iv = 1; i$iv < byteCount$iv; i$iv++) {
            int b$iv = getByte(i$iv);
            int other$iv$iv5 = 192 & b$iv;
            if (other$iv$iv5 != 128) {
                skip(i$iv);
                return Utf8.REPLACEMENT_CODE_POINT;
            }
            int other$iv$iv6 = 63 & b$iv;
            codePoint$iv = (codePoint$iv << 6) | other$iv$iv6;
        }
        skip(byteCount$iv);
        return codePoint$iv > 1114111 ? Utf8.REPLACEMENT_CODE_POINT : ((55296 <= codePoint$iv && 57343 >= codePoint$iv) || codePoint$iv < min$iv) ? Utf8.REPLACEMENT_CODE_POINT : codePoint$iv;
    }

    @Override // okio.BufferedSource
    public byte[] readByteArray() {
        return readByteArray(size());
    }

    @Override // okio.BufferedSource
    public byte[] readByteArray(long byteCount) throws EOFException {
        if (!(byteCount >= 0 && byteCount <= ((long) Integer.MAX_VALUE))) {
            throw new IllegalArgumentException(("byteCount: " + byteCount).toString());
        }
        if (size() < byteCount) {
            throw new EOFException();
        }
        byte[] result$iv = new byte[(int) byteCount];
        readFully(result$iv);
        return result$iv;
    }

    @Override // okio.BufferedSource
    public int read(byte[] sink) {
        Intrinsics.checkNotNullParameter(sink, "sink");
        return read(sink, 0, sink.length);
    }

    @Override // okio.BufferedSource
    public void readFully(byte[] sink) throws EOFException {
        Intrinsics.checkNotNullParameter(sink, "sink");
        int offset$iv = 0;
        while (offset$iv < sink.length) {
            int read$iv = read(sink, offset$iv, sink.length - offset$iv);
            if (read$iv == -1) {
                throw new EOFException();
            }
            offset$iv += read$iv;
        }
    }

    @Override // okio.BufferedSource
    public int read(byte[] sink, int offset, int byteCount) {
        Intrinsics.checkNotNullParameter(sink, "sink");
        Util.checkOffsetAndCount(sink.length, offset, byteCount);
        Segment s$iv = this.head;
        if (s$iv == null) {
            return -1;
        }
        int toCopy$iv = Math.min(byteCount, s$iv.limit - s$iv.pos);
        ArraysKt.copyInto(s$iv.data, sink, offset, s$iv.pos, s$iv.pos + toCopy$iv);
        s$iv.pos += toCopy$iv;
        setSize$okio(size() - toCopy$iv);
        if (s$iv.pos == s$iv.limit) {
            this.head = s$iv.pop();
            SegmentPool.recycle(s$iv);
            return toCopy$iv;
        }
        return toCopy$iv;
    }

    @Override // java.nio.channels.ReadableByteChannel
    public int read(ByteBuffer sink) throws IOException {
        Intrinsics.checkNotNullParameter(sink, "sink");
        Segment s = this.head;
        if (s == null) {
            return -1;
        }
        int toCopy = Math.min(sink.remaining(), s.limit - s.pos);
        sink.put(s.data, s.pos, toCopy);
        s.pos += toCopy;
        this.size -= toCopy;
        if (s.pos == s.limit) {
            this.head = s.pop();
            SegmentPool.recycle(s);
        }
        return toCopy;
    }

    public final void clear() {
        skip(size());
    }

    @Override // okio.BufferedSource
    public void skip(long byteCount) throws EOFException {
        long byteCount$iv = byteCount;
        while (byteCount$iv > 0) {
            Segment head$iv = this.head;
            if (head$iv == null) {
                throw new EOFException();
            }
            int b$iv$iv = (int) Math.min(byteCount$iv, head$iv.limit - head$iv.pos);
            setSize$okio(size() - b$iv$iv);
            byteCount$iv -= b$iv$iv;
            head$iv.pos += b$iv$iv;
            if (head$iv.pos == head$iv.limit) {
                this.head = head$iv.pop();
                SegmentPool.recycle(head$iv);
            }
        }
    }

    @Override // okio.BufferedSink
    public Buffer write(ByteString byteString) {
        Intrinsics.checkNotNullParameter(byteString, "byteString");
        int byteCount$iv = byteString.size();
        byteString.write$okio(this, 0, byteCount$iv);
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer write(ByteString byteString, int offset, int byteCount) {
        Intrinsics.checkNotNullParameter(byteString, "byteString");
        byteString.write$okio(this, offset, byteCount);
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeUtf8(String string) {
        Intrinsics.checkNotNullParameter(string, "string");
        return writeUtf8(string, 0, string.length());
    }

    @Override // okio.BufferedSink
    public Buffer writeUtf8(String string, int beginIndex, int endIndex) {
        int runLimit$iv;
        Intrinsics.checkNotNullParameter(string, "string");
        int i = 1;
        if (!(beginIndex >= 0)) {
            throw new IllegalArgumentException(("beginIndex < 0: " + beginIndex).toString());
        }
        if (!(endIndex >= beginIndex)) {
            throw new IllegalArgumentException(("endIndex < beginIndex: " + endIndex + " < " + beginIndex).toString());
        }
        if (!(endIndex <= string.length())) {
            throw new IllegalArgumentException(("endIndex > string.length: " + endIndex + " > " + string.length()).toString());
        }
        int runSize$iv = beginIndex;
        while (runSize$iv < endIndex) {
            int c$iv = string.charAt(runSize$iv);
            if (c$iv < 128) {
                Segment tail$iv = writableSegment$okio(i);
                byte[] data$iv = tail$iv.data;
                int segmentOffset$iv = tail$iv.limit - runSize$iv;
                int runLimit$iv2 = Math.min(endIndex, 8192 - segmentOffset$iv);
                int i$iv = runSize$iv + 1;
                int i$iv2 = runSize$iv + segmentOffset$iv;
                data$iv[i$iv2] = (byte) c$iv;
                while (i$iv < runLimit$iv2) {
                    int c$iv2 = string.charAt(i$iv);
                    if (c$iv2 >= 128) {
                        break;
                    }
                    data$iv[i$iv + segmentOffset$iv] = (byte) c$iv2;
                    i$iv++;
                }
                int runSize$iv2 = (i$iv + segmentOffset$iv) - tail$iv.limit;
                tail$iv.limit += runSize$iv2;
                setSize$okio(runSize$iv2 + size());
                runSize$iv = i$iv;
                runLimit$iv = 1;
            } else if (c$iv < 2048) {
                Segment tail$iv2 = writableSegment$okio(2);
                tail$iv2.data[tail$iv2.limit] = (byte) ((c$iv >> 6) | 192);
                tail$iv2.data[tail$iv2.limit + 1] = (byte) (128 | (c$iv & 63));
                tail$iv2.limit += 2;
                setSize$okio(size() + 2);
                runSize$iv++;
                runLimit$iv = 1;
            } else if (c$iv < 55296 || c$iv > 57343) {
                Segment tail$iv3 = writableSegment$okio(3);
                tail$iv3.data[tail$iv3.limit] = (byte) ((c$iv >> 12) | 224);
                runLimit$iv = 1;
                tail$iv3.data[tail$iv3.limit + 1] = (byte) ((63 & (c$iv >> 6)) | 128);
                tail$iv3.data[tail$iv3.limit + 2] = (byte) ((c$iv & 63) | 128);
                tail$iv3.limit += 3;
                setSize$okio(size() + 3);
                runSize$iv++;
            } else {
                int low$iv = runSize$iv + 1 < endIndex ? string.charAt(runSize$iv + 1) : 0;
                if (c$iv > 56319 || 56320 > low$iv || 57343 < low$iv) {
                    writeByte(63);
                    runSize$iv++;
                    runLimit$iv = 1;
                } else {
                    int codePoint$iv = (((c$iv & 1023) << 10) | (low$iv & 1023)) + 65536;
                    Segment tail$iv4 = writableSegment$okio(4);
                    tail$iv4.data[tail$iv4.limit] = (byte) ((codePoint$iv >> 18) | 240);
                    tail$iv4.data[tail$iv4.limit + 1] = (byte) (((codePoint$iv >> 12) & 63) | 128);
                    tail$iv4.data[tail$iv4.limit + 2] = (byte) (((codePoint$iv >> 6) & 63) | 128);
                    tail$iv4.data[tail$iv4.limit + 3] = (byte) (128 | (codePoint$iv & 63));
                    tail$iv4.limit += 4;
                    setSize$okio(size() + 4);
                    runSize$iv += 2;
                    runLimit$iv = 1;
                }
            }
            i = runLimit$iv;
        }
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeUtf8CodePoint(int codePoint) {
        if (codePoint < 128) {
            writeByte(codePoint);
        } else if (codePoint < 2048) {
            Segment tail$iv = writableSegment$okio(2);
            tail$iv.data[tail$iv.limit] = (byte) ((codePoint >> 6) | 192);
            tail$iv.data[tail$iv.limit + 1] = (byte) (128 | (codePoint & 63));
            tail$iv.limit += 2;
            setSize$okio(size() + 2);
        } else if (55296 <= codePoint && 57343 >= codePoint) {
            writeByte(63);
        } else if (codePoint < 65536) {
            Segment tail$iv2 = writableSegment$okio(3);
            tail$iv2.data[tail$iv2.limit] = (byte) ((codePoint >> 12) | 224);
            tail$iv2.data[tail$iv2.limit + 1] = (byte) ((63 & (codePoint >> 6)) | 128);
            tail$iv2.data[tail$iv2.limit + 2] = (byte) (128 | (codePoint & 63));
            tail$iv2.limit += 3;
            setSize$okio(size() + 3);
        } else if (codePoint <= 1114111) {
            Segment tail$iv3 = writableSegment$okio(4);
            tail$iv3.data[tail$iv3.limit] = (byte) ((codePoint >> 18) | 240);
            tail$iv3.data[tail$iv3.limit + 1] = (byte) (((codePoint >> 12) & 63) | 128);
            tail$iv3.data[tail$iv3.limit + 2] = (byte) (((codePoint >> 6) & 63) | 128);
            tail$iv3.data[tail$iv3.limit + 3] = (byte) (128 | (codePoint & 63));
            tail$iv3.limit += 4;
            setSize$okio(size() + 4);
        } else {
            throw new IllegalArgumentException("Unexpected code point: 0x" + Util.toHexString(codePoint));
        }
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeString(String string, Charset charset) {
        Intrinsics.checkNotNullParameter(string, "string");
        Intrinsics.checkNotNullParameter(charset, "charset");
        return writeString(string, 0, string.length(), charset);
    }

    @Override // okio.BufferedSink
    public Buffer writeString(String string, int beginIndex, int endIndex, Charset charset) {
        Intrinsics.checkNotNullParameter(string, "string");
        Intrinsics.checkNotNullParameter(charset, "charset");
        if (!(beginIndex >= 0)) {
            throw new IllegalArgumentException(("beginIndex < 0: " + beginIndex).toString());
        }
        if (!(endIndex >= beginIndex)) {
            throw new IllegalArgumentException(("endIndex < beginIndex: " + endIndex + " < " + beginIndex).toString());
        }
        if (!(endIndex <= string.length())) {
            throw new IllegalArgumentException(("endIndex > string.length: " + endIndex + " > " + string.length()).toString());
        }
        if (Intrinsics.areEqual(charset, Charsets.UTF_8)) {
            return writeUtf8(string, beginIndex, endIndex);
        }
        String substring = string.substring(beginIndex, endIndex);
        Intrinsics.checkNotNullExpressionValue(substring, "(this as java.lang.Strin…ing(startIndex, endIndex)");
        if (substring == null) {
            throw new NullPointerException("null cannot be cast to non-null type java.lang.String");
        }
        byte[] data = substring.getBytes(charset);
        Intrinsics.checkNotNullExpressionValue(data, "(this as java.lang.String).getBytes(charset)");
        return write(data, 0, data.length);
    }

    @Override // okio.BufferedSink
    public Buffer write(byte[] source) {
        Intrinsics.checkNotNullParameter(source, "source");
        Buffer $this$commonWrite$iv = write(source, 0, source.length);
        return $this$commonWrite$iv;
    }

    @Override // okio.BufferedSink
    public Buffer write(byte[] source, int offset, int byteCount) {
        Intrinsics.checkNotNullParameter(source, "source");
        int offset$iv = offset;
        Util.checkOffsetAndCount(source.length, offset$iv, byteCount);
        int limit$iv = offset$iv + byteCount;
        while (offset$iv < limit$iv) {
            Segment tail$iv = writableSegment$okio(1);
            int toCopy$iv = Math.min(limit$iv - offset$iv, 8192 - tail$iv.limit);
            ArraysKt.copyInto(source, tail$iv.data, tail$iv.limit, offset$iv, offset$iv + toCopy$iv);
            offset$iv += toCopy$iv;
            tail$iv.limit += toCopy$iv;
        }
        setSize$okio(size() + byteCount);
        return this;
    }

    @Override // java.nio.channels.WritableByteChannel
    public int write(ByteBuffer source) throws IOException {
        Intrinsics.checkNotNullParameter(source, "source");
        int byteCount = source.remaining();
        int remaining = byteCount;
        while (remaining > 0) {
            Segment tail = writableSegment$okio(1);
            int toCopy = Math.min(remaining, 8192 - tail.limit);
            source.get(tail.data, tail.limit, toCopy);
            remaining -= toCopy;
            tail.limit += toCopy;
        }
        this.size += byteCount;
        return byteCount;
    }

    @Override // okio.BufferedSink
    public long writeAll(Source source) throws IOException {
        Intrinsics.checkNotNullParameter(source, "source");
        long totalBytesRead$iv = 0;
        while (true) {
            long readCount$iv = source.read(this, 8192);
            if (readCount$iv == -1) {
                return totalBytesRead$iv;
            }
            totalBytesRead$iv += readCount$iv;
        }
    }

    @Override // okio.BufferedSink
    public Buffer write(Source source, long byteCount) throws IOException {
        Intrinsics.checkNotNullParameter(source, "source");
        long byteCount$iv = byteCount;
        while (byteCount$iv > 0) {
            long read$iv = source.read(this, byteCount$iv);
            if (read$iv == -1) {
                throw new EOFException();
            }
            byteCount$iv -= read$iv;
        }
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeByte(int b) {
        Segment tail$iv = writableSegment$okio(1);
        byte[] bArr = tail$iv.data;
        int i = tail$iv.limit;
        tail$iv.limit = i + 1;
        bArr[i] = (byte) b;
        setSize$okio(size() + 1);
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeShort(int s) {
        Segment tail$iv = writableSegment$okio(2);
        byte[] data$iv = tail$iv.data;
        int limit$iv = tail$iv.limit;
        int limit$iv2 = limit$iv + 1;
        data$iv[limit$iv] = (byte) ((s >>> 8) & 255);
        data$iv[limit$iv2] = (byte) (s & 255);
        tail$iv.limit = limit$iv2 + 1;
        setSize$okio(size() + 2);
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeShortLe(int s) {
        return writeShort((int) Util.reverseBytes((short) s));
    }

    @Override // okio.BufferedSink
    public Buffer writeInt(int i) {
        Segment tail$iv = writableSegment$okio(4);
        byte[] data$iv = tail$iv.data;
        int limit$iv = tail$iv.limit;
        int limit$iv2 = limit$iv + 1;
        data$iv[limit$iv] = (byte) ((i >>> 24) & 255);
        int limit$iv3 = limit$iv2 + 1;
        data$iv[limit$iv2] = (byte) ((i >>> 16) & 255);
        int limit$iv4 = limit$iv3 + 1;
        data$iv[limit$iv3] = (byte) ((i >>> 8) & 255);
        data$iv[limit$iv4] = (byte) (i & 255);
        tail$iv.limit = limit$iv4 + 1;
        setSize$okio(size() + 4);
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeIntLe(int i) {
        return writeInt(Util.reverseBytes(i));
    }

    @Override // okio.BufferedSink
    public Buffer writeLong(long v) {
        Segment tail$iv = writableSegment$okio(8);
        byte[] data$iv = tail$iv.data;
        int limit$iv = tail$iv.limit;
        int limit$iv2 = limit$iv + 1;
        data$iv[limit$iv] = (byte) ((v >>> 56) & 255);
        int limit$iv3 = limit$iv2 + 1;
        data$iv[limit$iv2] = (byte) ((v >>> 48) & 255);
        int limit$iv4 = limit$iv3 + 1;
        data$iv[limit$iv3] = (byte) ((v >>> 40) & 255);
        int limit$iv5 = limit$iv4 + 1;
        data$iv[limit$iv4] = (byte) ((v >>> 32) & 255);
        int limit$iv6 = limit$iv5 + 1;
        data$iv[limit$iv5] = (byte) ((v >>> 24) & 255);
        int limit$iv7 = limit$iv6 + 1;
        data$iv[limit$iv6] = (byte) ((v >>> 16) & 255);
        int limit$iv8 = limit$iv7 + 1;
        data$iv[limit$iv7] = (byte) ((v >>> 8) & 255);
        data$iv[limit$iv8] = (byte) (v & 255);
        tail$iv.limit = limit$iv8 + 1;
        setSize$okio(size() + 8);
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeLongLe(long v) {
        return writeLong(Util.reverseBytes(v));
    }

    @Override // okio.BufferedSink
    public Buffer writeDecimalLong(long v) {
        int width$iv;
        long v$iv = v;
        if (v$iv == 0) {
            return writeByte(48);
        }
        boolean negative$iv = false;
        if (v$iv < 0) {
            v$iv = -v$iv;
            if (v$iv < 0) {
                return writeUtf8("-9223372036854775808");
            }
            negative$iv = true;
        }
        if (v$iv < 100000000) {
            if (v$iv < 10000) {
                if (v$iv < 100) {
                    width$iv = v$iv < 10 ? 1 : 2;
                } else {
                    width$iv = v$iv < 1000 ? 3 : 4;
                }
            } else if (v$iv < 1000000) {
                width$iv = v$iv < 100000 ? 5 : 6;
            } else {
                width$iv = v$iv < 10000000 ? 7 : 8;
            }
        } else if (v$iv < 1000000000000L) {
            if (v$iv < RealConnection.IDLE_CONNECTION_HEALTHY_NS) {
                width$iv = v$iv < 1000000000 ? 9 : 10;
            } else {
                width$iv = v$iv < 100000000000L ? 11 : 12;
            }
        } else if (v$iv < 1000000000000000L) {
            if (v$iv < 10000000000000L) {
                width$iv = 13;
            } else {
                width$iv = v$iv < 100000000000000L ? 14 : 15;
            }
        } else if (v$iv < 100000000000000000L) {
            width$iv = v$iv < 10000000000000000L ? 16 : 17;
        } else {
            width$iv = v$iv < 1000000000000000000L ? 18 : 19;
        }
        if (negative$iv) {
            width$iv++;
        }
        Segment tail$iv = writableSegment$okio(width$iv);
        byte[] data$iv = tail$iv.data;
        int pos$iv = tail$iv.limit + width$iv;
        while (v$iv != 0) {
            long j = 10;
            int digit$iv = (int) (v$iv % j);
            pos$iv--;
            data$iv[pos$iv] = BufferKt.getHEX_DIGIT_BYTES()[digit$iv];
            v$iv /= j;
        }
        if (negative$iv) {
            data$iv[pos$iv - 1] = (byte) 45;
        }
        tail$iv.limit += width$iv;
        setSize$okio(size() + width$iv);
        return this;
    }

    @Override // okio.BufferedSink
    public Buffer writeHexadecimalUnsignedLong(long v) {
        long v$iv = v;
        if (v$iv == 0) {
            return writeByte(48);
        }
        long x$iv = v$iv | (v$iv >>> 1);
        long x$iv2 = x$iv | (x$iv >>> 2);
        long x$iv3 = x$iv2 | (x$iv2 >>> 4);
        long x$iv4 = x$iv3 | (x$iv3 >>> 8);
        long x$iv5 = x$iv4 | (x$iv4 >>> 16);
        long x$iv6 = x$iv5 | (x$iv5 >>> 32);
        long x$iv7 = x$iv6 - ((x$iv6 >>> 1) & 6148914691236517205L);
        long x$iv8 = ((x$iv7 >>> 2) & 3689348814741910323L) + (3689348814741910323L & x$iv7);
        long x$iv9 = ((x$iv8 >>> 4) + x$iv8) & 1085102592571150095L;
        long x$iv10 = x$iv9 + (x$iv9 >>> 8);
        long x$iv11 = x$iv10 + (x$iv10 >>> 16);
        int width$iv = (int) ((3 + ((x$iv11 & 63) + (63 & (x$iv11 >>> 32)))) / 4);
        Segment tail$iv = writableSegment$okio(width$iv);
        byte[] data$iv = tail$iv.data;
        int start$iv = tail$iv.limit;
        for (int pos$iv = (tail$iv.limit + width$iv) - 1; pos$iv >= start$iv; pos$iv--) {
            data$iv[pos$iv] = BufferKt.getHEX_DIGIT_BYTES()[(int) (15 & v$iv)];
            v$iv >>>= 4;
        }
        tail$iv.limit += width$iv;
        setSize$okio(size() + width$iv);
        return this;
    }

    public final Segment writableSegment$okio(int minimumCapacity) {
        if (!(minimumCapacity >= 1 && minimumCapacity <= 8192)) {
            throw new IllegalArgumentException("unexpected capacity".toString());
        }
        Segment segment = this.head;
        if (segment == null) {
            Segment result$iv = SegmentPool.take();
            this.head = result$iv;
            result$iv.prev = result$iv;
            result$iv.next = result$iv;
            return result$iv;
        }
        Intrinsics.checkNotNull(segment);
        Segment tail$iv = segment.prev;
        Intrinsics.checkNotNull(tail$iv);
        return (tail$iv.limit + minimumCapacity > 8192 || !tail$iv.owner) ? tail$iv.push(SegmentPool.take()) : tail$iv;
    }

    @Override // okio.Sink
    public void write(Buffer source, long byteCount) {
        Segment tail$iv;
        Intrinsics.checkNotNullParameter(source, "source");
        long byteCount$iv = byteCount;
        if (!(source != this)) {
            throw new IllegalArgumentException("source == this".toString());
        }
        Util.checkOffsetAndCount(source.size(), 0L, byteCount$iv);
        while (byteCount$iv > 0) {
            Segment segment = source.head;
            Intrinsics.checkNotNull(segment);
            int i = segment.limit;
            Intrinsics.checkNotNull(source.head);
            if (byteCount$iv < i - r3.pos) {
                Segment segment2 = this.head;
                if (segment2 != null) {
                    Intrinsics.checkNotNull(segment2);
                    tail$iv = segment2.prev;
                } else {
                    tail$iv = null;
                }
                if (tail$iv != null && tail$iv.owner) {
                    if ((tail$iv.limit + byteCount$iv) - (tail$iv.shared ? 0 : tail$iv.pos) <= 8192) {
                        Segment segment3 = source.head;
                        Intrinsics.checkNotNull(segment3);
                        segment3.writeTo(tail$iv, (int) byteCount$iv);
                        source.setSize$okio(source.size() - byteCount$iv);
                        setSize$okio(size() + byteCount$iv);
                        return;
                    }
                }
                Segment segment4 = source.head;
                Intrinsics.checkNotNull(segment4);
                source.head = segment4.split((int) byteCount$iv);
            }
            Segment segmentToMove$iv = source.head;
            Intrinsics.checkNotNull(segmentToMove$iv);
            long movedByteCount$iv = segmentToMove$iv.limit - segmentToMove$iv.pos;
            source.head = segmentToMove$iv.pop();
            Segment segment5 = this.head;
            if (segment5 == null) {
                this.head = segmentToMove$iv;
                segmentToMove$iv.prev = segmentToMove$iv;
                segmentToMove$iv.next = segmentToMove$iv.prev;
            } else {
                Intrinsics.checkNotNull(segment5);
                Segment tail$iv2 = segment5.prev;
                Intrinsics.checkNotNull(tail$iv2);
                tail$iv2.push(segmentToMove$iv).compact();
            }
            source.setSize$okio(source.size() - movedByteCount$iv);
            setSize$okio(size() + movedByteCount$iv);
            byteCount$iv -= movedByteCount$iv;
        }
    }

    @Override // okio.Source
    public long read(Buffer sink, long byteCount) {
        Intrinsics.checkNotNullParameter(sink, "sink");
        long byteCount$iv = byteCount;
        if (!(byteCount$iv >= 0)) {
            throw new IllegalArgumentException(("byteCount < 0: " + byteCount$iv).toString());
        }
        if (size() == 0) {
            return -1L;
        }
        if (byteCount$iv > size()) {
            byteCount$iv = size();
        }
        sink.write(this, byteCount$iv);
        return byteCount$iv;
    }

    @Override // okio.BufferedSource
    public long indexOf(byte b) {
        return indexOf(b, 0L, LongCompanionObject.MAX_VALUE);
    }

    @Override // okio.BufferedSource
    public long indexOf(byte b, long fromIndex) {
        return indexOf(b, fromIndex, LongCompanionObject.MAX_VALUE);
    }

    @Override // okio.BufferedSource
    public long indexOf(byte b, long fromIndex, long toIndex) {
        long fromIndex$iv = fromIndex;
        long toIndex$iv = toIndex;
        if (!(0 <= fromIndex$iv && toIndex$iv >= fromIndex$iv)) {
            throw new IllegalArgumentException(("size=" + size() + " fromIndex=" + fromIndex$iv + " toIndex=" + toIndex$iv).toString());
        }
        if (toIndex$iv > size()) {
            toIndex$iv = size();
        }
        if (fromIndex$iv == toIndex$iv) {
            return -1L;
        }
        long fromIndex$iv$iv = fromIndex$iv;
        Buffer $this$seek$iv$iv = this;
        int $i$f$seek = 0;
        Segment s$iv$iv = $this$seek$iv$iv.head;
        if (s$iv$iv == null) {
            return -1L;
        }
        if ($this$seek$iv$iv.size() - fromIndex$iv$iv >= fromIndex$iv$iv) {
            long offset$iv$iv = 0;
            while (true) {
                long nextOffset$iv$iv = (s$iv$iv.limit - s$iv$iv.pos) + offset$iv$iv;
                if (nextOffset$iv$iv > fromIndex$iv$iv) {
                    break;
                }
                Segment segment = s$iv$iv.next;
                Intrinsics.checkNotNull(segment);
                s$iv$iv = segment;
                offset$iv$iv = nextOffset$iv$iv;
            }
            Segment s$iv = s$iv$iv;
            long offset$iv = offset$iv$iv;
            int i = 0;
            if (s$iv == null) {
                return -1L;
            }
            Segment s$iv2 = s$iv;
            long offset$iv2 = offset$iv;
            while (offset$iv2 < toIndex$iv) {
                Segment s$iv3 = s$iv;
                byte[] data$iv = s$iv2.data;
                long offset$iv$iv2 = offset$iv$iv;
                int i2 = i;
                long fromIndex$iv$iv2 = fromIndex$iv$iv;
                int limit$iv = (int) Math.min(s$iv2.limit, (s$iv2.pos + toIndex$iv) - offset$iv2);
                for (int pos$iv = (int) ((s$iv2.pos + fromIndex$iv) - offset$iv2); pos$iv < limit$iv; pos$iv++) {
                    if (data$iv[pos$iv] == b) {
                        return (pos$iv - s$iv2.pos) + offset$iv2;
                    }
                }
                offset$iv2 += s$iv2.limit - s$iv2.pos;
                fromIndex$iv = offset$iv2;
                Segment segment2 = s$iv2.next;
                Intrinsics.checkNotNull(segment2);
                s$iv2 = segment2;
                s$iv = s$iv3;
                i = i2;
                offset$iv$iv = offset$iv$iv2;
                fromIndex$iv$iv = fromIndex$iv$iv2;
            }
            return -1L;
        }
        long offset$iv$iv3 = $this$seek$iv$iv.size();
        while (offset$iv$iv3 > fromIndex$iv$iv) {
            Segment segment3 = s$iv$iv.prev;
            Intrinsics.checkNotNull(segment3);
            s$iv$iv = segment3;
            offset$iv$iv3 -= s$iv$iv.limit - s$iv$iv.pos;
        }
        Segment s$iv4 = s$iv$iv;
        long offset$iv3 = offset$iv$iv3;
        int i3 = 0;
        if (s$iv4 == null) {
            return -1L;
        }
        long offset$iv4 = offset$iv3;
        Segment s$iv5 = s$iv4;
        while (offset$iv4 < toIndex$iv) {
            Buffer $this$seek$iv$iv2 = $this$seek$iv$iv;
            byte[] data$iv2 = s$iv5.data;
            Segment s$iv6 = s$iv4;
            int i4 = i3;
            long j = s$iv5.limit;
            int $i$f$seek2 = $i$f$seek;
            int $i$f$seek3 = s$iv5.pos;
            Segment s$iv$iv2 = s$iv$iv;
            int limit$iv2 = (int) Math.min(j, ($i$f$seek3 + toIndex$iv) - offset$iv4);
            for (int pos$iv2 = (int) ((s$iv5.pos + fromIndex$iv) - offset$iv4); pos$iv2 < limit$iv2; pos$iv2++) {
                if (data$iv2[pos$iv2] == b) {
                    return (pos$iv2 - s$iv5.pos) + offset$iv4;
                }
            }
            offset$iv4 += s$iv5.limit - s$iv5.pos;
            fromIndex$iv = offset$iv4;
            Segment segment4 = s$iv5.next;
            Intrinsics.checkNotNull(segment4);
            s$iv5 = segment4;
            $this$seek$iv$iv = $this$seek$iv$iv2;
            s$iv4 = s$iv6;
            i3 = i4;
            $i$f$seek = $i$f$seek2;
            s$iv$iv = s$iv$iv2;
        }
        return -1L;
    }

    @Override // okio.BufferedSource
    public long indexOf(ByteString bytes) throws IOException {
        Intrinsics.checkNotNullParameter(bytes, "bytes");
        return indexOf(bytes, 0L);
    }

    @Override // okio.BufferedSource
    public long indexOf(ByteString bytes, long fromIndex) throws IOException {
        byte b0$iv;
        byte[] targetByteArray$iv;
        Intrinsics.checkNotNullParameter(bytes, "bytes");
        Buffer $this$commonIndexOf$iv = this;
        long fromIndex$iv = fromIndex;
        if (!(bytes.size() > 0)) {
            throw new IllegalArgumentException("bytes is empty".toString());
        }
        if (!(fromIndex$iv >= 0)) {
            throw new IllegalArgumentException(("fromIndex < 0: " + fromIndex$iv).toString());
        }
        long fromIndex$iv$iv = fromIndex$iv;
        Buffer $this$seek$iv$iv = $this$commonIndexOf$iv;
        int $i$f$seek = 0;
        Segment s$iv$iv = $this$seek$iv$iv.head;
        if (s$iv$iv == null) {
            return -1L;
        }
        if ($this$seek$iv$iv.size() - fromIndex$iv$iv < fromIndex$iv$iv) {
            long offset$iv$iv = $this$seek$iv$iv.size();
            while (offset$iv$iv > fromIndex$iv$iv) {
                Segment segment = s$iv$iv.prev;
                Intrinsics.checkNotNull(segment);
                s$iv$iv = segment;
                offset$iv$iv -= s$iv$iv.limit - s$iv$iv.pos;
            }
            Segment s$iv = s$iv$iv;
            long offset$iv = offset$iv$iv;
            if (s$iv == null) {
                return -1L;
            }
            long offset$iv2 = offset$iv;
            byte[] targetByteArray$iv2 = bytes.internalArray$okio();
            byte b0$iv2 = targetByteArray$iv2[0];
            int bytesSize$iv = bytes.size();
            long resultLimit$iv = ($this$commonIndexOf$iv.size() - bytesSize$iv) + 1;
            Segment s$iv2 = s$iv;
            while (offset$iv2 < resultLimit$iv) {
                byte[] data$iv = s$iv2.data;
                Buffer $this$seek$iv$iv2 = $this$seek$iv$iv;
                int a$iv$iv = s$iv2.limit;
                int $i$f$seek2 = $i$f$seek;
                int $i$f$seek3 = s$iv2.pos;
                Segment s$iv$iv2 = s$iv$iv;
                long b$iv$iv = ($i$f$seek3 + resultLimit$iv) - offset$iv2;
                Segment s$iv3 = s$iv;
                long offset$iv3 = offset$iv;
                int a$iv$iv2 = (int) Math.min(a$iv$iv, b$iv$iv);
                for (int pos$iv = (int) ((s$iv2.pos + fromIndex$iv) - offset$iv2); pos$iv < a$iv$iv2; pos$iv++) {
                    if (data$iv[pos$iv] == b0$iv2 && BufferKt.rangeEquals(s$iv2, pos$iv + 1, targetByteArray$iv2, 1, bytesSize$iv)) {
                        return (pos$iv - s$iv2.pos) + offset$iv2;
                    }
                }
                offset$iv2 += s$iv2.limit - s$iv2.pos;
                fromIndex$iv = offset$iv2;
                Segment segment2 = s$iv2.next;
                Intrinsics.checkNotNull(segment2);
                s$iv2 = segment2;
                $this$seek$iv$iv = $this$seek$iv$iv2;
                s$iv$iv = s$iv$iv2;
                $i$f$seek = $i$f$seek2;
                s$iv = s$iv3;
                offset$iv = offset$iv3;
            }
            return -1L;
        }
        long offset$iv$iv2 = 0;
        while (true) {
            long nextOffset$iv$iv = (s$iv$iv.limit - s$iv$iv.pos) + offset$iv$iv2;
            if (nextOffset$iv$iv > fromIndex$iv$iv) {
                break;
            }
            Segment segment3 = s$iv$iv.next;
            Intrinsics.checkNotNull(segment3);
            s$iv$iv = segment3;
            offset$iv$iv2 = nextOffset$iv$iv;
            $this$commonIndexOf$iv = $this$commonIndexOf$iv;
            fromIndex$iv$iv = fromIndex$iv$iv;
        }
        Segment s$iv4 = s$iv$iv;
        long offset$iv4 = offset$iv$iv2;
        if (s$iv4 == null) {
            return -1L;
        }
        Segment s$iv5 = s$iv4;
        long offset$iv5 = offset$iv4;
        byte[] targetByteArray$iv3 = bytes.internalArray$okio();
        byte b0$iv3 = targetByteArray$iv3[0];
        int bytesSize$iv2 = bytes.size();
        long resultLimit$iv2 = ($this$commonIndexOf$iv.size() - bytesSize$iv2) + 1;
        while (offset$iv5 < resultLimit$iv2) {
            byte[] data$iv2 = s$iv5.data;
            int a$iv$iv3 = s$iv5.limit;
            Buffer $this$commonIndexOf$iv2 = $this$commonIndexOf$iv;
            long offset$iv6 = offset$iv4;
            long b$iv$iv2 = (s$iv5.pos + resultLimit$iv2) - offset$iv5;
            byte[] targetByteArray$iv4 = targetByteArray$iv3;
            int segmentLimit$iv = (int) Math.min(a$iv$iv3, b$iv$iv2);
            int pos$iv2 = (int) ((s$iv5.pos + fromIndex$iv) - offset$iv5);
            while (pos$iv2 < segmentLimit$iv) {
                if (data$iv2[pos$iv2] == b0$iv3) {
                    targetByteArray$iv = targetByteArray$iv4;
                    if (BufferKt.rangeEquals(s$iv5, pos$iv2 + 1, targetByteArray$iv, 1, bytesSize$iv2)) {
                        return (pos$iv2 - s$iv5.pos) + offset$iv5;
                    }
                    b0$iv = b0$iv3;
                } else {
                    b0$iv = b0$iv3;
                    targetByteArray$iv = targetByteArray$iv4;
                }
                pos$iv2++;
                b0$iv3 = b0$iv;
                fromIndex$iv = fromIndex$iv;
                targetByteArray$iv4 = targetByteArray$iv;
            }
            offset$iv5 += s$iv5.limit - s$iv5.pos;
            fromIndex$iv = offset$iv5;
            Segment segment4 = s$iv5.next;
            Intrinsics.checkNotNull(segment4);
            s$iv5 = segment4;
            targetByteArray$iv3 = targetByteArray$iv4;
            $this$commonIndexOf$iv = $this$commonIndexOf$iv2;
            b0$iv3 = b0$iv3;
            offset$iv4 = offset$iv6;
        }
        return -1L;
    }

    @Override // okio.BufferedSource
    public long indexOfElement(ByteString targetBytes) {
        Intrinsics.checkNotNullParameter(targetBytes, "targetBytes");
        return indexOfElement(targetBytes, 0L);
    }

    @Override // okio.BufferedSource
    public long indexOfElement(ByteString targetBytes, long fromIndex) {
        ByteString targetBytes2 = targetBytes;
        Intrinsics.checkNotNullParameter(targetBytes2, "targetBytes");
        Buffer $this$commonIndexOfElement$iv = this;
        int $i$f$commonIndexOfElement = 0;
        long fromIndex$iv = fromIndex;
        if (!(fromIndex$iv >= 0)) {
            throw new IllegalArgumentException(("fromIndex < 0: " + fromIndex$iv).toString());
        }
        Buffer $this$seek$iv$iv = $this$commonIndexOfElement$iv;
        int $i$f$seek = 0;
        Segment s$iv$iv = $this$seek$iv$iv.head;
        if (s$iv$iv == null) {
            return -1L;
        }
        if ($this$seek$iv$iv.size() - fromIndex$iv < fromIndex$iv) {
            long offset$iv$iv = $this$seek$iv$iv.size();
            while (offset$iv$iv > fromIndex$iv) {
                Segment segment = s$iv$iv.prev;
                Intrinsics.checkNotNull(segment);
                s$iv$iv = segment;
                offset$iv$iv -= s$iv$iv.limit - s$iv$iv.pos;
            }
            Segment s$iv = s$iv$iv;
            long offset$iv = offset$iv$iv;
            if (s$iv == null) {
                return -1L;
            }
            long offset$iv2 = offset$iv;
            if (targetBytes.size() == 2) {
                int b0$iv = targetBytes2.getByte(0);
                int b1$iv = targetBytes2.getByte(1);
                Segment s$iv2 = s$iv;
                while (offset$iv2 < $this$commonIndexOfElement$iv.size()) {
                    int $i$f$commonIndexOfElement2 = $i$f$commonIndexOfElement;
                    byte[] data$iv = s$iv2.data;
                    Buffer $this$seek$iv$iv2 = $this$seek$iv$iv;
                    int $i$f$seek2 = $i$f$seek;
                    Segment s$iv$iv2 = s$iv$iv;
                    int limit$iv = s$iv2.limit;
                    for (int pos$iv = (int) ((s$iv2.pos + fromIndex$iv) - offset$iv2); pos$iv < limit$iv; pos$iv++) {
                        int b$iv = data$iv[pos$iv];
                        if (b$iv == b0$iv || b$iv == b1$iv) {
                            return (pos$iv - s$iv2.pos) + offset$iv2;
                        }
                    }
                    offset$iv2 += s$iv2.limit - s$iv2.pos;
                    fromIndex$iv = offset$iv2;
                    Segment segment2 = s$iv2.next;
                    Intrinsics.checkNotNull(segment2);
                    s$iv2 = segment2;
                    s$iv$iv = s$iv$iv2;
                    $i$f$commonIndexOfElement = $i$f$commonIndexOfElement2;
                    $this$seek$iv$iv = $this$seek$iv$iv2;
                    $i$f$seek = $i$f$seek2;
                }
            } else {
                byte[] targetByteArray$iv = targetBytes.internalArray$okio();
                Segment s$iv3 = s$iv;
                while (offset$iv2 < $this$commonIndexOfElement$iv.size()) {
                    byte[] data$iv2 = s$iv3.data;
                    int pos$iv2 = (int) ((s$iv3.pos + fromIndex$iv) - offset$iv2);
                    int limit$iv2 = s$iv3.limit;
                    while (pos$iv2 < limit$iv2) {
                        byte b$iv2 = data$iv2[pos$iv2];
                        long fromIndex$iv2 = fromIndex$iv;
                        for (byte t$iv : targetByteArray$iv) {
                            if (b$iv2 == t$iv) {
                                return (pos$iv2 - s$iv3.pos) + offset$iv2;
                            }
                        }
                        pos$iv2++;
                        fromIndex$iv = fromIndex$iv2;
                    }
                    byte[] targetByteArray$iv2 = targetByteArray$iv;
                    offset$iv2 += s$iv3.limit - s$iv3.pos;
                    fromIndex$iv = offset$iv2;
                    Segment segment3 = s$iv3.next;
                    Intrinsics.checkNotNull(segment3);
                    s$iv3 = segment3;
                    targetByteArray$iv = targetByteArray$iv2;
                }
            }
            return -1L;
        }
        long offset$iv$iv2 = 0;
        while (true) {
            long nextOffset$iv$iv = (s$iv$iv.limit - s$iv$iv.pos) + offset$iv$iv2;
            if (nextOffset$iv$iv > fromIndex$iv) {
                break;
            }
            Buffer $this$commonIndexOfElement$iv2 = $this$commonIndexOfElement$iv;
            Segment segment4 = s$iv$iv.next;
            Intrinsics.checkNotNull(segment4);
            s$iv$iv = segment4;
            offset$iv$iv2 = nextOffset$iv$iv;
            targetBytes2 = targetBytes;
            $this$commonIndexOfElement$iv = $this$commonIndexOfElement$iv2;
        }
        Segment s$iv4 = s$iv$iv;
        long offset$iv3 = offset$iv$iv2;
        if (s$iv4 == null) {
            return -1L;
        }
        Segment s$iv5 = s$iv4;
        long offset$iv4 = offset$iv3;
        if (targetBytes.size() == 2) {
            int b0$iv2 = targetBytes2.getByte(0);
            int b1$iv2 = targetBytes2.getByte(1);
            while (offset$iv4 < $this$commonIndexOfElement$iv.size()) {
                byte[] data$iv3 = s$iv5.data;
                Segment s$iv6 = s$iv4;
                long offset$iv$iv3 = offset$iv$iv2;
                long offset$iv$iv4 = s$iv5.pos;
                int limit$iv3 = s$iv5.limit;
                for (int pos$iv3 = (int) ((offset$iv$iv4 + fromIndex$iv) - offset$iv4); pos$iv3 < limit$iv3; pos$iv3++) {
                    int b$iv3 = data$iv3[pos$iv3];
                    if (b$iv3 == b0$iv2 || b$iv3 == b1$iv2) {
                        return (pos$iv3 - s$iv5.pos) + offset$iv4;
                    }
                }
                offset$iv4 += s$iv5.limit - s$iv5.pos;
                Segment segment5 = s$iv5.next;
                Intrinsics.checkNotNull(segment5);
                s$iv5 = segment5;
                fromIndex$iv = offset$iv4;
                s$iv4 = s$iv6;
                offset$iv$iv2 = offset$iv$iv3;
            }
        } else {
            byte[] targetByteArray$iv3 = targetBytes.internalArray$okio();
            while (offset$iv4 < $this$commonIndexOfElement$iv.size()) {
                byte[] data$iv4 = s$iv5.data;
                int pos$iv4 = (int) ((s$iv5.pos + fromIndex$iv) - offset$iv4);
                int limit$iv4 = s$iv5.limit;
                while (pos$iv4 < limit$iv4) {
                    byte b$iv4 = data$iv4[pos$iv4];
                    int length = targetByteArray$iv3.length;
                    Buffer $this$commonIndexOfElement$iv3 = $this$commonIndexOfElement$iv;
                    int i = 0;
                    while (i < length) {
                        byte[] data$iv5 = data$iv4;
                        byte t$iv2 = targetByteArray$iv3[i];
                        if (b$iv4 == t$iv2) {
                            return (pos$iv4 - s$iv5.pos) + offset$iv4;
                        }
                        i++;
                        data$iv4 = data$iv5;
                    }
                    pos$iv4++;
                    $this$commonIndexOfElement$iv = $this$commonIndexOfElement$iv3;
                }
                byte[] targetByteArray$iv4 = targetByteArray$iv3;
                offset$iv4 += s$iv5.limit - s$iv5.pos;
                fromIndex$iv = offset$iv4;
                Segment segment6 = s$iv5.next;
                Intrinsics.checkNotNull(segment6);
                s$iv5 = segment6;
                $this$commonIndexOfElement$iv = $this$commonIndexOfElement$iv;
                targetByteArray$iv3 = targetByteArray$iv4;
            }
        }
        return -1L;
    }

    @Override // okio.BufferedSource
    public boolean rangeEquals(long offset, ByteString bytes) {
        Intrinsics.checkNotNullParameter(bytes, "bytes");
        return rangeEquals(offset, bytes, 0, bytes.size());
    }

    @Override // okio.BufferedSource
    public boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount) {
        Intrinsics.checkNotNullParameter(bytes, "bytes");
        if (offset < 0 || bytesOffset < 0 || byteCount < 0 || size() - offset < byteCount || bytes.size() - bytesOffset < byteCount) {
            return false;
        }
        for (int i$iv = 0; i$iv < byteCount; i$iv++) {
            if (getByte(i$iv + offset) != bytes.getByte(bytesOffset + i$iv)) {
                return false;
            }
        }
        return true;
    }

    @Override // okio.BufferedSink, okio.Sink, java.io.Flushable
    public void flush() {
    }

    @Override // java.nio.channels.Channel
    public boolean isOpen() {
        return true;
    }

    @Override // okio.Source, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
    }

    @Override // okio.Source
    /* renamed from: timeout */
    public Timeout getThis$0() {
        return Timeout.NONE;
    }

    public final ByteString md5() {
        return digest("MD5");
    }

    public final ByteString sha1() {
        return digest("SHA-1");
    }

    public final ByteString sha256() {
        return digest("SHA-256");
    }

    public final ByteString sha512() {
        return digest("SHA-512");
    }

    private final ByteString digest(String algorithm) {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        Segment head = this.head;
        if (head != null) {
            messageDigest.update(head.data, head.pos, head.limit - head.pos);
            Segment s = head.next;
            Intrinsics.checkNotNull(s);
            while (s != head) {
                messageDigest.update(s.data, s.pos, s.limit - s.pos);
                Segment segment = s.next;
                Intrinsics.checkNotNull(segment);
                s = segment;
            }
        }
        byte[] digest = messageDigest.digest();
        Intrinsics.checkNotNullExpressionValue(digest, "messageDigest.digest()");
        return new ByteString(digest);
    }

    public final ByteString hmacSha1(ByteString key) {
        Intrinsics.checkNotNullParameter(key, "key");
        return hmac("HmacSHA1", key);
    }

    public final ByteString hmacSha256(ByteString key) {
        Intrinsics.checkNotNullParameter(key, "key");
        return hmac("HmacSHA256", key);
    }

    public final ByteString hmacSha512(ByteString key) {
        Intrinsics.checkNotNullParameter(key, "key");
        return hmac("HmacSHA512", key);
    }

    private final ByteString hmac(String algorithm, ByteString key) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key.internalArray$okio(), algorithm));
            Segment head = this.head;
            if (head != null) {
                mac.update(head.data, head.pos, head.limit - head.pos);
                Segment s = head.next;
                Intrinsics.checkNotNull(s);
                while (s != head) {
                    mac.update(s.data, s.pos, s.limit - s.pos);
                    Segment segment = s.next;
                    Intrinsics.checkNotNull(segment);
                    s = segment;
                }
            }
            byte[] doFinal = mac.doFinal();
            Intrinsics.checkNotNullExpressionValue(doFinal, "mac.doFinal()");
            return new ByteString(doFinal);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Buffer) || size() != ((Buffer) other).size()) {
            return false;
        }
        if (size() == 0) {
            return true;
        }
        Segment sa$iv = this.head;
        Intrinsics.checkNotNull(sa$iv);
        Segment sb$iv = ((Buffer) other).head;
        Intrinsics.checkNotNull(sb$iv);
        int posA$iv = sa$iv.pos;
        int posB$iv = sb$iv.pos;
        long pos$iv = 0;
        while (pos$iv < size()) {
            long count$iv = Math.min(sa$iv.limit - posA$iv, sb$iv.limit - posB$iv);
            long i$iv = 0;
            while (i$iv < count$iv) {
                int posA$iv2 = posA$iv + 1;
                int posB$iv2 = posB$iv + 1;
                if (sa$iv.data[posA$iv] != sb$iv.data[posB$iv]) {
                    return false;
                }
                i$iv++;
                posA$iv = posA$iv2;
                posB$iv = posB$iv2;
            }
            if (posA$iv == sa$iv.limit) {
                Segment sa$iv2 = sa$iv.next;
                Intrinsics.checkNotNull(sa$iv2);
                posA$iv = sa$iv2.pos;
                sa$iv = sa$iv2;
            }
            if (posB$iv == sb$iv.limit) {
                Segment sb$iv2 = sb$iv.next;
                Intrinsics.checkNotNull(sb$iv2);
                posB$iv = sb$iv2.pos;
                sb$iv = sb$iv2;
            }
            pos$iv += count$iv;
        }
        return true;
    }

    public int hashCode() {
        Segment s$iv = this.head;
        if (s$iv == null) {
            return 0;
        }
        int result$iv = 1;
        do {
            int limit$iv = s$iv.limit;
            for (int pos$iv = s$iv.pos; pos$iv < limit$iv; pos$iv++) {
                result$iv = (result$iv * 31) + s$iv.data[pos$iv];
            }
            Segment segment = s$iv.next;
            Intrinsics.checkNotNull(segment);
            s$iv = segment;
        } while (s$iv != this.head);
        return result$iv;
    }

    public String toString() {
        return snapshot().toString();
    }

    public final Buffer copy() {
        Buffer result$iv = new Buffer();
        if (size() != 0) {
            Segment head$iv = this.head;
            Intrinsics.checkNotNull(head$iv);
            Segment headCopy$iv = head$iv.sharedCopy();
            result$iv.head = headCopy$iv;
            headCopy$iv.prev = headCopy$iv;
            headCopy$iv.next = headCopy$iv.prev;
            for (Segment s$iv = head$iv.next; s$iv != head$iv; s$iv = s$iv.next) {
                Segment segment = headCopy$iv.prev;
                Intrinsics.checkNotNull(segment);
                Intrinsics.checkNotNull(s$iv);
                segment.push(s$iv.sharedCopy());
            }
            result$iv.setSize$okio(size());
        }
        return result$iv;
    }

    public Buffer clone() {
        return copy();
    }

    public final ByteString snapshot() {
        if (size() <= ((long) Integer.MAX_VALUE)) {
            return snapshot((int) size());
        }
        throw new IllegalStateException(("size > Int.MAX_VALUE: " + size()).toString());
    }

    public final ByteString snapshot(int byteCount) {
        if (byteCount == 0) {
            return ByteString.EMPTY;
        }
        Util.checkOffsetAndCount(size(), 0L, byteCount);
        int offset$iv = 0;
        int segmentCount$iv = 0;
        Segment s$iv = this.head;
        while (offset$iv < byteCount) {
            Intrinsics.checkNotNull(s$iv);
            if (s$iv.limit == s$iv.pos) {
                throw new AssertionError("s.limit == s.pos");
            }
            offset$iv += s$iv.limit - s$iv.pos;
            segmentCount$iv++;
            s$iv = s$iv.next;
        }
        byte[][] segments$iv = new byte[segmentCount$iv][];
        int[] directory$iv = new int[segmentCount$iv * 2];
        int offset$iv2 = 0;
        int segmentCount$iv2 = 0;
        Segment s$iv2 = this.head;
        while (offset$iv2 < byteCount) {
            Intrinsics.checkNotNull(s$iv2);
            segments$iv[segmentCount$iv2] = s$iv2.data;
            offset$iv2 += s$iv2.limit - s$iv2.pos;
            directory$iv[segmentCount$iv2] = Math.min(offset$iv2, byteCount);
            directory$iv[segments$iv.length + segmentCount$iv2] = s$iv2.pos;
            s$iv2.shared = true;
            segmentCount$iv2++;
            s$iv2 = s$iv2.next;
        }
        return new SegmentedByteString(segments$iv, directory$iv);
    }

    public static /* synthetic */ UnsafeCursor readUnsafe$default(Buffer buffer, UnsafeCursor unsafeCursor, int i, Object obj) {
        if ((i & 1) != 0) {
            unsafeCursor = new UnsafeCursor();
        }
        return buffer.readUnsafe(unsafeCursor);
    }

    public final UnsafeCursor readUnsafe(UnsafeCursor unsafeCursor) {
        Intrinsics.checkNotNullParameter(unsafeCursor, "unsafeCursor");
        if (!(unsafeCursor.buffer == null)) {
            throw new IllegalStateException("already attached to a buffer".toString());
        }
        unsafeCursor.buffer = this;
        unsafeCursor.readWrite = false;
        return unsafeCursor;
    }

    public static /* synthetic */ UnsafeCursor readAndWriteUnsafe$default(Buffer buffer, UnsafeCursor unsafeCursor, int i, Object obj) {
        if ((i & 1) != 0) {
            unsafeCursor = new UnsafeCursor();
        }
        return buffer.readAndWriteUnsafe(unsafeCursor);
    }

    public final UnsafeCursor readAndWriteUnsafe(UnsafeCursor unsafeCursor) {
        Intrinsics.checkNotNullParameter(unsafeCursor, "unsafeCursor");
        if (!(unsafeCursor.buffer == null)) {
            throw new IllegalStateException("already attached to a buffer".toString());
        }
        unsafeCursor.buffer = this;
        unsafeCursor.readWrite = true;
        return unsafeCursor;
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "moved to operator function", replaceWith = @ReplaceWith(expression = "this[index]", imports = {}))
    /* renamed from: -deprecated_getByte, reason: not valid java name */
    public final byte m1743deprecated_getByte(long index) {
        return getByte(index);
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "moved to val", replaceWith = @ReplaceWith(expression = "size", imports = {}))
    /* renamed from: -deprecated_size, reason: not valid java name and from getter */
    public final long getSize() {
        return this.size;
    }

    /* compiled from: Buffer.kt */
    @Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\b\u0010\u0010\u001a\u00020\u0011H\u0016J\u000e\u0010\u0012\u001a\u00020\n2\u0006\u0010\u0013\u001a\u00020\bJ\u0006\u0010\u0014\u001a\u00020\bJ\u000e\u0010\u0015\u001a\u00020\n2\u0006\u0010\u0016\u001a\u00020\nJ\u000e\u0010\u0017\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nR\u0014\u0010\u0003\u001a\u0004\u0018\u00010\u00048\u0006@\u0006X\u0087\u000e¢\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u0004\u0018\u00010\u00068\u0006@\u0006X\u0087\u000e¢\u0006\u0002\n\u0000R\u0012\u0010\u0007\u001a\u00020\b8\u0006@\u0006X\u0087\u000e¢\u0006\u0002\n\u0000R\u0012\u0010\t\u001a\u00020\n8\u0006@\u0006X\u0087\u000e¢\u0006\u0002\n\u0000R\u0012\u0010\u000b\u001a\u00020\f8\u0006@\u0006X\u0087\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e¢\u0006\u0002\n\u0000R\u0012\u0010\u000f\u001a\u00020\b8\u0006@\u0006X\u0087\u000e¢\u0006\u0002\n\u0000¨\u0006\u0018"}, m238d2 = {"Lokio/Buffer$UnsafeCursor;", "Ljava/io/Closeable;", "()V", "buffer", "Lokio/Buffer;", "data", "", "end", "", TypedValues.Cycle.S_WAVE_OFFSET, "", "readWrite", "", "segment", "Lokio/Segment;", "start", "close", "", "expandBuffer", "minByteCount", "next", "resizeBuffer", "newSize", "seek", "okio"}, m239k = 1, m240mv = {1, 4, 0})
    public static final class UnsafeCursor implements Closeable {
        public Buffer buffer;
        public byte[] data;
        public boolean readWrite;
        private Segment segment;
        public long offset = -1;
        public int start = -1;
        public int end = -1;

        public final int next() {
            long j = this.offset;
            Buffer buffer = this.buffer;
            Intrinsics.checkNotNull(buffer);
            if (!(j != buffer.size())) {
                throw new IllegalStateException("no more bytes".toString());
            }
            long j2 = this.offset;
            return seek(j2 == -1 ? 0L : j2 + (this.end - this.start));
        }

        public final int seek(long offset) {
            Segment next;
            long nextOffset;
            Buffer buffer = this.buffer;
            if (buffer == null) {
                throw new IllegalStateException("not attached to a buffer".toString());
            }
            if (offset < -1 || offset > buffer.size()) {
                StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
                String format = String.format("offset=%s > size=%s", Arrays.copyOf(new Object[]{Long.valueOf(offset), Long.valueOf(buffer.size())}, 2));
                Intrinsics.checkNotNullExpressionValue(format, "java.lang.String.format(format, *args)");
                throw new ArrayIndexOutOfBoundsException(format);
            }
            if (offset == -1 || offset == buffer.size()) {
                this.segment = null;
                this.offset = offset;
                this.data = null;
                this.start = -1;
                this.end = -1;
                return -1;
            }
            long min = 0;
            long max = buffer.size();
            Segment head = buffer.head;
            Segment tail = buffer.head;
            Segment segment = this.segment;
            if (segment != null) {
                long j = this.offset;
                int i = this.start;
                Intrinsics.checkNotNull(segment);
                long segmentOffset = j - (i - segment.pos);
                if (segmentOffset > offset) {
                    max = segmentOffset;
                    tail = this.segment;
                } else {
                    min = segmentOffset;
                    head = this.segment;
                }
            }
            if (max - offset > offset - min) {
                next = head;
                nextOffset = min;
                while (true) {
                    Intrinsics.checkNotNull(next);
                    if (offset < (next.limit - next.pos) + nextOffset) {
                        break;
                    }
                    nextOffset += next.limit - next.pos;
                    next = next.next;
                }
            } else {
                next = tail;
                nextOffset = max;
                while (nextOffset > offset) {
                    Intrinsics.checkNotNull(next);
                    next = next.prev;
                    Intrinsics.checkNotNull(next);
                    nextOffset -= next.limit - next.pos;
                }
            }
            if (this.readWrite) {
                Intrinsics.checkNotNull(next);
                if (next.shared) {
                    Segment unsharedNext = next.unsharedCopy();
                    if (buffer.head == next) {
                        buffer.head = unsharedNext;
                    }
                    next = next.push(unsharedNext);
                    Segment segment2 = next.prev;
                    Intrinsics.checkNotNull(segment2);
                    segment2.pop();
                }
            }
            this.segment = next;
            this.offset = offset;
            Intrinsics.checkNotNull(next);
            this.data = next.data;
            this.start = next.pos + ((int) (offset - nextOffset));
            int i2 = next.limit;
            this.end = i2;
            return i2 - this.start;
        }

        public final long resizeBuffer(long newSize) {
            Buffer buffer = this.buffer;
            if (buffer == null) {
                throw new IllegalStateException("not attached to a buffer".toString());
            }
            if (!this.readWrite) {
                throw new IllegalStateException("resizeBuffer() only permitted for read/write buffers".toString());
            }
            long oldSize = buffer.size();
            if (newSize <= oldSize) {
                if ((newSize < 0 ? 0 : 1) == 0) {
                    throw new IllegalArgumentException(("newSize < 0: " + newSize).toString());
                }
                long bytesToSubtract = oldSize - newSize;
                while (true) {
                    if (bytesToSubtract <= 0) {
                        break;
                    }
                    Segment segment = buffer.head;
                    Intrinsics.checkNotNull(segment);
                    Segment tail = segment.prev;
                    Intrinsics.checkNotNull(tail);
                    int tailSize = tail.limit - tail.pos;
                    if (tailSize > bytesToSubtract) {
                        tail.limit -= (int) bytesToSubtract;
                        break;
                    }
                    buffer.head = tail.pop();
                    SegmentPool.recycle(tail);
                    bytesToSubtract -= tailSize;
                }
                this.segment = null;
                this.offset = newSize;
                this.data = null;
                this.start = -1;
                this.end = -1;
            } else if (newSize > oldSize) {
                boolean needsToSeek = true;
                long bytesToAdd = newSize - oldSize;
                for (long j = 0; bytesToAdd > j; j = 0) {
                    Segment tail2 = buffer.writableSegment$okio(segmentBytesToAdd);
                    int b$iv = 8192 - tail2.limit;
                    int segmentBytesToAdd = (int) Math.min(bytesToAdd, b$iv);
                    tail2.limit += segmentBytesToAdd;
                    bytesToAdd -= segmentBytesToAdd;
                    if (needsToSeek) {
                        this.segment = tail2;
                        this.offset = oldSize;
                        this.data = tail2.data;
                        this.start = tail2.limit - segmentBytesToAdd;
                        this.end = tail2.limit;
                        needsToSeek = false;
                    }
                    segmentBytesToAdd = 1;
                }
            }
            buffer.setSize$okio(newSize);
            return oldSize;
        }

        public final long expandBuffer(int minByteCount) {
            if (!(minByteCount > 0)) {
                throw new IllegalArgumentException(("minByteCount <= 0: " + minByteCount).toString());
            }
            if (!(minByteCount <= 8192)) {
                throw new IllegalArgumentException(("minByteCount > Segment.SIZE: " + minByteCount).toString());
            }
            Buffer buffer = this.buffer;
            if (buffer == null) {
                throw new IllegalStateException("not attached to a buffer".toString());
            }
            if (!this.readWrite) {
                throw new IllegalStateException("expandBuffer() only permitted for read/write buffers".toString());
            }
            long oldSize = buffer.size();
            Segment tail = buffer.writableSegment$okio(minByteCount);
            int result = 8192 - tail.limit;
            tail.limit = 8192;
            buffer.setSize$okio(result + oldSize);
            this.segment = tail;
            this.offset = oldSize;
            this.data = tail.data;
            this.start = 8192 - result;
            this.end = 8192;
            return result;
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable
        public void close() {
            if (!(this.buffer != null)) {
                throw new IllegalStateException("not attached to a buffer".toString());
            }
            this.buffer = null;
            this.segment = null;
            this.offset = -1L;
            this.data = null;
            this.start = -1;
            this.end = -1;
        }
    }
}
