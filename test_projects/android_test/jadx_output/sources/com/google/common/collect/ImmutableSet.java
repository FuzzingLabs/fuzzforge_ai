package com.google.common.collect;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.math.IntMath;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.Serializable;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Collector;

/* loaded from: classes.dex */
public abstract class ImmutableSet<E> extends ImmutableCollection<E> implements Set<E> {
    private static final int CUTOFF = 751619276;
    private static final double DESIRED_LOAD_FACTOR = 0.7d;
    static final double HASH_FLOODING_FPP = 0.001d;
    static final int MAX_RUN_MULTIPLIER = 12;
    static final int MAX_TABLE_SIZE = 1073741824;
    static final int SPLITERATOR_CHARACTERISTICS = 1297;

    @LazyInit
    private transient ImmutableList<E> asList;

    @Override // com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet, com.google.common.collect.SortedIterable
    public abstract UnmodifiableIterator<E> iterator();

    public static <E> Collector<E, ?, ImmutableSet<E>> toImmutableSet() {
        return CollectCollectors.toImmutableSet();
    }

    /* renamed from: of */
    public static <E> ImmutableSet<E> m116of() {
        return RegularImmutableSet.EMPTY;
    }

    /* renamed from: of */
    public static <E> ImmutableSet<E> m117of(E element) {
        return new SingletonImmutableSet(element);
    }

    /* renamed from: of */
    public static <E> ImmutableSet<E> m118of(E e1, E e2) {
        return construct(2, e1, e2);
    }

    /* renamed from: of */
    public static <E> ImmutableSet<E> m119of(E e1, E e2, E e3) {
        return construct(3, e1, e2, e3);
    }

    /* renamed from: of */
    public static <E> ImmutableSet<E> m120of(E e1, E e2, E e3, E e4) {
        return construct(4, e1, e2, e3, e4);
    }

    /* renamed from: of */
    public static <E> ImmutableSet<E> m121of(E e1, E e2, E e3, E e4, E e5) {
        return construct(5, e1, e2, e3, e4, e5);
    }

    @SafeVarargs
    /* renamed from: of */
    public static <E> ImmutableSet<E> m122of(E e1, E e2, E e3, E e4, E e5, E e6, E... others) {
        Preconditions.checkArgument(others.length <= 2147483641, "the total number of elements must fit in an int");
        Object[] elements = new Object[others.length + 6];
        elements[0] = e1;
        elements[1] = e2;
        elements[2] = e3;
        elements[3] = e4;
        elements[4] = e5;
        elements[5] = e6;
        System.arraycopy(others, 0, elements, 6, others.length);
        return construct(elements.length, elements);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static <E> ImmutableSet<E> construct(int i, Object... objArr) {
        switch (i) {
            case 0:
                return m116of();
            case 1:
                return m117of(objArr[0]);
            default:
                int i2 = 0;
                SetBuilderImpl setBuilderImpl = new RegularSetBuilderImpl(4);
                while (i2 < i) {
                    SetBuilderImpl add = setBuilderImpl.add(Preconditions.checkNotNull(objArr[i2]));
                    i2++;
                    setBuilderImpl = add;
                }
                return setBuilderImpl.review().build();
        }
    }

    public static <E> ImmutableSet<E> copyOf(Collection<? extends E> elements) {
        if ((elements instanceof ImmutableSet) && !(elements instanceof SortedSet)) {
            ImmutableSet<E> set = (ImmutableSet) elements;
            if (!set.isPartialView()) {
                return set;
            }
        } else if (elements instanceof EnumSet) {
            return copyOfEnumSet((EnumSet) elements);
        }
        Object[] array = elements.toArray();
        return construct(array.length, array);
    }

    public static <E> ImmutableSet<E> copyOf(Iterable<? extends E> elements) {
        if (elements instanceof Collection) {
            return copyOf((Collection) elements);
        }
        return copyOf(elements.iterator());
    }

    public static <E> ImmutableSet<E> copyOf(Iterator<? extends E> elements) {
        if (!elements.hasNext()) {
            return m116of();
        }
        E first = elements.next();
        if (!elements.hasNext()) {
            return m117of((Object) first);
        }
        return new Builder().add((Builder) first).addAll((Iterator) elements).build();
    }

    public static <E> ImmutableSet<E> copyOf(E[] elements) {
        switch (elements.length) {
            case 0:
                return m116of();
            case 1:
                return m117of((Object) elements[0]);
            default:
                return construct(elements.length, (Object[]) elements.clone());
        }
    }

    private static ImmutableSet copyOfEnumSet(EnumSet enumSet) {
        return ImmutableEnumSet.asImmutable(EnumSet.copyOf(enumSet));
    }

    boolean isHashCodeFast() {
        return false;
    }

    @Override // java.util.Collection, java.util.Set
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if ((object instanceof ImmutableSet) && isHashCodeFast() && ((ImmutableSet) object).isHashCodeFast() && hashCode() != object.hashCode()) {
            return false;
        }
        return Sets.equalsImpl(this, object);
    }

    @Override // java.util.Collection, java.util.Set
    public int hashCode() {
        return Sets.hashCodeImpl(this);
    }

    @Override // com.google.common.collect.ImmutableCollection
    public ImmutableList<E> asList() {
        ImmutableList<E> result = this.asList;
        if (result != null) {
            return result;
        }
        ImmutableList<E> createAsList = createAsList();
        this.asList = createAsList;
        return createAsList;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableList<E> createAsList() {
        return new RegularImmutableAsList(this, toArray());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static abstract class Indexed<E> extends ImmutableSet<E> {
        /* JADX INFO: Access modifiers changed from: package-private */
        public abstract E get(int i);

        @Override // com.google.common.collect.ImmutableSet, com.google.common.collect.ImmutableCollection, java.util.AbstractCollection, java.util.Collection, java.lang.Iterable, java.util.Set, java.util.NavigableSet, com.google.common.collect.SortedIterable
        public UnmodifiableIterator<E> iterator() {
            return asList().iterator();
        }

        @Override // com.google.common.collect.ImmutableCollection, java.util.Collection, java.lang.Iterable
        public Spliterator<E> spliterator() {
            return CollectSpliterators.indexed(size(), ImmutableSet.SPLITERATOR_CHARACTERISTICS, new IntFunction() { // from class: com.google.common.collect.ImmutableSet$Indexed$$ExternalSyntheticLambda0
                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return ImmutableSet.Indexed.this.get(i);
                }
            });
        }

        @Override // java.lang.Iterable
        public void forEach(Consumer<? super E> consumer) {
            Preconditions.checkNotNull(consumer);
            int n = size();
            for (int i = 0; i < n; i++) {
                consumer.accept(get(i));
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        @Override // com.google.common.collect.ImmutableCollection
        public int copyIntoArray(Object[] dst, int offset) {
            return asList().copyIntoArray(dst, offset);
        }

        @Override // com.google.common.collect.ImmutableSet
        ImmutableList<E> createAsList() {
            return new ImmutableAsList<E>() { // from class: com.google.common.collect.ImmutableSet.Indexed.1
                @Override // java.util.List
                public E get(int i) {
                    return (E) Indexed.this.get(i);
                }

                /* JADX INFO: Access modifiers changed from: package-private */
                @Override // com.google.common.collect.ImmutableAsList
                public Indexed<E> delegateCollection() {
                    return Indexed.this;
                }
            };
        }
    }

    /* loaded from: classes.dex */
    private static class SerializedForm implements Serializable {
        private static final long serialVersionUID = 0;
        final Object[] elements;

        SerializedForm(Object[] elements) {
            this.elements = elements;
        }

        Object readResolve() {
            return ImmutableSet.copyOf(this.elements);
        }
    }

    @Override // com.google.common.collect.ImmutableCollection
    Object writeReplace() {
        return new SerializedForm(toArray());
    }

    public static <E> Builder<E> builder() {
        return new Builder<>();
    }

    public static <E> Builder<E> builderWithExpectedSize(int expectedSize) {
        CollectPreconditions.checkNonnegative(expectedSize, "expectedSize");
        return new Builder<>(expectedSize);
    }

    static Object[] rebuildHashTable(int newTableSize, Object[] elements, int n) {
        int index;
        Object[] hashTable = new Object[newTableSize];
        int mask = hashTable.length - 1;
        for (int i = 0; i < n; i++) {
            Object e = elements[i];
            int j0 = Hashing.smear(e.hashCode());
            int j = j0;
            while (true) {
                index = j & mask;
                if (hashTable[index] == null) {
                    break;
                }
                j++;
            }
            hashTable[index] = e;
        }
        return hashTable;
    }

    /* loaded from: classes.dex */
    public static class Builder<E> extends ImmutableCollection.Builder<E> {
        boolean forceCopy;
        private SetBuilderImpl<E> impl;

        /* JADX WARN: Multi-variable type inference failed */
        @Override // com.google.common.collect.ImmutableCollection.Builder
        public /* bridge */ /* synthetic */ ImmutableCollection.Builder add(Object obj) {
            return add((Builder<E>) obj);
        }

        public Builder() {
            this(4);
        }

        Builder(int capacity) {
            this.impl = new RegularSetBuilderImpl(capacity);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public Builder(boolean subclass) {
            this.impl = null;
        }

        void forceJdk() {
            this.impl = new JdkBackedSetBuilderImpl(this.impl);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public final void copyIfNecessary() {
            if (this.forceCopy) {
                copy();
                this.forceCopy = false;
            }
        }

        void copy() {
            this.impl = this.impl.copy();
        }

        @Override // com.google.common.collect.ImmutableCollection.Builder
        public Builder<E> add(E element) {
            Preconditions.checkNotNull(element);
            copyIfNecessary();
            this.impl = this.impl.add(element);
            return this;
        }

        @Override // com.google.common.collect.ImmutableCollection.Builder
        public Builder<E> add(E... elements) {
            super.add((Object[]) elements);
            return this;
        }

        @Override // com.google.common.collect.ImmutableCollection.Builder
        public Builder<E> addAll(Iterable<? extends E> elements) {
            super.addAll((Iterable) elements);
            return this;
        }

        @Override // com.google.common.collect.ImmutableCollection.Builder
        public Builder<E> addAll(Iterator<? extends E> elements) {
            super.addAll((Iterator) elements);
            return this;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public Builder<E> combine(Builder<E> other) {
            copyIfNecessary();
            this.impl = this.impl.combine(other.impl);
            return this;
        }

        @Override // com.google.common.collect.ImmutableCollection.Builder
        public ImmutableSet<E> build() {
            this.forceCopy = true;
            SetBuilderImpl<E> review = this.impl.review();
            this.impl = review;
            return review.build();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static abstract class SetBuilderImpl<E> {
        E[] dedupedElements;
        int distinct;

        abstract SetBuilderImpl<E> add(E e);

        abstract ImmutableSet<E> build();

        abstract SetBuilderImpl<E> copy();

        SetBuilderImpl(int i) {
            this.dedupedElements = (E[]) new Object[i];
            this.distinct = 0;
        }

        SetBuilderImpl(SetBuilderImpl<E> setBuilderImpl) {
            E[] eArr = setBuilderImpl.dedupedElements;
            this.dedupedElements = (E[]) Arrays.copyOf(eArr, eArr.length);
            this.distinct = setBuilderImpl.distinct;
        }

        private void ensureCapacity(int i) {
            E[] eArr = this.dedupedElements;
            if (i > eArr.length) {
                this.dedupedElements = (E[]) Arrays.copyOf(this.dedupedElements, ImmutableCollection.Builder.expandedCapacity(eArr.length, i));
            }
        }

        final void addDedupedElement(E e) {
            ensureCapacity(this.distinct + 1);
            E[] eArr = this.dedupedElements;
            int i = this.distinct;
            this.distinct = i + 1;
            eArr[i] = e;
        }

        final SetBuilderImpl<E> combine(SetBuilderImpl<E> other) {
            SetBuilderImpl<E> result = this;
            for (int i = 0; i < other.distinct; i++) {
                result = result.add(other.dedupedElements[i]);
            }
            return result;
        }

        SetBuilderImpl<E> review() {
            return this;
        }
    }

    static int chooseTableSize(int setSize) {
        int setSize2 = Math.max(setSize, 2);
        if (setSize2 < CUTOFF) {
            int tableSize = Integer.highestOneBit(setSize2 - 1) << 1;
            while (tableSize * DESIRED_LOAD_FACTOR < setSize2) {
                tableSize <<= 1;
            }
            return tableSize;
        }
        Preconditions.checkArgument(setSize2 < 1073741824, "collection too large");
        return 1073741824;
    }

    static boolean hashFloodingDetected(Object[] hashTable) {
        int maxRunBeforeFallback = maxRunBeforeFallback(hashTable.length);
        int endOfStartRun = 0;
        while (endOfStartRun < hashTable.length && hashTable[endOfStartRun] != null) {
            endOfStartRun++;
            if (endOfStartRun > maxRunBeforeFallback) {
                return true;
            }
        }
        int startOfEndRun = hashTable.length - 1;
        while (startOfEndRun > endOfStartRun && hashTable[startOfEndRun] != null) {
            if (((hashTable.length - 1) - startOfEndRun) + endOfStartRun > maxRunBeforeFallback) {
                return true;
            }
            startOfEndRun--;
        }
        int i = endOfStartRun + 1;
        while (i < startOfEndRun) {
            int runLength = 0;
            while (i < startOfEndRun && hashTable[i] != null) {
                runLength++;
                if (runLength > maxRunBeforeFallback) {
                    return true;
                }
                i++;
            }
            i++;
        }
        return false;
    }

    static int maxRunBeforeFallback(int tableSize) {
        return IntMath.log2(tableSize, RoundingMode.UNNECESSARY) * 12;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class RegularSetBuilderImpl<E> extends SetBuilderImpl<E> {
        private int expandTableThreshold;
        private int hashCode;
        private Object[] hashTable;
        private int maxRunBeforeFallback;

        RegularSetBuilderImpl(int expectedCapacity) {
            super(expectedCapacity);
            int tableSize = ImmutableSet.chooseTableSize(expectedCapacity);
            this.hashTable = new Object[tableSize];
            this.maxRunBeforeFallback = ImmutableSet.maxRunBeforeFallback(tableSize);
            this.expandTableThreshold = (int) (tableSize * ImmutableSet.DESIRED_LOAD_FACTOR);
        }

        RegularSetBuilderImpl(RegularSetBuilderImpl<E> toCopy) {
            super(toCopy);
            Object[] objArr = toCopy.hashTable;
            this.hashTable = Arrays.copyOf(objArr, objArr.length);
            this.maxRunBeforeFallback = toCopy.maxRunBeforeFallback;
            this.expandTableThreshold = toCopy.expandTableThreshold;
            this.hashCode = toCopy.hashCode;
        }

        void ensureTableCapacity(int minCapacity) {
            if (minCapacity > this.expandTableThreshold) {
                Object[] objArr = this.hashTable;
                if (objArr.length < 1073741824) {
                    int newTableSize = objArr.length * 2;
                    this.hashTable = ImmutableSet.rebuildHashTable(newTableSize, this.dedupedElements, this.distinct);
                    this.maxRunBeforeFallback = ImmutableSet.maxRunBeforeFallback(newTableSize);
                    this.expandTableThreshold = (int) (newTableSize * ImmutableSet.DESIRED_LOAD_FACTOR);
                }
            }
        }

        @Override // com.google.common.collect.ImmutableSet.SetBuilderImpl
        SetBuilderImpl<E> add(E e) {
            Preconditions.checkNotNull(e);
            int eHash = e.hashCode();
            int i0 = Hashing.smear(eHash);
            int mask = this.hashTable.length - 1;
            for (int i = i0; i - i0 < this.maxRunBeforeFallback; i++) {
                int index = i & mask;
                Object tableEntry = this.hashTable[index];
                if (tableEntry == null) {
                    addDedupedElement(e);
                    this.hashTable[index] = e;
                    this.hashCode += eHash;
                    ensureTableCapacity(this.distinct);
                    return this;
                }
                if (tableEntry.equals(e)) {
                    return this;
                }
            }
            return new JdkBackedSetBuilderImpl(this).add(e);
        }

        @Override // com.google.common.collect.ImmutableSet.SetBuilderImpl
        SetBuilderImpl<E> copy() {
            return new RegularSetBuilderImpl(this);
        }

        @Override // com.google.common.collect.ImmutableSet.SetBuilderImpl
        SetBuilderImpl<E> review() {
            int targetTableSize = ImmutableSet.chooseTableSize(this.distinct);
            if (targetTableSize * 2 < this.hashTable.length) {
                this.hashTable = ImmutableSet.rebuildHashTable(targetTableSize, this.dedupedElements, this.distinct);
            }
            return ImmutableSet.hashFloodingDetected(this.hashTable) ? new JdkBackedSetBuilderImpl(this) : this;
        }

        @Override // com.google.common.collect.ImmutableSet.SetBuilderImpl
        ImmutableSet<E> build() {
            switch (this.distinct) {
                case 0:
                    return ImmutableSet.m116of();
                case 1:
                    return ImmutableSet.m117of((Object) this.dedupedElements[0]);
                default:
                    Object[] elements = this.distinct == this.dedupedElements.length ? this.dedupedElements : Arrays.copyOf(this.dedupedElements, this.distinct);
                    return new RegularImmutableSet(elements, this.hashCode, this.hashTable, r3.length - 1);
            }
        }
    }

    /* loaded from: classes.dex */
    private static final class JdkBackedSetBuilderImpl<E> extends SetBuilderImpl<E> {
        private final Set<Object> delegate;

        JdkBackedSetBuilderImpl(SetBuilderImpl<E> toCopy) {
            super(toCopy);
            this.delegate = Sets.newHashSetWithExpectedSize(this.distinct);
            for (int i = 0; i < this.distinct; i++) {
                this.delegate.add(this.dedupedElements[i]);
            }
        }

        @Override // com.google.common.collect.ImmutableSet.SetBuilderImpl
        SetBuilderImpl<E> add(E e) {
            Preconditions.checkNotNull(e);
            if (this.delegate.add(e)) {
                addDedupedElement(e);
            }
            return this;
        }

        @Override // com.google.common.collect.ImmutableSet.SetBuilderImpl
        SetBuilderImpl<E> copy() {
            return new JdkBackedSetBuilderImpl(this);
        }

        @Override // com.google.common.collect.ImmutableSet.SetBuilderImpl
        ImmutableSet<E> build() {
            switch (this.distinct) {
                case 0:
                    return ImmutableSet.m116of();
                case 1:
                    return ImmutableSet.m117of((Object) this.dedupedElements[0]);
                default:
                    return new JdkBackedImmutableSet(this.delegate, ImmutableList.asImmutableList(this.dedupedElements, this.distinct));
            }
        }
    }
}
