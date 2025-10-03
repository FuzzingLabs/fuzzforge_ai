package com.google.common.collect;

/* loaded from: classes.dex */
class EmptyImmutableListMultimap extends ImmutableListMultimap<Object, Object> {
    static final EmptyImmutableListMultimap INSTANCE = new EmptyImmutableListMultimap();
    private static final long serialVersionUID = 0;

    private EmptyImmutableListMultimap() {
        super(ImmutableMap.m92of(), 0);
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
