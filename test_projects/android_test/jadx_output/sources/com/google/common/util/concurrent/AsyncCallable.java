package com.google.common.util.concurrent;

@FunctionalInterface
/* loaded from: classes.dex */
public interface AsyncCallable<V> {
    ListenableFuture<V> call() throws Exception;
}
