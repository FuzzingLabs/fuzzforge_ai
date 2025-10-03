package com.google.common.util.concurrent;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

/* loaded from: classes.dex */
public final class AtomicLongMap<K> implements Serializable {
    private transient Map<K, Long> asMap;
    private final ConcurrentHashMap<K, Long> map;

    private AtomicLongMap(ConcurrentHashMap<K, Long> map) {
        this.map = (ConcurrentHashMap) Preconditions.checkNotNull(map);
    }

    public static <K> AtomicLongMap<K> create() {
        return new AtomicLongMap<>(new ConcurrentHashMap());
    }

    public static <K> AtomicLongMap<K> create(Map<? extends K, ? extends Long> m) {
        AtomicLongMap<K> result = create();
        result.putAll(m);
        return result;
    }

    public long get(K key) {
        return this.map.getOrDefault(key, 0L).longValue();
    }

    public long incrementAndGet(K key) {
        return addAndGet(key, 1L);
    }

    public long decrementAndGet(K key) {
        return addAndGet(key, -1L);
    }

    public long addAndGet(K key, long delta) {
        return accumulateAndGet(key, delta, AtomicLongMap$$ExternalSyntheticLambda4.INSTANCE);
    }

    public long getAndIncrement(K key) {
        return getAndAdd(key, 1L);
    }

    public long getAndDecrement(K key) {
        return getAndAdd(key, -1L);
    }

    public long getAndAdd(K key, long delta) {
        return getAndAccumulate(key, delta, AtomicLongMap$$ExternalSyntheticLambda4.INSTANCE);
    }

    public long updateAndGet(K key, final LongUnaryOperator updaterFunction) {
        Preconditions.checkNotNull(updaterFunction);
        return this.map.compute(key, new BiFunction() { // from class: com.google.common.util.concurrent.AtomicLongMap$$ExternalSyntheticLambda3
            @Override // java.util.function.BiFunction
            public final Object apply(Object obj, Object obj2) {
                Long valueOf;
                valueOf = Long.valueOf(updaterFunction.applyAsLong(value == null ? 0L : ((Long) obj2).longValue()));
                return valueOf;
            }
        }).longValue();
    }

    public long getAndUpdate(K key, final LongUnaryOperator updaterFunction) {
        Preconditions.checkNotNull(updaterFunction);
        final AtomicLong holder = new AtomicLong();
        this.map.compute(key, new BiFunction() { // from class: com.google.common.util.concurrent.AtomicLongMap$$ExternalSyntheticLambda2
            @Override // java.util.function.BiFunction
            public final Object apply(Object obj, Object obj2) {
                return AtomicLongMap.lambda$getAndUpdate$1(holder, updaterFunction, obj, (Long) obj2);
            }
        });
        return holder.get();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ Long lambda$getAndUpdate$1(AtomicLong holder, LongUnaryOperator updaterFunction, Object k, Long value) {
        long oldValue = value == null ? 0L : value.longValue();
        holder.set(oldValue);
        return Long.valueOf(updaterFunction.applyAsLong(oldValue));
    }

    public long accumulateAndGet(K key, final long x, final LongBinaryOperator accumulatorFunction) {
        Preconditions.checkNotNull(accumulatorFunction);
        return updateAndGet(key, new LongUnaryOperator() { // from class: com.google.common.util.concurrent.AtomicLongMap$$ExternalSyntheticLambda6
            @Override // java.util.function.LongUnaryOperator
            public final long applyAsLong(long j) {
                long applyAsLong;
                applyAsLong = accumulatorFunction.applyAsLong(j, x);
                return applyAsLong;
            }
        });
    }

    public long getAndAccumulate(K key, final long x, final LongBinaryOperator accumulatorFunction) {
        Preconditions.checkNotNull(accumulatorFunction);
        return getAndUpdate(key, new LongUnaryOperator() { // from class: com.google.common.util.concurrent.AtomicLongMap$$ExternalSyntheticLambda7
            @Override // java.util.function.LongUnaryOperator
            public final long applyAsLong(long j) {
                long applyAsLong;
                applyAsLong = accumulatorFunction.applyAsLong(j, x);
                return applyAsLong;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ long lambda$put$4(long newValue, long x) {
        return newValue;
    }

    public long put(K key, final long newValue) {
        return getAndUpdate(key, new LongUnaryOperator() { // from class: com.google.common.util.concurrent.AtomicLongMap$$ExternalSyntheticLambda5
            @Override // java.util.function.LongUnaryOperator
            public final long applyAsLong(long j) {
                return AtomicLongMap.lambda$put$4(newValue, j);
            }
        });
    }

    public void putAll(Map<? extends K, ? extends Long> m) {
        m.forEach(new BiConsumer() { // from class: com.google.common.util.concurrent.AtomicLongMap$$ExternalSyntheticLambda0
            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                AtomicLongMap.this.put(obj, ((Long) obj2).longValue());
            }
        });
    }

    public long remove(K key) {
        Long result = this.map.remove(key);
        if (result == null) {
            return 0L;
        }
        return result.longValue();
    }

    boolean remove(K key, long value) {
        return this.map.remove(key, Long.valueOf(value));
    }

    public boolean removeIfZero(K key) {
        return remove(key, 0L);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ boolean lambda$removeAllZeros$5(Long x) {
        return x.longValue() == 0;
    }

    public void removeAllZeros() {
        this.map.values().removeIf(new Predicate() { // from class: com.google.common.util.concurrent.AtomicLongMap$$ExternalSyntheticLambda8
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AtomicLongMap.lambda$removeAllZeros$5((Long) obj);
            }
        });
    }

    public long sum() {
        return this.map.values().stream().mapToLong(new ToLongFunction() { // from class: com.google.common.util.concurrent.AtomicLongMap$$ExternalSyntheticLambda9
            @Override // java.util.function.ToLongFunction
            public final long applyAsLong(Object obj) {
                return ((Long) obj).longValue();
            }
        }).sum();
    }

    public Map<K, Long> asMap() {
        Map<K, Long> result = this.asMap;
        if (result != null) {
            return result;
        }
        Map<K, Long> createAsMap = createAsMap();
        this.asMap = createAsMap;
        return createAsMap;
    }

    private Map<K, Long> createAsMap() {
        return Collections.unmodifiableMap(this.map);
    }

    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    public int size() {
        return this.map.size();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public void clear() {
        this.map.clear();
    }

    public String toString() {
        return this.map.toString();
    }

    long putIfAbsent(K key, final long newValue) {
        final AtomicBoolean noValue = new AtomicBoolean(false);
        Long result = this.map.compute(key, new BiFunction() { // from class: com.google.common.util.concurrent.AtomicLongMap$$ExternalSyntheticLambda1
            @Override // java.util.function.BiFunction
            public final Object apply(Object obj, Object obj2) {
                return AtomicLongMap.lambda$putIfAbsent$6(noValue, newValue, obj, (Long) obj2);
            }
        });
        if (noValue.get()) {
            return 0L;
        }
        return result.longValue();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ Long lambda$putIfAbsent$6(AtomicBoolean noValue, long newValue, Object k, Long oldValue) {
        if (oldValue == null || oldValue.longValue() == 0) {
            noValue.set(true);
            return Long.valueOf(newValue);
        }
        return oldValue;
    }

    boolean replace(K key, long expectedOldValue, long newValue) {
        if (expectedOldValue == 0) {
            return putIfAbsent(key, newValue) == 0;
        }
        return this.map.replace(key, Long.valueOf(expectedOldValue), Long.valueOf(newValue));
    }
}
