package com.google.common.collect;

import com.android.tools.r8.annotations.SynthesizedClass;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

/* loaded from: classes.dex */
public interface RowSortedTable<R, C, V> extends Table<R, C, V> {
    @Override // com.google.common.collect.Table
    SortedSet<R> rowKeySet();

    @Override // com.google.common.collect.Table
    SortedMap<R, Map<C, V>> rowMap();

    @SynthesizedClass(kind = "$-CC")
    /* renamed from: com.google.common.collect.RowSortedTable$-CC, reason: invalid class name */
    public final /* synthetic */ class CC<R, C, V> {
    }
}
