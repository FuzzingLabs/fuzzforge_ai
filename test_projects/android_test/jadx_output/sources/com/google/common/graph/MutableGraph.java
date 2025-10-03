package com.google.common.graph;

/* loaded from: classes.dex */
public interface MutableGraph<N> extends Graph<N> {
    boolean addNode(N n);

    boolean putEdge(N n, N n2);

    boolean removeEdge(N n, N n2);

    boolean removeNode(N n);
}
