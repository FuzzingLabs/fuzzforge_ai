package com.google.firebase.database.collection;

import java.lang.Comparable;
import java.util.Comparator;

/* loaded from: classes.dex */
public class StandardComparator<A extends Comparable<A>> implements Comparator<A> {
    private static StandardComparator INSTANCE = new StandardComparator();

    private StandardComparator() {
    }

    public static <T extends Comparable<T>> StandardComparator<T> getComparator(Class<T> clazz) {
        return INSTANCE;
    }

    @Override // java.util.Comparator
    public int compare(A o1, A o2) {
        return o1.compareTo(o2);
    }
}
