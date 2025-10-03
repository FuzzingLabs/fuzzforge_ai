package com.google.common.base;

@FunctionalInterface
/* loaded from: classes.dex */
public interface Function<F, T> extends java.util.function.Function<F, T> {
    @Override // java.util.function.Function
    T apply(F f);

    boolean equals(Object obj);
}
