package com.google.common.collect;

import com.google.common.base.Preconditions;
import com.google.common.collect.CollectSpliterators;
import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import kotlin.jvm.internal.LongCompanionObject;

/* loaded from: classes.dex */
final class CollectSpliterators {
    private CollectSpliterators() {
    }

    static <T> Spliterator<T> indexed(int size, int extraCharacteristics, IntFunction<T> function) {
        return indexed(size, extraCharacteristics, function, null);
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [java.util.Spliterator$OfInt] */
    static <T> Spliterator<T> indexed(int size, int extraCharacteristics, IntFunction<T> function, Comparator<? super T> comparator) {
        if (comparator != null) {
            Preconditions.checkArgument((extraCharacteristics & 4) != 0);
        }
        return new C1WithCharacteristics(IntStream.range(0, size).spliterator(), function, extraCharacteristics, comparator);
    }

    /* JADX INFO: Add missing generic type declarations: [T] */
    /* renamed from: com.google.common.collect.CollectSpliterators$1WithCharacteristics, reason: invalid class name */
    class C1WithCharacteristics<T> implements Spliterator<T> {
        private final Spliterator.OfInt delegate;
        final /* synthetic */ Comparator val$comparator;
        final /* synthetic */ int val$extraCharacteristics;
        final /* synthetic */ IntFunction val$function;

        C1WithCharacteristics(Spliterator.OfInt delegate, IntFunction intFunction, int i, Comparator comparator) {
            this.val$function = intFunction;
            this.val$extraCharacteristics = i;
            this.val$comparator = comparator;
            this.delegate = delegate;
        }

        @Override // java.util.Spliterator
        public boolean tryAdvance(final Consumer<? super T> action) {
            Spliterator.OfInt ofInt = this.delegate;
            final IntFunction intFunction = this.val$function;
            return ofInt.tryAdvance(new IntConsumer() { // from class: com.google.common.collect.CollectSpliterators$1WithCharacteristics$$ExternalSyntheticLambda1
                @Override // java.util.function.IntConsumer
                public final void accept(int i) {
                    action.accept(intFunction.apply(i));
                }
            });
        }

        @Override // java.util.Spliterator
        public void forEachRemaining(final Consumer<? super T> action) {
            Spliterator.OfInt ofInt = this.delegate;
            final IntFunction intFunction = this.val$function;
            ofInt.forEachRemaining(new IntConsumer() { // from class: com.google.common.collect.CollectSpliterators$1WithCharacteristics$$ExternalSyntheticLambda0
                @Override // java.util.function.IntConsumer
                public final void accept(int i) {
                    action.accept(intFunction.apply(i));
                }
            });
        }

        @Override // java.util.Spliterator
        public Spliterator<T> trySplit() {
            Spliterator.OfInt split = this.delegate.trySplit();
            if (split == null) {
                return null;
            }
            return new C1WithCharacteristics(split, this.val$function, this.val$extraCharacteristics, this.val$comparator);
        }

        @Override // java.util.Spliterator
        public long estimateSize() {
            return this.delegate.estimateSize();
        }

        @Override // java.util.Spliterator
        public int characteristics() {
            return this.val$extraCharacteristics | 16464;
        }

        @Override // java.util.Spliterator
        public Comparator<? super T> getComparator() {
            if (hasCharacteristics(4)) {
                return this.val$comparator;
            }
            throw new IllegalStateException();
        }
    }

    static <F, T> Spliterator<T> map(Spliterator<F> fromSpliterator, Function<? super F, ? extends T> function) {
        Preconditions.checkNotNull(fromSpliterator);
        Preconditions.checkNotNull(function);
        return new C10511(fromSpliterator, function);
    }

    /* JADX INFO: Add missing generic type declarations: [T] */
    /* renamed from: com.google.common.collect.CollectSpliterators$1 */
    static class C10511<T> implements Spliterator<T> {
        final /* synthetic */ Spliterator val$fromSpliterator;
        final /* synthetic */ Function val$function;

        C10511(Spliterator spliterator, Function function) {
            this.val$fromSpliterator = spliterator;
            this.val$function = function;
        }

        @Override // java.util.Spliterator
        public boolean tryAdvance(final Consumer<? super T> action) {
            Spliterator spliterator = this.val$fromSpliterator;
            final Function function = this.val$function;
            return spliterator.tryAdvance(new Consumer() { // from class: com.google.common.collect.CollectSpliterators$1$$ExternalSyntheticLambda1
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    action.accept(function.apply(obj));
                }
            });
        }

        @Override // java.util.Spliterator
        public void forEachRemaining(final Consumer<? super T> action) {
            Spliterator spliterator = this.val$fromSpliterator;
            final Function function = this.val$function;
            spliterator.forEachRemaining(new Consumer() { // from class: com.google.common.collect.CollectSpliterators$1$$ExternalSyntheticLambda0
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    action.accept(function.apply(obj));
                }
            });
        }

        @Override // java.util.Spliterator
        public Spliterator<T> trySplit() {
            Spliterator<T> trySplit = this.val$fromSpliterator.trySplit();
            if (trySplit != null) {
                return CollectSpliterators.map(trySplit, this.val$function);
            }
            return null;
        }

        @Override // java.util.Spliterator
        public long estimateSize() {
            return this.val$fromSpliterator.estimateSize();
        }

        @Override // java.util.Spliterator
        public int characteristics() {
            return this.val$fromSpliterator.characteristics() & (-262);
        }
    }

    static <T> Spliterator<T> filter(Spliterator<T> fromSpliterator, Predicate<? super T> predicate) {
        Preconditions.checkNotNull(fromSpliterator);
        Preconditions.checkNotNull(predicate);
        return new C1Splitr(fromSpliterator, predicate);
    }

    /* JADX INFO: Add missing generic type declarations: [T] */
    /* renamed from: com.google.common.collect.CollectSpliterators$1Splitr, reason: invalid class name */
    class C1Splitr<T> implements Spliterator<T>, Consumer<T> {
        T holder = null;
        final /* synthetic */ Spliterator val$fromSpliterator;
        final /* synthetic */ Predicate val$predicate;

        C1Splitr(Spliterator spliterator, Predicate predicate) {
            this.val$fromSpliterator = spliterator;
            this.val$predicate = predicate;
        }

        @Override // java.util.function.Consumer
        public void accept(T t) {
            this.holder = t;
        }

        @Override // java.util.Spliterator
        public boolean tryAdvance(Consumer<? super T> consumer) {
            while (this.val$fromSpliterator.tryAdvance(this)) {
                try {
                    if (this.val$predicate.test(this.holder)) {
                        consumer.accept(this.holder);
                        return true;
                    }
                } finally {
                    this.holder = null;
                }
            }
            return false;
        }

        @Override // java.util.Spliterator
        public Spliterator<T> trySplit() {
            Spliterator<T> fromSplit = this.val$fromSpliterator.trySplit();
            if (fromSplit == null) {
                return null;
            }
            return CollectSpliterators.filter(fromSplit, this.val$predicate);
        }

        @Override // java.util.Spliterator
        public long estimateSize() {
            return this.val$fromSpliterator.estimateSize() / 2;
        }

        @Override // java.util.Spliterator
        public Comparator<? super T> getComparator() {
            return this.val$fromSpliterator.getComparator();
        }

        @Override // java.util.Spliterator
        public int characteristics() {
            return this.val$fromSpliterator.characteristics() & 277;
        }
    }

    static <F, T> Spliterator<T> flatMap(Spliterator<F> fromSpliterator, Function<? super F, Spliterator<T>> function, int topCharacteristics, long topSize) {
        Preconditions.checkArgument((topCharacteristics & 16384) == 0, "flatMap does not support SUBSIZED characteristic");
        Preconditions.checkArgument((topCharacteristics & 4) == 0, "flatMap does not support SORTED characteristic");
        Preconditions.checkNotNull(fromSpliterator);
        Preconditions.checkNotNull(function);
        return new C1FlatMapSpliterator(null, fromSpliterator, topCharacteristics, topSize, function);
    }

    /* JADX INFO: Add missing generic type declarations: [T] */
    /* renamed from: com.google.common.collect.CollectSpliterators$1FlatMapSpliterator, reason: invalid class name */
    class C1FlatMapSpliterator<T> implements Spliterator<T> {
        int characteristics;
        long estimatedSize;
        final Spliterator<F> from;
        Spliterator<T> prefix;
        final /* synthetic */ Function val$function;

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Not initialized variable reg: 6, insn: 0x0000: IPUT (r6 I:java.util.function.Function), (r0 I:com.google.common.collect.CollectSpliterators$1FlatMapSpliterator) (LINE:217) com.google.common.collect.CollectSpliterators.1FlatMapSpliterator.val$function java.util.function.Function, block:B:1:0x0000 */
        C1FlatMapSpliterator(Spliterator spliterator, Spliterator<T> spliterator2, Spliterator<F> spliterator3, int i, long j) {
            Function function;
            this.val$function = function;
            this.prefix = spliterator;
            this.from = spliterator2;
            this.characteristics = spliterator3;
            this.estimatedSize = i;
        }

        @Override // java.util.Spliterator
        public boolean tryAdvance(Consumer<? super T> action) {
            Spliterator<F> spliterator;
            final Function function;
            do {
                Spliterator<T> spliterator2 = this.prefix;
                if (spliterator2 != null && spliterator2.tryAdvance(action)) {
                    long j = this.estimatedSize;
                    if (j != LongCompanionObject.MAX_VALUE) {
                        this.estimatedSize = j - 1;
                        return true;
                    }
                    return true;
                }
                this.prefix = null;
                spliterator = this.from;
                function = this.val$function;
            } while (spliterator.tryAdvance(new Consumer() { // from class: com.google.common.collect.CollectSpliterators$1FlatMapSpliterator$$ExternalSyntheticLambda0
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    CollectSpliterators.C1FlatMapSpliterator.this.m57x2277fef6(function, obj);
                }
            }));
            return false;
        }

        /* renamed from: lambda$tryAdvance$0$com-google-common-collect-CollectSpliterators$1FlatMapSpliterator */
        public /* synthetic */ void m57x2277fef6(Function function, Object fromElement) {
            this.prefix = (Spliterator) function.apply(fromElement);
        }

        @Override // java.util.Spliterator
        public void forEachRemaining(final Consumer<? super T> action) {
            Spliterator<T> spliterator = this.prefix;
            if (spliterator != null) {
                spliterator.forEachRemaining(action);
                this.prefix = null;
            }
            Spliterator<F> spliterator2 = this.from;
            final Function function = this.val$function;
            spliterator2.forEachRemaining(new Consumer() { // from class: com.google.common.collect.CollectSpliterators$1FlatMapSpliterator$$ExternalSyntheticLambda1
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((Spliterator) function.apply(obj)).forEachRemaining(action);
                }
            });
            this.estimatedSize = 0L;
        }

        @Override // java.util.Spliterator
        public Spliterator<T> trySplit() {
            long estSplitSize;
            Spliterator trySplit = this.from.trySplit();
            if (trySplit != null) {
                int splitCharacteristics = this.characteristics & (-65);
                long estSplitSize2 = estimateSize();
                if (estSplitSize2 >= LongCompanionObject.MAX_VALUE) {
                    estSplitSize = estSplitSize2;
                } else {
                    long estSplitSize3 = estSplitSize2 / 2;
                    this.estimatedSize -= estSplitSize3;
                    this.characteristics = splitCharacteristics;
                    estSplitSize = estSplitSize3;
                }
                Spliterator<T> result = new C1FlatMapSpliterator<>(this.prefix, trySplit, splitCharacteristics, estSplitSize, this.val$function);
                this.prefix = null;
                return result;
            }
            Spliterator<T> result2 = this.prefix;
            if (result2 == null) {
                return null;
            }
            Spliterator<T> result3 = this.prefix;
            this.prefix = null;
            return result3;
        }

        @Override // java.util.Spliterator
        public long estimateSize() {
            Spliterator<T> spliterator = this.prefix;
            if (spliterator != null) {
                this.estimatedSize = Math.max(this.estimatedSize, spliterator.estimateSize());
            }
            return Math.max(this.estimatedSize, 0L);
        }

        @Override // java.util.Spliterator
        public int characteristics() {
            return this.characteristics;
        }
    }
}
