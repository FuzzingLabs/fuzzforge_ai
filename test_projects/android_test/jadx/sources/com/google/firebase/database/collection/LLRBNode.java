package com.google.firebase.database.collection;

import java.util.Comparator;

/* loaded from: classes.dex */
public interface LLRBNode<K, V> {

    public enum Color {
        RED,
        BLACK
    }

    public interface ShortCircuitingNodeVisitor<K, V> {
        boolean shouldContinue(K k, V v);
    }

    LLRBNode<K, V> copy(K k, V v, Color color, LLRBNode<K, V> lLRBNode, LLRBNode<K, V> lLRBNode2);

    K getKey();

    LLRBNode<K, V> getLeft();

    LLRBNode<K, V> getMax();

    LLRBNode<K, V> getMin();

    LLRBNode<K, V> getRight();

    V getValue();

    void inOrderTraversal(NodeVisitor<K, V> nodeVisitor);

    LLRBNode<K, V> insert(K k, V v, Comparator<K> comparator);

    boolean isEmpty();

    boolean isRed();

    LLRBNode<K, V> remove(K k, Comparator<K> comparator);

    boolean shortCircuitingInOrderTraversal(ShortCircuitingNodeVisitor<K, V> shortCircuitingNodeVisitor);

    boolean shortCircuitingReverseOrderTraversal(ShortCircuitingNodeVisitor<K, V> shortCircuitingNodeVisitor);

    int size();

    public static abstract class NodeVisitor<K, V> implements ShortCircuitingNodeVisitor<K, V> {
        public abstract void visitEntry(K k, V v);

        @Override // com.google.firebase.database.collection.LLRBNode.ShortCircuitingNodeVisitor
        public boolean shouldContinue(K key, V value) {
            visitEntry(key, value);
            return true;
        }
    }
}
