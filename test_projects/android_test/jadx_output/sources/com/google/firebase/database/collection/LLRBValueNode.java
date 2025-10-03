package com.google.firebase.database.collection;

import com.google.firebase.database.collection.LLRBNode;
import java.util.Comparator;

/* loaded from: classes.dex */
public abstract class LLRBValueNode<K, V> implements LLRBNode<K, V> {
    private final K key;
    private LLRBNode<K, V> left;
    private final LLRBNode<K, V> right;
    private final V value;

    protected abstract LLRBValueNode<K, V> copy(K k, V v, LLRBNode<K, V> lLRBNode, LLRBNode<K, V> lLRBNode2);

    protected abstract LLRBNode.Color getColor();

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.google.firebase.database.collection.LLRBNode
    public /* bridge */ /* synthetic */ LLRBNode copy(Object obj, Object obj2, LLRBNode.Color color, LLRBNode lLRBNode, LLRBNode lLRBNode2) {
        return copy((LLRBValueNode<K, V>) obj, obj2, color, (LLRBNode<LLRBValueNode<K, V>, Object>) lLRBNode, (LLRBNode<LLRBValueNode<K, V>, Object>) lLRBNode2);
    }

    private static LLRBNode.Color oppositeColor(LLRBNode node) {
        return node.isRed() ? LLRBNode.Color.BLACK : LLRBNode.Color.RED;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public LLRBValueNode(K key, V value, LLRBNode<K, V> left, LLRBNode<K, V> right) {
        this.key = key;
        this.value = value;
        this.left = left == null ? LLRBEmptyNode.getInstance() : left;
        this.right = right == null ? LLRBEmptyNode.getInstance() : right;
    }

    @Override // com.google.firebase.database.collection.LLRBNode
    public LLRBNode<K, V> getLeft() {
        return this.left;
    }

    @Override // com.google.firebase.database.collection.LLRBNode
    public LLRBNode<K, V> getRight() {
        return this.right;
    }

    @Override // com.google.firebase.database.collection.LLRBNode
    public K getKey() {
        return this.key;
    }

    @Override // com.google.firebase.database.collection.LLRBNode
    public V getValue() {
        return this.value;
    }

    @Override // com.google.firebase.database.collection.LLRBNode
    public LLRBValueNode<K, V> copy(K key, V value, LLRBNode.Color color, LLRBNode<K, V> left, LLRBNode<K, V> right) {
        K newKey = key == null ? this.key : key;
        V newValue = value == null ? this.value : value;
        LLRBNode<K, V> newLeft = left == null ? this.left : left;
        LLRBNode<K, V> newRight = right == null ? this.right : right;
        if (color == LLRBNode.Color.RED) {
            return new LLRBRedValueNode(newKey, newValue, newLeft, newRight);
        }
        return new LLRBBlackValueNode(newKey, newValue, newLeft, newRight);
    }

    @Override // com.google.firebase.database.collection.LLRBNode
    public LLRBNode<K, V> insert(K key, V value, Comparator<K> comparator) {
        LLRBValueNode<K, V> n;
        int cmp = comparator.compare(key, this.key);
        if (cmp < 0) {
            LLRBNode<K, V> newLeft = this.left.insert(key, value, comparator);
            n = copy(null, null, newLeft, null);
        } else if (cmp == 0) {
            n = copy(key, value, null, null);
        } else {
            LLRBNode<K, V> newRight = this.right.insert(key, value, comparator);
            n = copy(null, null, null, newRight);
        }
        LLRBNode<K, V> newRight2 = n.fixUp();
        return newRight2;
    }

    @Override // com.google.firebase.database.collection.LLRBNode
    public LLRBNode<K, V> remove(K key, Comparator<K> comparator) {
        LLRBValueNode<K, V> n;
        LLRBValueNode<K, V> n2 = this;
        if (comparator.compare(key, n2.key) < 0) {
            if (!n2.left.isEmpty() && !n2.left.isRed() && !((LLRBValueNode) n2.left).left.isRed()) {
                n2 = n2.moveRedLeft();
            }
            n = n2.copy(null, null, n2.left.remove(key, comparator), null);
        } else {
            if (n2.left.isRed()) {
                n2 = n2.rotateRight();
            }
            if (!n2.right.isEmpty() && !n2.right.isRed() && !((LLRBValueNode) n2.right).left.isRed()) {
                n2 = n2.moveRedRight();
            }
            if (comparator.compare(key, n2.key) == 0) {
                if (n2.right.isEmpty()) {
                    return LLRBEmptyNode.getInstance();
                }
                LLRBNode<K, V> smallest = n2.right.getMin();
                n2 = n2.copy(smallest.getKey(), smallest.getValue(), null, ((LLRBValueNode) n2.right).removeMin());
            }
            n = n2.copy(null, null, null, n2.right.remove(key, comparator));
        }
        return n.fixUp();
    }

    @Override // com.google.firebase.database.collection.LLRBNode
    public boolean isEmpty() {
        return false;
    }

    @Override // com.google.firebase.database.collection.LLRBNode
    public LLRBNode<K, V> getMin() {
        if (this.left.isEmpty()) {
            return this;
        }
        return this.left.getMin();
    }

    @Override // com.google.firebase.database.collection.LLRBNode
    public LLRBNode<K, V> getMax() {
        if (this.right.isEmpty()) {
            return this;
        }
        return this.right.getMax();
    }

    @Override // com.google.firebase.database.collection.LLRBNode
    public void inOrderTraversal(LLRBNode.NodeVisitor<K, V> visitor) {
        this.left.inOrderTraversal(visitor);
        visitor.visitEntry(this.key, this.value);
        this.right.inOrderTraversal(visitor);
    }

    @Override // com.google.firebase.database.collection.LLRBNode
    public boolean shortCircuitingInOrderTraversal(LLRBNode.ShortCircuitingNodeVisitor<K, V> visitor) {
        if (this.left.shortCircuitingInOrderTraversal(visitor) && visitor.shouldContinue(this.key, this.value)) {
            return this.right.shortCircuitingInOrderTraversal(visitor);
        }
        return false;
    }

    @Override // com.google.firebase.database.collection.LLRBNode
    public boolean shortCircuitingReverseOrderTraversal(LLRBNode.ShortCircuitingNodeVisitor<K, V> visitor) {
        if (this.right.shortCircuitingReverseOrderTraversal(visitor) && visitor.shouldContinue(this.key, this.value)) {
            return this.left.shortCircuitingReverseOrderTraversal(visitor);
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setLeft(LLRBNode<K, V> left) {
        this.left = left;
    }

    private LLRBNode<K, V> removeMin() {
        if (this.left.isEmpty()) {
            return LLRBEmptyNode.getInstance();
        }
        LLRBValueNode<K, V> n = this;
        if (!n.getLeft().isRed() && !n.getLeft().getLeft().isRed()) {
            n = n.moveRedLeft();
        }
        return n.copy(null, null, ((LLRBValueNode) n.left).removeMin(), null).fixUp();
    }

    private LLRBValueNode<K, V> moveRedLeft() {
        LLRBValueNode<K, V> n = colorFlip();
        if (n.getRight().getLeft().isRed()) {
            return n.copy(null, null, null, ((LLRBValueNode) n.getRight()).rotateRight()).rotateLeft().colorFlip();
        }
        return n;
    }

    private LLRBValueNode<K, V> moveRedRight() {
        LLRBValueNode<K, V> n = colorFlip();
        if (n.getLeft().getLeft().isRed()) {
            return n.rotateRight().colorFlip();
        }
        return n;
    }

    private LLRBValueNode<K, V> fixUp() {
        LLRBValueNode<K, V> n = this;
        if (n.right.isRed() && !n.left.isRed()) {
            n = n.rotateLeft();
        }
        if (n.left.isRed() && ((LLRBValueNode) n.left).left.isRed()) {
            n = n.rotateRight();
        }
        if (n.left.isRed() && n.right.isRed()) {
            return n.colorFlip();
        }
        return n;
    }

    private LLRBValueNode<K, V> rotateLeft() {
        LLRBValueNode<K, V> newLeft = copy((LLRBValueNode<K, V>) null, (K) null, LLRBNode.Color.RED, (LLRBNode<LLRBValueNode<K, V>, K>) null, (LLRBNode<LLRBValueNode<K, V>, K>) ((LLRBValueNode) this.right).left);
        return (LLRBValueNode) this.right.copy(null, null, getColor(), newLeft, null);
    }

    private LLRBValueNode<K, V> rotateRight() {
        LLRBValueNode<K, V> newRight = copy((LLRBValueNode<K, V>) null, (K) null, LLRBNode.Color.RED, (LLRBNode<LLRBValueNode<K, V>, K>) ((LLRBValueNode) this.left).right, (LLRBNode<LLRBValueNode<K, V>, K>) null);
        return (LLRBValueNode) this.left.copy(null, null, getColor(), null, newRight);
    }

    private LLRBValueNode<K, V> colorFlip() {
        LLRBNode<K, V> lLRBNode = this.left;
        LLRBNode<K, V> newLeft = lLRBNode.copy(null, null, oppositeColor(lLRBNode), null, null);
        LLRBNode<K, V> lLRBNode2 = this.right;
        LLRBNode<K, V> newRight = lLRBNode2.copy(null, null, oppositeColor(lLRBNode2), null, null);
        return copy((LLRBValueNode<K, V>) null, (K) null, oppositeColor(this), (LLRBNode<LLRBValueNode<K, V>, K>) newLeft, (LLRBNode<LLRBValueNode<K, V>, K>) newRight);
    }
}
