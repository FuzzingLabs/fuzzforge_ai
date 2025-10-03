package com.google.common.graph;

/* loaded from: classes.dex */
public interface MutableValueGraph<N, V> extends ValueGraph<N, V> {
    boolean addNode(N n);

    V putEdgeValue(N n, N n2, V v);

    V removeEdge(N n, N n2);

    boolean removeNode(N n);
}
