package com.google.common.util.concurrent;

@FunctionalInterface
/* loaded from: classes.dex */
public interface AsyncFunction<I, O> {
    ListenableFuture<O> apply(I i) throws Exception;
}
