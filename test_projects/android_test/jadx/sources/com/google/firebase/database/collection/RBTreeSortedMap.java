package com.google.firebase.database.collection;

import com.google.firebase.database.collection.ImmutableSortedMap;
import com.google.firebase.database.collection.LLRBNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class RBTreeSortedMap<K, V> extends ImmutableSortedMap<K, V> {
    private Comparator<K> comparator;
    private LLRBNode<K, V> root;

    RBTreeSortedMap(Comparator<K> comparator) {
        this.root = LLRBEmptyNode.getInstance();
        this.comparator = comparator;
    }

    private RBTreeSortedMap(LLRBNode<K, V> root, Comparator<K> comparator) {
        this.root = root;
        this.comparator = comparator;
    }

    LLRBNode<K, V> getRoot() {
        return this.root;
    }

    private LLRBNode<K, V> getNode(K key) {
        LLRBNode<K, V> node = this.root;
        while (!node.isEmpty()) {
            int cmp = this.comparator.compare(key, node.getKey());
            if (cmp < 0) {
                node = node.getLeft();
            } else {
                if (cmp == 0) {
                    return node;
                }
                node = node.getRight();
            }
        }
        return null;
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public boolean containsKey(K key) {
        return getNode(key) != null;
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public V get(K key) {
        LLRBNode<K, V> node = getNode(key);
        if (node != null) {
            return node.getValue();
        }
        return null;
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public ImmutableSortedMap<K, V> remove(K key) {
        if (!containsKey(key)) {
            return this;
        }
        LLRBNode<K, V> newRoot = this.root.remove(key, this.comparator).copy(null, null, LLRBNode.Color.BLACK, null, null);
        return new RBTreeSortedMap(newRoot, this.comparator);
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public ImmutableSortedMap<K, V> insert(K key, V value) {
        LLRBNode<K, V> newRoot = this.root.insert(key, value, this.comparator).copy(null, null, LLRBNode.Color.BLACK, null, null);
        return new RBTreeSortedMap(newRoot, this.comparator);
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public K getMinKey() {
        return this.root.getMin().getKey();
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public K getMaxKey() {
        return this.root.getMax().getKey();
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public int size() {
        return this.root.size();
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public boolean isEmpty() {
        return this.root.isEmpty();
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public void inOrderTraversal(LLRBNode.NodeVisitor<K, V> visitor) {
        this.root.inOrderTraversal(visitor);
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap, java.lang.Iterable
    public Iterator<Map.Entry<K, V>> iterator() {
        return new ImmutableSortedMapIterator(this.root, null, this.comparator, false);
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public Iterator<Map.Entry<K, V>> iteratorFrom(K key) {
        return new ImmutableSortedMapIterator(this.root, key, this.comparator, false);
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public Iterator<Map.Entry<K, V>> reverseIteratorFrom(K key) {
        return new ImmutableSortedMapIterator(this.root, key, this.comparator, true);
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public Iterator<Map.Entry<K, V>> reverseIterator() {
        return new ImmutableSortedMapIterator(this.root, null, this.comparator, true);
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public K getPredecessorKey(K key) {
        LLRBNode<K, V> node = this.root;
        LLRBNode<K, V> rightParent = null;
        while (!node.isEmpty()) {
            int cmp = this.comparator.compare(key, node.getKey());
            if (cmp == 0) {
                if (!node.getLeft().isEmpty()) {
                    LLRBNode<K, V> node2 = node.getLeft();
                    while (!node2.getRight().isEmpty()) {
                        node2 = node2.getRight();
                    }
                    return node2.getKey();
                }
                if (rightParent != null) {
                    return rightParent.getKey();
                }
                return null;
            }
            if (cmp < 0) {
                node = node.getLeft();
            } else {
                rightParent = node;
                node = node.getRight();
            }
        }
        throw new IllegalArgumentException("Couldn't find predecessor key of non-present key: " + key);
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public K getSuccessorKey(K key) {
        LLRBNode<K, V> node = this.root;
        LLRBNode<K, V> leftParent = null;
        while (!node.isEmpty()) {
            int cmp = this.comparator.compare(node.getKey(), key);
            if (cmp == 0) {
                if (!node.getRight().isEmpty()) {
                    LLRBNode<K, V> node2 = node.getRight();
                    while (!node2.getLeft().isEmpty()) {
                        node2 = node2.getLeft();
                    }
                    return node2.getKey();
                }
                if (leftParent != null) {
                    return leftParent.getKey();
                }
                return null;
            }
            if (cmp < 0) {
                node = node.getRight();
            } else {
                leftParent = node;
                node = node.getLeft();
            }
        }
        throw new IllegalArgumentException("Couldn't find successor key of non-present key: " + key);
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public int indexOf(K key) {
        int prunedNodes = 0;
        LLRBNode<K, V> node = this.root;
        while (!node.isEmpty()) {
            int cmp = this.comparator.compare(key, node.getKey());
            if (cmp == 0) {
                return node.getLeft().size() + prunedNodes;
            }
            if (cmp < 0) {
                node = node.getLeft();
            } else {
                prunedNodes += node.getLeft().size() + 1;
                node = node.getRight();
            }
        }
        return -1;
    }

    @Override // com.google.firebase.database.collection.ImmutableSortedMap
    public Comparator<K> getComparator() {
        return this.comparator;
    }

    public static <A, B, C> RBTreeSortedMap<A, C> buildFrom(List<A> keys, Map<B, C> values, ImmutableSortedMap.Builder.KeyTranslator<A, B> translator, Comparator<A> comparator) {
        return Builder.buildFrom(keys, values, translator, comparator);
    }

    public static <A, B> RBTreeSortedMap<A, B> fromMap(Map<A, B> values, Comparator<A> comparator) {
        return Builder.buildFrom(new ArrayList(values.keySet()), values, ImmutableSortedMap.Builder.identityTranslator(), comparator);
    }

    private static class Builder<A, B, C> {
        private final ImmutableSortedMap.Builder.KeyTranslator<A, B> keyTranslator;
        private final List<A> keys;
        private LLRBValueNode<A, C> leaf;
        private LLRBValueNode<A, C> root;
        private final Map<B, C> values;

        static class BooleanChunk {
            public int chunkSize;
            public boolean isOne;

            BooleanChunk() {
            }
        }

        static class Base1_2 implements Iterable<BooleanChunk> {
            private final int length;
            private long value;

            public Base1_2(int size) {
                int toCalc = size + 1;
                int floor = (int) Math.floor(Math.log(toCalc) / Math.log(2.0d));
                this.length = floor;
                long mask = ((long) Math.pow(2.0d, floor)) - 1;
                this.value = toCalc & mask;
            }

            @Override // java.lang.Iterable
            public Iterator<BooleanChunk> iterator() {
                return new Iterator<BooleanChunk>() { // from class: com.google.firebase.database.collection.RBTreeSortedMap.Builder.Base1_2.1
                    private int current;

                    {
                        this.current = Base1_2.this.length - 1;
                    }

                    @Override // java.util.Iterator
                    public boolean hasNext() {
                        return this.current >= 0;
                    }

                    /* JADX WARN: Can't rename method to resolve collision */
                    @Override // java.util.Iterator
                    public BooleanChunk next() {
                        long result = Base1_2.this.value & (1 << this.current);
                        BooleanChunk next = new BooleanChunk();
                        next.isOne = result == 0;
                        next.chunkSize = (int) Math.pow(2.0d, this.current);
                        this.current--;
                        return next;
                    }

                    @Override // java.util.Iterator
                    public void remove() {
                    }
                };
            }
        }

        private Builder(List<A> keys, Map<B, C> values, ImmutableSortedMap.Builder.KeyTranslator<A, B> translator) {
            this.keys = keys;
            this.values = values;
            this.keyTranslator = translator;
        }

        private C getValue(A key) {
            return this.values.get(this.keyTranslator.translate(key));
        }

        private LLRBNode<A, C> buildBalancedTree(int start, int size) {
            if (size == 0) {
                return LLRBEmptyNode.getInstance();
            }
            if (size == 1) {
                A key = this.keys.get(start);
                return new LLRBBlackValueNode(key, getValue(key), null, null);
            }
            int half = size / 2;
            int middle = start + half;
            LLRBNode<A, C> left = buildBalancedTree(start, half);
            LLRBNode<A, C> right = buildBalancedTree(middle + 1, half);
            A key2 = this.keys.get(middle);
            return new LLRBBlackValueNode(key2, getValue(key2), left, right);
        }

        private void buildPennant(LLRBNode.Color color, int chunkSize, int start) {
            LLRBValueNode<A, C> node;
            LLRBNode<A, C> treeRoot = buildBalancedTree(start + 1, chunkSize - 1);
            A key = this.keys.get(start);
            if (color == LLRBNode.Color.RED) {
                node = new LLRBRedValueNode<>(key, getValue(key), null, treeRoot);
            } else {
                node = new LLRBBlackValueNode<>(key, getValue(key), null, treeRoot);
            }
            if (this.root == null) {
                this.root = node;
                this.leaf = node;
            } else {
                this.leaf.setLeft(node);
                this.leaf = node;
            }
        }

        public static <A, B, C> RBTreeSortedMap<A, C> buildFrom(List<A> keys, Map<B, C> values, ImmutableSortedMap.Builder.KeyTranslator<A, B> translator, Comparator<A> comparator) {
            Builder<A, B, C> builder = new Builder<>(keys, values, translator);
            Collections.sort(keys, comparator);
            Iterator<BooleanChunk> iter = new Base1_2(keys.size()).iterator();
            int index = keys.size();
            while (iter.hasNext()) {
                BooleanChunk next = iter.next();
                index -= next.chunkSize;
                if (next.isOne) {
                    builder.buildPennant(LLRBNode.Color.BLACK, next.chunkSize, index);
                } else {
                    builder.buildPennant(LLRBNode.Color.BLACK, next.chunkSize, index);
                    index -= next.chunkSize;
                    builder.buildPennant(LLRBNode.Color.RED, next.chunkSize, index);
                }
            }
            LLRBNode lLRBNode = ((Builder) builder).root;
            if (lLRBNode == null) {
                lLRBNode = LLRBEmptyNode.getInstance();
            }
            return new RBTreeSortedMap<>(lLRBNode, comparator);
        }
    }
}
