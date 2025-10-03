package com.google.common.collect;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.common.math.LongMath;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/* loaded from: classes.dex */
public final class Streams {

    public interface DoubleFunctionWithIndex<R> {
        R apply(double d, long j);
    }

    public interface FunctionWithIndex<T, R> {
        R apply(T t, long j);
    }

    public interface IntFunctionWithIndex<R> {
        R apply(int i, long j);
    }

    public interface LongFunctionWithIndex<R> {
        R apply(long j, long j2);
    }

    public static <T> Stream<T> stream(Iterable<T> iterable) {
        if (iterable instanceof Collection) {
            return ((Collection) iterable).stream();
        }
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    @Deprecated
    public static <T> Stream<T> stream(Collection<T> collection) {
        return collection.stream();
    }

    public static <T> Stream<T> stream(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }

    public static <T> Stream<T> stream(Optional<T> optional) {
        return optional.isPresent() ? Stream.of(optional.get()) : Stream.of(new Object[0]);
    }

    public static <T> Stream<T> stream(java.util.Optional<T> optional) {
        return optional.isPresent() ? Stream.of(optional.get()) : Stream.of(new Object[0]);
    }

    public static IntStream stream(OptionalInt optional) {
        return optional.isPresent() ? IntStream.of(optional.getAsInt()) : IntStream.empty();
    }

    public static LongStream stream(OptionalLong optional) {
        return optional.isPresent() ? LongStream.of(optional.getAsLong()) : LongStream.empty();
    }

    public static DoubleStream stream(OptionalDouble optional) {
        return optional.isPresent() ? DoubleStream.of(optional.getAsDouble()) : DoubleStream.empty();
    }

    @SafeVarargs
    public static <T> Stream<T> concat(final Stream<? extends T>... streams) {
        boolean isParallel = false;
        int characteristics = 336;
        long estimatedSize = 0;
        ImmutableList.Builder<Spliterator<? extends T>> splitrsBuilder = new ImmutableList.Builder<>(streams.length);
        for (Stream<? extends T> stream : streams) {
            isParallel |= stream.isParallel();
            Spliterator<? extends T> splitr = stream.spliterator();
            splitrsBuilder.add((ImmutableList.Builder<Spliterator<? extends T>>) splitr);
            characteristics &= splitr.characteristics();
            estimatedSize = LongMath.saturatedAdd(estimatedSize, splitr.estimateSize());
        }
        return (Stream) StreamSupport.stream(CollectSpliterators.flatMap(splitrsBuilder.build().spliterator(), new Function() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda10
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return Streams.lambda$concat$0((Spliterator) obj);
            }
        }, characteristics, estimatedSize), isParallel).onClose(new Runnable() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                Streams.lambda$concat$1(streams);
            }
        });
    }

    static /* synthetic */ Spliterator lambda$concat$0(Spliterator splitr) {
        return splitr;
    }

    static /* synthetic */ void lambda$concat$1(Stream[] streams) {
        for (Stream stream : streams) {
            stream.close();
        }
    }

    public static IntStream concat(IntStream... streams) {
        return Stream.of((Object[]) streams).flatMapToInt(new Function() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda1
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return Streams.lambda$concat$2((IntStream) obj);
            }
        });
    }

    static /* synthetic */ IntStream lambda$concat$2(IntStream stream) {
        return stream;
    }

    public static LongStream concat(LongStream... streams) {
        return Stream.of((Object[]) streams).flatMapToLong(new Function() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda2
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return Streams.lambda$concat$3((LongStream) obj);
            }
        });
    }

    static /* synthetic */ LongStream lambda$concat$3(LongStream stream) {
        return stream;
    }

    public static DoubleStream concat(DoubleStream... streams) {
        return Stream.of((Object[]) streams).flatMapToDouble(new Function() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda11
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return Streams.lambda$concat$4((DoubleStream) obj);
            }
        });
    }

    static /* synthetic */ DoubleStream lambda$concat$4(DoubleStream stream) {
        return stream;
    }

    public static <A, B, R> Stream<R> zip(Stream<A> streamA, Stream<B> streamB, final BiFunction<? super A, ? super B, R> function) {
        Preconditions.checkNotNull(streamA);
        Preconditions.checkNotNull(streamB);
        Preconditions.checkNotNull(function);
        boolean isParallel = streamA.isParallel() || streamB.isParallel();
        Spliterator<A> splitrA = streamA.spliterator();
        Spliterator<B> splitrB = streamB.spliterator();
        int characteristics = splitrA.characteristics() & splitrB.characteristics() & 80;
        final Iterator<A> itrA = Spliterators.iterator(splitrA);
        final Iterator<B> itrB = Spliterators.iterator(splitrB);
        Stream stream = StreamSupport.stream(new Spliterators.AbstractSpliterator<R>(Math.min(splitrA.estimateSize(), splitrB.estimateSize()), characteristics) { // from class: com.google.common.collect.Streams.1
            @Override // java.util.Spliterator
            public boolean tryAdvance(Consumer<? super R> consumer) {
                if (itrA.hasNext() && itrB.hasNext()) {
                    consumer.accept((Object) function.apply(itrA.next(), itrB.next()));
                    return true;
                }
                return false;
            }
        }, isParallel);
        streamA.getClass();
        Stream stream2 = (Stream) stream.onClose(new Streams$$ExternalSyntheticLambda5(streamA));
        streamB.getClass();
        return (Stream) stream2.onClose(new Streams$$ExternalSyntheticLambda5(streamB));
    }

    public static <A, B> void forEachPair(Stream<A> streamA, Stream<B> streamB, final BiConsumer<? super A, ? super B> consumer) {
        Preconditions.checkNotNull(consumer);
        if (streamA.isParallel() || streamB.isParallel()) {
            zip(streamA, streamB, new BiFunction() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda7
                @Override // java.util.function.BiFunction
                public final Object apply(Object obj, Object obj2) {
                    return new Streams.TemporaryPair(obj, obj2);
                }
            }).forEach(new Consumer() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda9
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    consumer.accept(r2.f199a, ((Streams.TemporaryPair) obj).f200b);
                }
            });
            return;
        }
        Iterator<A> iterA = streamA.iterator();
        Iterator<B> iterB = streamB.iterator();
        while (iterA.hasNext() && iterB.hasNext()) {
            consumer.accept(iterA.next(), iterB.next());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    static class TemporaryPair<A, B> {

        /* renamed from: a */
        final A f199a;

        /* renamed from: b */
        final B f200b;

        TemporaryPair(A a, B b) {
            this.f199a = a;
            this.f200b = b;
        }
    }

    public static <T, R> Stream<R> mapWithIndex(Stream<T> stream, final FunctionWithIndex<? super T, ? extends R> function) {
        Preconditions.checkNotNull(stream);
        Preconditions.checkNotNull(function);
        boolean isParallel = stream.isParallel();
        Spliterator<T> fromSpliterator = stream.spliterator();
        if (!fromSpliterator.hasCharacteristics(16384)) {
            final Iterator<T> fromIterator = Spliterators.iterator(fromSpliterator);
            Stream stream2 = StreamSupport.stream(new Spliterators.AbstractSpliterator<R>(fromSpliterator.estimateSize(), fromSpliterator.characteristics() & 80) { // from class: com.google.common.collect.Streams.2
                long index = 0;

                @Override // java.util.Spliterator
                public boolean tryAdvance(Consumer<? super R> consumer) {
                    if (fromIterator.hasNext()) {
                        FunctionWithIndex functionWithIndex = function;
                        Object next = fromIterator.next();
                        long j = this.index;
                        this.index = 1 + j;
                        consumer.accept((Object) functionWithIndex.apply(next, j));
                        return true;
                    }
                    return false;
                }
            }, isParallel);
            stream.getClass();
            return (Stream) stream2.onClose(new Streams$$ExternalSyntheticLambda5(stream));
        }
        Stream stream3 = StreamSupport.stream(new C1Splitr(fromSpliterator, 0L, function), isParallel);
        stream.getClass();
        return (Stream) stream3.onClose(new Streams$$ExternalSyntheticLambda5(stream));
    }

    /* JADX INFO: Add missing generic type declarations: [R, T] */
    /* renamed from: com.google.common.collect.Streams$1Splitr, reason: invalid class name */
    class C1Splitr<R, T> extends MapWithIndexSpliterator<Spliterator<T>, R, C1Splitr> implements Consumer<T> {
        T holder;
        final /* synthetic */ FunctionWithIndex val$function;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Not initialized variable reg: 4, insn: 0x0000: IPUT (r4 I:com.google.common.collect.Streams$FunctionWithIndex), (r0 I:com.google.common.collect.Streams$1Splitr) (LINE:378) com.google.common.collect.Streams.1Splitr.val$function com.google.common.collect.Streams$FunctionWithIndex, block:B:1:0x0000 */
        C1Splitr(Spliterator spliterator, Spliterator<T> spliterator2, long j) {
            super(spliterator, spliterator2);
            FunctionWithIndex functionWithIndex;
            this.val$function = functionWithIndex;
        }

        @Override // java.util.function.Consumer
        public void accept(T t) {
            this.holder = t;
        }

        @Override // java.util.Spliterator
        public boolean tryAdvance(Consumer<? super R> consumer) {
            if (this.fromSpliterator.tryAdvance(this)) {
                try {
                    FunctionWithIndex functionWithIndex = this.val$function;
                    T t = this.holder;
                    long j = this.index;
                    this.index = 1 + j;
                    consumer.accept((Object) functionWithIndex.apply(t, j));
                    return true;
                } finally {
                    this.holder = null;
                }
            }
            return false;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // com.google.common.collect.Streams.MapWithIndexSpliterator
        public C1Splitr createSplit(Spliterator<T> from, long i) {
            return new C1Splitr(from, i, this.val$function);
        }
    }

    /* JADX WARN: Type inference failed for: r1v0, types: [java.util.Spliterator$OfInt] */
    public static <R> Stream<R> mapWithIndex(final IntStream stream, final IntFunctionWithIndex<R> function) {
        Preconditions.checkNotNull(stream);
        Preconditions.checkNotNull(function);
        boolean isParallel = stream.isParallel();
        ?? spliterator = stream.spliterator();
        if (!spliterator.hasCharacteristics(16384)) {
            final PrimitiveIterator.OfInt fromIterator = Spliterators.iterator((Spliterator.OfInt) spliterator);
            Stream stream2 = StreamSupport.stream(new Spliterators.AbstractSpliterator<R>(spliterator.estimateSize(), spliterator.characteristics() & 80) { // from class: com.google.common.collect.Streams.3
                long index = 0;

                @Override // java.util.Spliterator
                public boolean tryAdvance(Consumer<? super R> consumer) {
                    if (fromIterator.hasNext()) {
                        IntFunctionWithIndex intFunctionWithIndex = function;
                        int nextInt = fromIterator.nextInt();
                        long j = this.index;
                        this.index = 1 + j;
                        consumer.accept((Object) intFunctionWithIndex.apply(nextInt, j));
                        return true;
                    }
                    return false;
                }
            }, isParallel);
            stream.getClass();
            return (Stream) stream2.onClose(new Runnable() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    stream.close();
                }
            });
        }
        Stream stream3 = StreamSupport.stream(new C2Splitr(spliterator, 0L, function), isParallel);
        stream.getClass();
        return (Stream) stream3.onClose(new Runnable() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                stream.close();
            }
        });
    }

    /* JADX INFO: Add missing generic type declarations: [R] */
    /* renamed from: com.google.common.collect.Streams$2Splitr, reason: invalid class name */
    class C2Splitr<R> extends MapWithIndexSpliterator<Spliterator.OfInt, R, C2Splitr> implements IntConsumer, Spliterator<R> {
        int holder;
        final /* synthetic */ IntFunctionWithIndex val$function;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Not initialized variable reg: 4, insn: 0x0000: IPUT (r4 I:com.google.common.collect.Streams$IntFunctionWithIndex), (r0 I:com.google.common.collect.Streams$2Splitr) (LINE:460) com.google.common.collect.Streams.2Splitr.val$function com.google.common.collect.Streams$IntFunctionWithIndex, block:B:1:0x0000 */
        C2Splitr(Spliterator.OfInt splitr, Spliterator.OfInt ofInt, long j) {
            super(splitr, ofInt);
            IntFunctionWithIndex intFunctionWithIndex;
            this.val$function = intFunctionWithIndex;
        }

        @Override // java.util.function.IntConsumer
        public void accept(int t) {
            this.holder = t;
        }

        @Override // java.util.Spliterator
        public boolean tryAdvance(Consumer<? super R> consumer) {
            if (((Spliterator.OfInt) this.fromSpliterator).tryAdvance((IntConsumer) this)) {
                IntFunctionWithIndex intFunctionWithIndex = this.val$function;
                int i = this.holder;
                long j = this.index;
                this.index = 1 + j;
                consumer.accept((Object) intFunctionWithIndex.apply(i, j));
                return true;
            }
            return false;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.Streams.MapWithIndexSpliterator
        public C2Splitr createSplit(Spliterator.OfInt from, long i) {
            return new C2Splitr(from, i, this.val$function);
        }
    }

    /* JADX WARN: Type inference failed for: r1v0, types: [java.util.Spliterator$OfLong] */
    public static <R> Stream<R> mapWithIndex(final LongStream stream, final LongFunctionWithIndex<R> function) {
        Preconditions.checkNotNull(stream);
        Preconditions.checkNotNull(function);
        boolean isParallel = stream.isParallel();
        ?? spliterator = stream.spliterator();
        if (!spliterator.hasCharacteristics(16384)) {
            final PrimitiveIterator.OfLong fromIterator = Spliterators.iterator((Spliterator.OfLong) spliterator);
            Stream stream2 = StreamSupport.stream(new Spliterators.AbstractSpliterator<R>(spliterator.estimateSize(), spliterator.characteristics() & 80) { // from class: com.google.common.collect.Streams.4
                long index = 0;

                @Override // java.util.Spliterator
                public boolean tryAdvance(Consumer<? super R> consumer) {
                    if (fromIterator.hasNext()) {
                        LongFunctionWithIndex longFunctionWithIndex = function;
                        long nextLong = fromIterator.nextLong();
                        long j = this.index;
                        this.index = 1 + j;
                        consumer.accept((Object) longFunctionWithIndex.apply(nextLong, j));
                        return true;
                    }
                    return false;
                }
            }, isParallel);
            stream.getClass();
            return (Stream) stream2.onClose(new Runnable() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    stream.close();
                }
            });
        }
        Stream stream3 = StreamSupport.stream(new C3Splitr(spliterator, 0L, function), isParallel);
        stream.getClass();
        return (Stream) stream3.onClose(new Runnable() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                stream.close();
            }
        });
    }

    /* JADX INFO: Add missing generic type declarations: [R] */
    /* renamed from: com.google.common.collect.Streams$3Splitr, reason: invalid class name */
    class C3Splitr<R> extends MapWithIndexSpliterator<Spliterator.OfLong, R, C3Splitr> implements LongConsumer, Spliterator<R> {
        long holder;
        final /* synthetic */ LongFunctionWithIndex val$function;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Not initialized variable reg: 4, insn: 0x0000: IPUT (r4 I:com.google.common.collect.Streams$LongFunctionWithIndex), (r0 I:com.google.common.collect.Streams$3Splitr) (LINE:538) com.google.common.collect.Streams.3Splitr.val$function com.google.common.collect.Streams$LongFunctionWithIndex, block:B:1:0x0000 */
        C3Splitr(Spliterator.OfLong splitr, Spliterator.OfLong ofLong, long j) {
            super(splitr, ofLong);
            LongFunctionWithIndex longFunctionWithIndex;
            this.val$function = longFunctionWithIndex;
        }

        @Override // java.util.function.LongConsumer
        public void accept(long t) {
            this.holder = t;
        }

        @Override // java.util.Spliterator
        public boolean tryAdvance(Consumer<? super R> consumer) {
            if (((Spliterator.OfLong) this.fromSpliterator).tryAdvance((LongConsumer) this)) {
                LongFunctionWithIndex longFunctionWithIndex = this.val$function;
                long j = this.holder;
                long j2 = this.index;
                this.index = 1 + j2;
                consumer.accept((Object) longFunctionWithIndex.apply(j, j2));
                return true;
            }
            return false;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.Streams.MapWithIndexSpliterator
        public C3Splitr createSplit(Spliterator.OfLong from, long i) {
            return new C3Splitr(from, i, this.val$function);
        }
    }

    /* JADX WARN: Type inference failed for: r1v0, types: [java.util.Spliterator$OfDouble] */
    public static <R> Stream<R> mapWithIndex(final DoubleStream stream, final DoubleFunctionWithIndex<R> function) {
        Preconditions.checkNotNull(stream);
        Preconditions.checkNotNull(function);
        boolean isParallel = stream.isParallel();
        ?? spliterator = stream.spliterator();
        if (!spliterator.hasCharacteristics(16384)) {
            final PrimitiveIterator.OfDouble fromIterator = Spliterators.iterator((Spliterator.OfDouble) spliterator);
            Stream stream2 = StreamSupport.stream(new Spliterators.AbstractSpliterator<R>(spliterator.estimateSize(), spliterator.characteristics() & 80) { // from class: com.google.common.collect.Streams.5
                long index = 0;

                @Override // java.util.Spliterator
                public boolean tryAdvance(Consumer<? super R> consumer) {
                    if (fromIterator.hasNext()) {
                        DoubleFunctionWithIndex doubleFunctionWithIndex = function;
                        double nextDouble = fromIterator.nextDouble();
                        long j = this.index;
                        this.index = 1 + j;
                        consumer.accept((Object) doubleFunctionWithIndex.apply(nextDouble, j));
                        return true;
                    }
                    return false;
                }
            }, isParallel);
            stream.getClass();
            return (Stream) stream2.onClose(new Runnable() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    stream.close();
                }
            });
        }
        Stream stream3 = StreamSupport.stream(new C4Splitr(spliterator, 0L, function), isParallel);
        stream.getClass();
        return (Stream) stream3.onClose(new Runnable() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                stream.close();
            }
        });
    }

    /* JADX INFO: Add missing generic type declarations: [R] */
    /* renamed from: com.google.common.collect.Streams$4Splitr, reason: invalid class name */
    class C4Splitr<R> extends MapWithIndexSpliterator<Spliterator.OfDouble, R, C4Splitr> implements DoubleConsumer, Spliterator<R> {
        double holder;
        final /* synthetic */ DoubleFunctionWithIndex val$function;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Not initialized variable reg: 4, insn: 0x0000: IPUT (r4 I:com.google.common.collect.Streams$DoubleFunctionWithIndex), (r0 I:com.google.common.collect.Streams$4Splitr) (LINE:617) com.google.common.collect.Streams.4Splitr.val$function com.google.common.collect.Streams$DoubleFunctionWithIndex, block:B:1:0x0000 */
        C4Splitr(Spliterator.OfDouble splitr, Spliterator.OfDouble ofDouble, long j) {
            super(splitr, ofDouble);
            DoubleFunctionWithIndex doubleFunctionWithIndex;
            this.val$function = doubleFunctionWithIndex;
        }

        @Override // java.util.function.DoubleConsumer
        public void accept(double t) {
            this.holder = t;
        }

        @Override // java.util.Spliterator
        public boolean tryAdvance(Consumer<? super R> consumer) {
            if (((Spliterator.OfDouble) this.fromSpliterator).tryAdvance((DoubleConsumer) this)) {
                DoubleFunctionWithIndex doubleFunctionWithIndex = this.val$function;
                double d = this.holder;
                long j = this.index;
                this.index = 1 + j;
                consumer.accept((Object) doubleFunctionWithIndex.apply(d, j));
                return true;
            }
            return false;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.Streams.MapWithIndexSpliterator
        public C4Splitr createSplit(Spliterator.OfDouble from, long i) {
            return new C4Splitr(from, i, this.val$function);
        }
    }

    private static abstract class MapWithIndexSpliterator<F extends Spliterator<?>, R, S extends MapWithIndexSpliterator<F, R, S>> implements Spliterator<R> {
        final F fromSpliterator;
        long index;

        abstract S createSplit(F f, long j);

        MapWithIndexSpliterator(F fromSpliterator, long index) {
            this.fromSpliterator = fromSpliterator;
            this.index = index;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // java.util.Spliterator
        public S trySplit() {
            Spliterator trySplit = this.fromSpliterator.trySplit();
            if (trySplit == null) {
                return null;
            }
            S s = (S) createSplit(trySplit, this.index);
            this.index += trySplit.getExactSizeIfKnown();
            return s;
        }

        @Override // java.util.Spliterator
        public long estimateSize() {
            return this.fromSpliterator.estimateSize();
        }

        @Override // java.util.Spliterator
        public int characteristics() {
            return this.fromSpliterator.characteristics() & 16464;
        }
    }

    /* renamed from: com.google.common.collect.Streams$1OptionalState, reason: invalid class name */
    class C1OptionalState {
        boolean set = false;
        T value = null;

        C1OptionalState() {
        }

        void set(T t) {
            this.set = true;
            this.value = t;
        }

        T get() {
            Preconditions.checkState(this.set);
            return this.value;
        }
    }

    public static <T> java.util.Optional<T> findLast(Stream<T> stream) {
        final C1OptionalState state = new C1OptionalState();
        Deque<Spliterator<T>> splits = new ArrayDeque<>();
        splits.addLast(stream.spliterator());
        while (!splits.isEmpty()) {
            Spliterator<T> spliterator = splits.removeLast();
            if (spliterator.getExactSizeIfKnown() != 0) {
                if (spliterator.hasCharacteristics(16384)) {
                    while (true) {
                        Spliterator<T> prefix = spliterator.trySplit();
                        if (prefix == null || prefix.getExactSizeIfKnown() == 0) {
                            break;
                        }
                        if (spliterator.getExactSizeIfKnown() == 0) {
                            spliterator = prefix;
                            break;
                        }
                    }
                    state.getClass();
                    spliterator.forEachRemaining(new Consumer() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda8
                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            Streams.C1OptionalState.this.set(obj);
                        }
                    });
                    return java.util.Optional.of(state.get());
                }
                Spliterator<T> prefix2 = spliterator.trySplit();
                if (prefix2 == null || prefix2.getExactSizeIfKnown() == 0) {
                    state.getClass();
                    spliterator.forEachRemaining(new Consumer() { // from class: com.google.common.collect.Streams$$ExternalSyntheticLambda8
                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            Streams.C1OptionalState.this.set(obj);
                        }
                    });
                    if (state.set) {
                        return java.util.Optional.of(state.get());
                    }
                } else {
                    splits.addLast(prefix2);
                    splits.addLast(spliterator);
                }
            }
        }
        return java.util.Optional.empty();
    }

    public static OptionalInt findLast(IntStream stream) {
        java.util.Optional<Integer> boxedLast = findLast(stream.boxed());
        return boxedLast.isPresent() ? OptionalInt.of(boxedLast.get().intValue()) : OptionalInt.empty();
    }

    public static OptionalLong findLast(LongStream stream) {
        java.util.Optional<Long> boxedLast = findLast(stream.boxed());
        return boxedLast.isPresent() ? OptionalLong.of(boxedLast.get().longValue()) : OptionalLong.empty();
    }

    public static OptionalDouble findLast(DoubleStream stream) {
        java.util.Optional<Double> boxedLast = findLast(stream.boxed());
        return boxedLast.isPresent() ? OptionalDouble.of(boxedLast.get().doubleValue()) : OptionalDouble.empty();
    }

    private Streams() {
    }
}
