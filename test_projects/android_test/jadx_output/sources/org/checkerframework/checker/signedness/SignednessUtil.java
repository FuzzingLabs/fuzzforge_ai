package org.checkerframework.checker.signedness;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import okhttp3.internal.ws.WebSocketProtocol;

/* loaded from: classes11.dex */
public final class SignednessUtil {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    private SignednessUtil() {
        throw new Error("Do not instantiate");
    }

    public static ByteBuffer wrapUnsigned(byte[] array) {
        return ByteBuffer.wrap(array);
    }

    public static ByteBuffer wrapUnsigned(byte[] array, int offset, int length) {
        return ByteBuffer.wrap(array, offset, length);
    }

    public static int getUnsignedInt(ByteBuffer b) {
        return b.getInt();
    }

    public static short getUnsignedShort(ByteBuffer b) {
        return b.getShort();
    }

    public static byte getUnsigned(ByteBuffer b) {
        return b.get();
    }

    public static byte getUnsigned(ByteBuffer b, int i) {
        return b.get(i);
    }

    public static ByteBuffer getUnsigned(ByteBuffer b, byte[] bs, int i, int l) {
        return b.get(bs, i, l);
    }

    public static ByteBuffer putUnsigned(ByteBuffer b, byte ubyte) {
        return b.put(ubyte);
    }

    public static ByteBuffer putUnsigned(ByteBuffer b, int i, byte ubyte) {
        return b.put(i, ubyte);
    }

    public static IntBuffer putUnsigned(IntBuffer b, int uint) {
        return b.put(uint);
    }

    public static IntBuffer putUnsigned(IntBuffer b, int i, int uint) {
        return b.put(i, uint);
    }

    public static IntBuffer putUnsigned(IntBuffer b, int[] uints) {
        return b.put(uints);
    }

    public static IntBuffer putUnsigned(IntBuffer b, int[] uints, int i, int l) {
        return b.put(uints, i, l);
    }

    public static int getUnsigned(IntBuffer b, int i) {
        return b.get(i);
    }

    public static ByteBuffer putUnsignedShort(ByteBuffer b, short ushort) {
        return b.putShort(ushort);
    }

    public static ByteBuffer putUnsignedShort(ByteBuffer b, int i, short ushort) {
        return b.putShort(i, ushort);
    }

    public static ByteBuffer putUnsignedInt(ByteBuffer b, int uint) {
        return b.putInt(uint);
    }

    public static ByteBuffer putUnsignedInt(ByteBuffer b, int i, int uint) {
        return b.putInt(i, uint);
    }

    public static ByteBuffer putUnsignedLong(ByteBuffer b, int i, long ulong) {
        return b.putLong(i, ulong);
    }

    public static byte readUnsignedByte(RandomAccessFile f) throws IOException {
        return f.readByte();
    }

    public static char readUnsignedChar(RandomAccessFile f) throws IOException {
        return f.readChar();
    }

    public static short readUnsignedShort(RandomAccessFile f) throws IOException {
        return f.readShort();
    }

    public static int readUnsignedInt(RandomAccessFile f) throws IOException {
        return f.readInt();
    }

    public static long readUnsignedLong(RandomAccessFile f) throws IOException {
        return f.readLong();
    }

    public static int readUnsigned(RandomAccessFile f, byte[] b, int off, int len) throws IOException {
        return f.read(b, off, len);
    }

    public static void readFullyUnsigned(RandomAccessFile f, byte[] b) throws IOException {
        f.readFully(b);
    }

    public static void writeUnsigned(RandomAccessFile f, byte[] bs, int off, int len) throws IOException {
        f.write(bs, off, len);
    }

    public static void writeUnsignedByte(RandomAccessFile f, byte b) throws IOException {
        f.writeByte(toUnsignedInt(b));
    }

    public static void writeUnsignedChar(RandomAccessFile f, char c) throws IOException {
        f.writeChar(toUnsignedInt(c));
    }

    public static void writeUnsignedShort(RandomAccessFile f, short s) throws IOException {
        f.writeShort(toUnsignedInt(s));
    }

    public static void writeUnsignedInt(RandomAccessFile f, int i) throws IOException {
        f.writeInt(i);
    }

    public static void writeUnsignedLong(RandomAccessFile f, long l) throws IOException {
        f.writeLong(l);
    }

    public static void getUnsigned(ByteBuffer b, byte[] bs) {
        b.get(bs);
    }

    public static int compareUnsigned(long x, long y) {
        return Long.compare(x - Long.MIN_VALUE, Long.MIN_VALUE + y);
    }

    public static int compareUnsigned(int x, int y) {
        return Integer.compare(x - 2147483648, Integer.MIN_VALUE + y);
    }

    public static int compareUnsigned(short x, short y) {
        return compareUnsigned(toUnsignedInt(x), toUnsignedInt(y));
    }

    public static int compareUnsigned(byte x, byte y) {
        return compareUnsigned(toUnsignedInt(x), toUnsignedInt(y));
    }

    public static String toUnsignedString(long l) {
        return toUnsignedBigInteger(l).toString();
    }

    public static String toUnsignedString(long l, int radix) {
        return toUnsignedBigInteger(l).toString(radix);
    }

    public static String toUnsignedString(int i) {
        return Long.toString(toUnsignedLong(i));
    }

    public static String toUnsignedString(int i, int radix) {
        return Long.toString(toUnsignedLong(i), radix);
    }

    public static String toUnsignedString(short s) {
        return Long.toString(toUnsignedLong(s));
    }

    public static String toUnsignedString(short s, int radix) {
        return Long.toString(toUnsignedLong(s), radix);
    }

    public static String toUnsignedString(byte b) {
        return Long.toString(toUnsignedLong(b));
    }

    public static String toUnsignedString(byte b, int radix) {
        return Long.toString(toUnsignedLong(b), radix);
    }

    private static BigInteger toUnsignedBigInteger(long l) {
        if (l >= 0) {
            return BigInteger.valueOf(l);
        }
        int upper = (int) (l >>> 32);
        int lower = (int) l;
        return BigInteger.valueOf(toUnsignedLong(upper)).shiftLeft(32).add(BigInteger.valueOf(toUnsignedLong(lower)));
    }

    public static long toUnsignedLong(int i) {
        return i & 4294967295L;
    }

    public static long toUnsignedLong(short s) {
        return s & WebSocketProtocol.PAYLOAD_SHORT_MAX;
    }

    public static int toUnsignedInt(short s) {
        return 65535 & s;
    }

    public static long toUnsignedLong(byte b) {
        return b & 255;
    }

    public static int toUnsignedInt(byte b) {
        return b & 255;
    }

    public static short toUnsignedShort(byte b) {
        return (short) (b & 255);
    }

    public static long toUnsignedLong(char c) {
        return c & 255;
    }

    public static int toUnsignedInt(char c) {
        return c & 255;
    }

    public static short toUnsignedShort(char c) {
        return (short) (c & 255);
    }

    public static float toFloat(byte b) {
        return toUnsignedBigInteger(toUnsignedLong(b)).floatValue();
    }

    public static float toFloat(short s) {
        return toUnsignedBigInteger(toUnsignedLong(s)).floatValue();
    }

    public static float toFloat(int i) {
        return toUnsignedBigInteger(toUnsignedLong(i)).floatValue();
    }

    public static float toFloat(long l) {
        return toUnsignedBigInteger(l).floatValue();
    }

    public static double toDouble(byte b) {
        return toUnsignedBigInteger(toUnsignedLong(b)).doubleValue();
    }

    public static double toDouble(short s) {
        return toUnsignedBigInteger(toUnsignedLong(s)).doubleValue();
    }

    public static double toDouble(int i) {
        return toUnsignedBigInteger(toUnsignedLong(i)).doubleValue();
    }

    public static double toDouble(long l) {
        return toUnsignedBigInteger(l).doubleValue();
    }

    public static byte byteFromFloat(float f) {
        if (f < 0.0f) {
            throw new AssertionError();
        }
        return (byte) f;
    }

    public static short shortFromFloat(float f) {
        if (f < 0.0f) {
            throw new AssertionError();
        }
        return (short) f;
    }

    public static int intFromFloat(float f) {
        if (f < 0.0f) {
            throw new AssertionError();
        }
        return (int) f;
    }

    public static long longFromFloat(float f) {
        if (f < 0.0f) {
            throw new AssertionError();
        }
        return f;
    }

    public static byte byteFromDouble(double d) {
        if (d < 0.0d) {
            throw new AssertionError();
        }
        return (byte) d;
    }

    public static short shortFromDouble(double d) {
        if (d < 0.0d) {
            throw new AssertionError();
        }
        return (short) d;
    }

    public static int intFromDouble(double d) {
        if (d < 0.0d) {
            throw new AssertionError();
        }
        return (int) d;
    }

    public static long longFromDouble(double d) {
        if (d < 0.0d) {
            throw new AssertionError();
        }
        return (long) d;
    }
}
