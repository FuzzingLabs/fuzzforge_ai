package com.google.common.collect;

import com.android.tools.r8.annotations.SynthesizedClass;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public interface Multimap<K, V> {
    Map<K, Collection<V>> asMap();

    void clear();

    boolean containsEntry(Object obj, Object obj2);

    boolean containsKey(Object obj);

    boolean containsValue(Object obj);

    Collection<Map.Entry<K, V>> entries();

    boolean equals(Object obj);

    void forEach(BiConsumer<? super K, ? super V> biConsumer);

    Collection<V> get(K k);

    int hashCode();

    boolean isEmpty();

    Set<K> keySet();

    Multiset<K> keys();

    boolean put(K k, V v);

    boolean putAll(Multimap<? extends K, ? extends V> multimap);

    boolean putAll(K k, Iterable<? extends V> iterable);

    boolean remove(Object obj, Object obj2);

    Collection<V> removeAll(Object obj);

    Collection<V> replaceValues(K k, Iterable<? extends V> iterable);

    int size();

    Collection<V> values();

    @SynthesizedClass(kind = "$-CC")
    /* renamed from: com.google.common.collect.Multimap$-CC, reason: invalid class name */
    /* loaded from: classes.dex */
    public final /* synthetic */ class CC<K, V> {
        public static void $default$forEach(Multimap _this, final BiConsumer biConsumer) {
            Preconditions.checkNotNull(biConsumer);
            _this.entries().forEach(new Consumer() { // from class: com.google.common.collect.Multimap$$ExternalSyntheticLambda0
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    biConsumer.accept(r2.getKey(), ((Map.Entry) obj).getValue());
                }
            });
        }
    }
}
