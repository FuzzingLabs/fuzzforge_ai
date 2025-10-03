package com.google.common.io;

import java.io.IOException;

/* loaded from: classes.dex */
public interface LineProcessor<T> {
    T getResult();

    boolean processLine(String str) throws IOException;
}
