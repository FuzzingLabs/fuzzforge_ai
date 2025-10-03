package com.google.common.collect;

import java.util.Map;

/* loaded from: classes.dex */
public interface ClassToInstanceMap<B> extends Map<Class<? extends B>, B> {
    <T extends B> T getInstance(Class<T> cls);

    <T extends B> T putInstance(Class<T> cls, T t);
}
