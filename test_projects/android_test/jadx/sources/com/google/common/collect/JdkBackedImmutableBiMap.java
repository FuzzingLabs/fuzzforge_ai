package com.google.common.collect;

import com.google.common.collect.ImmutableMapEntrySet;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.Map;

/* loaded from: classes.dex */
final class JdkBackedImmutableBiMap<K, V> extends ImmutableBiMap<K, V> {
    private final Map<V, K> backwardDelegate;
    private final transient ImmutableList<Map.Entry<K, V>> entries;
    private final Map<K, V> forwardDelegate;

    @LazyInit
    private transient JdkBackedImmutableBiMap<V, K> inverse;

    static <K, V> ImmutableBiMap<K, V> create(int n, Map.Entry<K, V>[] entryArray) {
        Map<K, V> forwardDelegate = Maps.newHashMapWithExpectedSize(n);
        Map<V, K> backwardDelegate = Maps.newHashMapWithExpectedSize(n);
        for (int i = 0; i < n; i++) {
            Map.Entry<K, V> e = RegularImmutableMap.makeImmutable(entryArray[i]);
            entryArray[i] = e;
            V oldValue = forwardDelegate.putIfAbsent(e.getKey(), e.getValue());
            if (oldValue != null) {
                throw conflictException("key", e.getKey() + "=" + oldValue, entryArray[i]);
            }
            K oldKey = backwardDelegate.putIfAbsent(e.getValue(), e.getKey());
            if (oldKey != null) {
                throw conflictException("value", oldKey + "=" + e.getValue(), entryArray[i]);
            }
        }
        ImmutableList<Map.Entry<K, V>> entryList = ImmutableList.asImmutableList(entryArray, n);
        return new JdkBackedImmutableBiMap(entryList, forwardDelegate, backwardDelegate);
    }

    private JdkBackedImmutableBiMap(ImmutableList<Map.Entry<K, V>> entries, Map<K, V> forwardDelegate, Map<V, K> backwardDelegate) {
        this.entries = entries;
        this.forwardDelegate = forwardDelegate;
        this.backwardDelegate = backwardDelegate;
    }

    @Override // java.util.Map
    public int size() {
        return this.entries.size();
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.google.common.collect.ImmutableBiMap, com.google.common.collect.BiMap
    public ImmutableBiMap<V, K> inverse() {
        JdkBackedImmutableBiMap<V, K> result = this.inverse;
        if (result == null) {
            JdkBackedImmutableBiMap<V, K> result2 = new JdkBackedImmutableBiMap<>(new InverseEntries(), this.backwardDelegate, this.forwardDelegate);
            this.inverse = result2;
            result2.inverse = this;
            return result2;
        }
        return result;
    }

    private final class InverseEntries extends ImmutableList<Map.Entry<V, K>> {
        private InverseEntries() {
        }

        @Override // java.util.List
        public Map.Entry<V, K> get(int index) {
            Map.Entry<K, V> entry = (Map.Entry) JdkBackedImmutableBiMap.this.entries.get(index);
            return Maps.immutableEntry(entry.getValue(), entry.getKey());
        }

        @Override // com.google.common.collect.ImmutableCollection
        boolean isPartialView() {
            return false;
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.util.List
        public int size() {
            return JdkBackedImmutableBiMap.this.entries.size();
        }
    }

    @Override // com.google.common.collect.ImmutableMap, java.util.Map
    public V get(Object key) {
        return this.forwardDelegate.get(key);
    }

    @Override // com.google.common.collect.ImmutableMap
    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return new ImmutableMapEntrySet.RegularEntrySet(this, this.entries);
    }

    @Override // com.google.common.collect.ImmutableMap
    ImmutableSet<K> createKeySet() {
        return new ImmutableMapKeySet(this);
    }

    @Override // com.google.common.collect.ImmutableMap
    boolean isPartialView() {
        return false;
    }
}
