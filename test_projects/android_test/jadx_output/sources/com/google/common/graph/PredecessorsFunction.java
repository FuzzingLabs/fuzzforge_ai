package com.google.common.graph;

/* loaded from: classes.dex */
public interface PredecessorsFunction<N> {
    Iterable<? extends N> predecessors(N n);
}
