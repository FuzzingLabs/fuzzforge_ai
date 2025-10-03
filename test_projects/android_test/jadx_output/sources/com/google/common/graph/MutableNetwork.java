package com.google.common.graph;

/* loaded from: classes.dex */
public interface MutableNetwork<N, E> extends Network<N, E> {
    boolean addEdge(N n, N n2, E e);

    boolean addNode(N n);

    boolean removeEdge(E e);

    boolean removeNode(N n);
}
