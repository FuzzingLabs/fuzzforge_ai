package com.google.common.collect;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Iterator;

/* loaded from: classes.dex */
final class ReverseNaturalOrdering extends Ordering<Comparable> implements Serializable {
    static final ReverseNaturalOrdering INSTANCE = new ReverseNaturalOrdering();
    private static final long serialVersionUID = 0;

    @Override // com.google.common.collect.Ordering, java.util.Comparator
    public int compare(Comparable left, Comparable right) {
        Preconditions.checkNotNull(left);
        if (left == right) {
            return 0;
        }
        return right.compareTo(left);
    }

    @Override // com.google.common.collect.Ordering
    public <S extends Comparable> Ordering<S> reverse() {
        return Ordering.natural();
    }

    @Override // com.google.common.collect.Ordering
    public <E extends Comparable> E min(E a, E b) {
        return (E) NaturalOrdering.INSTANCE.max(a, b);
    }

    @Override // com.google.common.collect.Ordering
    public <E extends Comparable> E min(E a, E b, E c, E... rest) {
        return (E) NaturalOrdering.INSTANCE.max(a, b, c, rest);
    }

    @Override // com.google.common.collect.Ordering
    public <E extends Comparable> E min(Iterator<E> iterator) {
        return (E) NaturalOrdering.INSTANCE.max(iterator);
    }

    @Override // com.google.common.collect.Ordering
    public <E extends Comparable> E min(Iterable<E> iterable) {
        return (E) NaturalOrdering.INSTANCE.max(iterable);
    }

    @Override // com.google.common.collect.Ordering
    public <E extends Comparable> E max(E a, E b) {
        return (E) NaturalOrdering.INSTANCE.min(a, b);
    }

    @Override // com.google.common.collect.Ordering
    public <E extends Comparable> E max(E a, E b, E c, E... rest) {
        return (E) NaturalOrdering.INSTANCE.min(a, b, c, rest);
    }

    @Override // com.google.common.collect.Ordering
    public <E extends Comparable> E max(Iterator<E> iterator) {
        return (E) NaturalOrdering.INSTANCE.min(iterator);
    }

    @Override // com.google.common.collect.Ordering
    public <E extends Comparable> E max(Iterable<E> iterable) {
        return (E) NaturalOrdering.INSTANCE.min(iterable);
    }

    private Object readResolve() {
        return INSTANCE;
    }

    public String toString() {
        return "Ordering.natural().reverse()";
    }

    private ReverseNaturalOrdering() {
    }
}
