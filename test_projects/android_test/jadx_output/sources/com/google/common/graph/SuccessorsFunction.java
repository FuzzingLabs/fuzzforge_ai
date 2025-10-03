package com.google.common.graph;

/* loaded from: classes.dex */
public interface SuccessorsFunction<N> {
    Iterable<? extends N> successors(N n);
}
