package com.google.common.collect;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.primitives.Ints;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.Arrays;
import java.util.Collection;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class RegularImmutableMultiset<E> extends ImmutableMultiset<E> {
    static final ImmutableMultiset<Object> EMPTY = create(ImmutableList.m73of());
    static final double HASH_FLOODING_FPP = 0.001d;
    static final int MAX_HASH_BUCKET_LENGTH = 9;
    static final double MAX_LOAD_FACTOR = 1.0d;

    @LazyInit
    private transient ImmutableSet<E> elementSet;
    private final transient Multisets.ImmutableEntry<E>[] entries;
    private final transient int hashCode;
    private final transient Multisets.ImmutableEntry<E>[] hashTable;
    private final transient int size;

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <E> ImmutableMultiset<E> create(Collection<? extends Multiset.Entry<? extends E>> entries) {
        int distinct;
        Multisets.ImmutableEntry<E> newEntry;
        int distinct2 = entries.size();
        Multisets.ImmutableEntry<E>[] entryArray = new Multisets.ImmutableEntry[distinct2];
        if (distinct2 == 0) {
            return new RegularImmutableMultiset(entryArray, null, 0, 0, ImmutableSet.m116of());
        }
        int tableSize = Hashing.closedTableSize(distinct2, 1.0d);
        int mask = tableSize - 1;
        Multisets.ImmutableEntry<E>[] hashTable = new Multisets.ImmutableEntry[tableSize];
        int index = 0;
        int hashCode = 0;
        long size = 0;
        for (Multiset.Entry<? extends E> entry : entries) {
            Object checkNotNull = Preconditions.checkNotNull(entry.getElement());
            int count = entry.getCount();
            int hash = checkNotNull.hashCode();
            int bucket = Hashing.smear(hash) & mask;
            Multisets.ImmutableEntry<E> bucketHead = hashTable[bucket];
            if (bucketHead == null) {
                distinct = distinct2;
                boolean canReuseEntry = (entry instanceof Multisets.ImmutableEntry) && !(entry instanceof NonTerminalEntry);
                newEntry = canReuseEntry ? (Multisets.ImmutableEntry) entry : new Multisets.ImmutableEntry<>(checkNotNull, count);
            } else {
                distinct = distinct2;
                newEntry = new NonTerminalEntry<>(checkNotNull, count, bucketHead);
            }
            hashCode += hash ^ count;
            entryArray[index] = newEntry;
            hashTable[bucket] = newEntry;
            size += count;
            distinct2 = distinct;
            index++;
        }
        if (hashFloodingDetected(hashTable)) {
            return JdkBackedImmutableMultiset.create(ImmutableList.asImmutableList(entryArray));
        }
        return new RegularImmutableMultiset(entryArray, hashTable, Ints.saturatedCast(size), hashCode, null);
    }

    private static boolean hashFloodingDetected(Multisets.ImmutableEntry<?>[] hashTable) {
        for (Multisets.ImmutableEntry<?> entry : hashTable) {
            int bucketLength = 0;
            for (; entry != null; entry = entry.nextInBucket()) {
                bucketLength++;
                if (bucketLength > 9) {
                    return true;
                }
            }
        }
        return false;
    }

    private RegularImmutableMultiset(Multisets.ImmutableEntry<E>[] entries, Multisets.ImmutableEntry<E>[] hashTable, int size, int hashCode, ImmutableSet<E> elementSet) {
        this.entries = entries;
        this.hashTable = hashTable;
        this.size = size;
        this.hashCode = hashCode;
        this.elementSet = elementSet;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static final class NonTerminalEntry<E> extends Multisets.ImmutableEntry<E> {
        private final Multisets.ImmutableEntry<E> nextInBucket;

        NonTerminalEntry(E element, int count, Multisets.ImmutableEntry<E> nextInBucket) {
            super(element, count);
            this.nextInBucket = nextInBucket;
        }

        @Override // com.google.common.collect.Multisets.ImmutableEntry
        public Multisets.ImmutableEntry<E> nextInBucket() {
            return this.nextInBucket;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.common.collect.ImmutableCollection
    public boolean isPartialView() {
        return false;
    }

    @Override // com.google.common.collect.Multiset
    public int count(Object element) {
        Multisets.ImmutableEntry<E>[] hashTable = this.hashTable;
        if (element == null || hashTable == null) {
            return 0;
        }
        int hash = Hashing.smearedHash(element);
        int mask = hashTable.length - 1;
        for (Multisets.ImmutableEntry<E> entry = hashTable[hash & mask]; entry != null; entry = entry.nextInBucket()) {
            if (Objects.equal(element, entry.getElement())) {
                return entry.getCount();
            }
        }
        return 0;
    }

    @Override // java.util.AbstractCollection, java.util.Collection, com.google.common.collect.Multiset
    public int size() {
        return this.size;
    }

    @Override // com.google.common.collect.ImmutableMultiset, com.google.common.collect.Multiset
    public ImmutableSet<E> elementSet() {
        ImmutableSet<E> result = this.elementSet;
        if (result != null) {
            return result;
        }
        ImmutableMultiset.ElementSet elementSet = new ImmutableMultiset.ElementSet(Arrays.asList(this.entries), this);
        this.elementSet = elementSet;
        return elementSet;
    }

    @Override // com.google.common.collect.ImmutableMultiset
    Multiset.Entry<E> getEntry(int index) {
        return this.entries[index];
    }

    @Override // com.google.common.collect.ImmutableMultiset, java.util.Collection, com.google.common.collect.Multiset
    public int hashCode() {
        return this.hashCode;
    }
}
