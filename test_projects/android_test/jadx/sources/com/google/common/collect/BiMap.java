package com.google.common.collect;

import com.android.tools.r8.annotations.SynthesizedClass;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public interface BiMap<K, V> extends Map<K, V> {
    V forcePut(K k, V v);

    BiMap<V, K> inverse();

    V put(K k, V v);

    void putAll(Map<? extends K, ? extends V> map);

    @Override // java.util.Map
    Set<V> values();

    @SynthesizedClass(kind = "$-CC")
    /* renamed from: com.google.common.collect.BiMap$-CC, reason: invalid class name */
    public final /* synthetic */ class CC<K, V> {
    }
}
