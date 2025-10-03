package com.google.common.collect;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ImmutableMapEntry<K, V> extends ImmutableEntry<K, V> {
    /* JADX INFO: Access modifiers changed from: package-private */
    public static <K, V> ImmutableMapEntry<K, V>[] createEntryArray(int size) {
        return new ImmutableMapEntry[size];
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableMapEntry(K key, V value) {
        super(key, value);
        CollectPreconditions.checkEntryNotNull(key, value);
    }

    ImmutableMapEntry(ImmutableMapEntry<K, V> contents) {
        super(contents.getKey(), contents.getValue());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableMapEntry<K, V> getNextInKeyBucket() {
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ImmutableMapEntry<K, V> getNextInValueBucket() {
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isReusable() {
        return true;
    }

    /* loaded from: classes.dex */
    static class NonTerminalImmutableMapEntry<K, V> extends ImmutableMapEntry<K, V> {
        private final transient ImmutableMapEntry<K, V> nextInKeyBucket;

        /* JADX INFO: Access modifiers changed from: package-private */
        public NonTerminalImmutableMapEntry(K key, V value, ImmutableMapEntry<K, V> nextInKeyBucket) {
            super(key, value);
            this.nextInKeyBucket = nextInKeyBucket;
        }

        @Override // com.google.common.collect.ImmutableMapEntry
        final ImmutableMapEntry<K, V> getNextInKeyBucket() {
            return this.nextInKeyBucket;
        }

        @Override // com.google.common.collect.ImmutableMapEntry
        final boolean isReusable() {
            return false;
        }
    }

    /* loaded from: classes.dex */
    static final class NonTerminalImmutableBiMapEntry<K, V> extends NonTerminalImmutableMapEntry<K, V> {
        private final transient ImmutableMapEntry<K, V> nextInValueBucket;

        /* JADX INFO: Access modifiers changed from: package-private */
        public NonTerminalImmutableBiMapEntry(K key, V value, ImmutableMapEntry<K, V> nextInKeyBucket, ImmutableMapEntry<K, V> nextInValueBucket) {
            super(key, value, nextInKeyBucket);
            this.nextInValueBucket = nextInValueBucket;
        }

        @Override // com.google.common.collect.ImmutableMapEntry
        ImmutableMapEntry<K, V> getNextInValueBucket() {
            return this.nextInValueBucket;
        }
    }
}
