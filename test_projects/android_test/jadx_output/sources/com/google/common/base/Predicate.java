package com.google.common.base;

import com.android.tools.r8.annotations.SynthesizedClass;

@FunctionalInterface
/* loaded from: classes.dex */
public interface Predicate<T> extends java.util.function.Predicate<T> {
    boolean apply(T t);

    boolean equals(Object obj);

    @Override // java.util.function.Predicate
    boolean test(T t);

    @SynthesizedClass(kind = "$-CC")
    /* renamed from: com.google.common.base.Predicate$-CC, reason: invalid class name */
    /* loaded from: classes.dex */
    public final /* synthetic */ class CC<T> {
    }
}
