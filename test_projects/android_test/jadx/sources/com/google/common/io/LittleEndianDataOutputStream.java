package com.google.common.io;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/* loaded from: classes.dex */
public final class LittleEndianDataOutputStream extends FilterOutputStream implements DataOutput {
    public LittleEndianDataOutputStream(OutputStream out) {
        super(new DataOutputStream((OutputStream) Preconditions.checkNotNull(out)));
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.DataOutput
    public void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
    }

    @Override // java.io.DataOutput
    public void writeBoolean(boolean v) throws IOException {
        ((DataOutputStream) this.out).writeBoolean(v);
    }

    @Override // java.io.DataOutput
    public void writeByte(int v) throws IOException {
        ((DataOutputStream) this.out).writeByte(v);
    }

    @Override // java.io.DataOutput
    @Deprecated
    public void writeBytes(String s) throws IOException {
        ((DataOutputStream) this.out).writeBytes(s);
    }

    @Override // java.io.DataOutput
    public void writeChar(int v) throws IOException {
        writeShort(v);
    }

    @Override // java.io.DataOutput
    public void writeChars(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            writeChar(s.charAt(i));
        }
    }

    @Override // java.io.DataOutput
    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    @Override // java.io.DataOutput
    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    @Override // java.io.DataOutput
    public void writeInt(int v) throws IOException {
        this.out.write(v & 255);
        this.out.write((v >> 8) & 255);
        this.out.write((v >> 16) & 255);
        this.out.write((v >> 24) & 255);
    }

    @Override // java.io.DataOutput
    public void writeLong(long v) throws IOException {
        byte[] bytes = Longs.toByteArray(Long.reverseBytes(v));
        write(bytes, 0, bytes.length);
    }

    @Override // java.io.DataOutput
    public void writeShort(int v) throws IOException {
        this.out.write(v & 255);
        this.out.write((v >> 8) & 255);
    }

    @Override // java.io.DataOutput
    public void writeUTF(String str) throws IOException {
        ((DataOutputStream) this.out).writeUTF(str);
    }

    @Override // java.io.FilterOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.out.close();
    }
}
