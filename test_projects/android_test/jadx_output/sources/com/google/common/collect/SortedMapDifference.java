package com.google.common.collect;

import com.android.tools.r8.annotations.SynthesizedClass;
import com.google.common.collect.MapDifference;
import java.util.SortedMap;

/* loaded from: classes.dex */
public interface SortedMapDifference<K, V> extends MapDifference<K, V> {
    @Override // com.google.common.collect.MapDifference
    SortedMap<K, MapDifference.ValueDifference<V>> entriesDiffering();

    @Override // com.google.common.collect.MapDifference
    SortedMap<K, V> entriesInCommon();

    @Override // com.google.common.collect.MapDifference
    SortedMap<K, V> entriesOnlyOnLeft();

    @Override // com.google.common.collect.MapDifference
    SortedMap<K, V> entriesOnlyOnRight();

    @SynthesizedClass(kind = "$-CC")
    /* renamed from: com.google.common.collect.SortedMapDifference$-CC, reason: invalid class name */
    /* loaded from: classes.dex */
    public final /* synthetic */ class CC<K, V> {
    }
}
