package com.google.common.collect;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
final class WellBehavedMap<K, V> extends ForwardingMap<K, V> {
    private final Map<K, V> delegate;
    private Set<Map.Entry<K, V>> entrySet;

    private WellBehavedMap(Map<K, V> delegate) {
        this.delegate = delegate;
    }

    static <K, V> WellBehavedMap<K, V> wrap(Map<K, V> delegate) {
        return new WellBehavedMap<>(delegate);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.common.collect.ForwardingMap, com.google.common.collect.ForwardingObject
    public Map<K, V> delegate() {
        return this.delegate;
    }

    @Override // com.google.common.collect.ForwardingMap, java.util.Map
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es = this.entrySet;
        if (es != null) {
            return es;
        }
        EntrySet entrySet = new EntrySet();
        this.entrySet = entrySet;
        return entrySet;
    }

    private final class EntrySet extends Maps.EntrySet<K, V> {
        private EntrySet() {
        }

        @Override // com.google.common.collect.Maps.EntrySet
        Map<K, V> map() {
            return WellBehavedMap.this;
        }

        @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set
        public Iterator<Map.Entry<K, V>> iterator() {
            return new TransformedIterator<K, Map.Entry<K, V>>(WellBehavedMap.this.keySet().iterator()) { // from class: com.google.common.collect.WellBehavedMap.EntrySet.1
                @Override // com.google.common.collect.TransformedIterator
                /* bridge */ /* synthetic */ Object transform(Object obj) {
                    return transform((C12501) obj);
                }

                @Override // com.google.common.collect.TransformedIterator
                Map.Entry<K, V> transform(final K key) {
                    return new AbstractMapEntry<K, V>() { // from class: com.google.common.collect.WellBehavedMap.EntrySet.1.1
                        @Override // com.google.common.collect.AbstractMapEntry, java.util.Map.Entry
                        public K getKey() {
                            return (K) key;
                        }

                        @Override // com.google.common.collect.AbstractMapEntry, java.util.Map.Entry
                        public V getValue() {
                            return WellBehavedMap.this.get(key);
                        }

                        /* JADX WARN: Multi-variable type inference failed */
                        @Override // com.google.common.collect.AbstractMapEntry, java.util.Map.Entry
                        public V setValue(V v) {
                            return (V) WellBehavedMap.this.put(key, v);
                        }
                    };
                }
            };
        }
    }
}
