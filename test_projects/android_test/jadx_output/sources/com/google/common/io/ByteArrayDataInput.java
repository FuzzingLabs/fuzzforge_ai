package com.google.common.io;

import java.io.DataInput;

/* loaded from: classes.dex */
public interface ByteArrayDataInput extends DataInput {
    @Override // java.io.DataInput
    boolean readBoolean();

    @Override // java.io.DataInput
    byte readByte();

    @Override // java.io.DataInput
    char readChar();

    @Override // java.io.DataInput
    double readDouble();

    @Override // java.io.DataInput
    float readFloat();

    @Override // java.io.DataInput
    void readFully(byte[] bArr);

    @Override // java.io.DataInput
    void readFully(byte[] bArr, int i, int i2);

    @Override // java.io.DataInput
    int readInt();

    @Override // java.io.DataInput
    String readLine();

    @Override // java.io.DataInput
    long readLong();

    @Override // java.io.DataInput
    short readShort();

    @Override // java.io.DataInput
    String readUTF();

    @Override // java.io.DataInput
    int readUnsignedByte();

    @Override // java.io.DataInput
    int readUnsignedShort();

    @Override // java.io.DataInput
    int skipBytes(int i);
}
