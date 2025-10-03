package com.google.common.io;

import java.io.IOException;

/* loaded from: classes.dex */
public interface ByteProcessor<T> {
    T getResult();

    boolean processBytes(byte[] bArr, int i, int i2) throws IOException;
}
