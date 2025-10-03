package com.google.firebase.database.snapshot;

import com.google.android.gms.common.internal.Objects;
import com.google.firebase.database.collection.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class IndexedNode implements Iterable<NamedNode> {
    private static final ImmutableSortedSet<NamedNode> FALLBACK_INDEX = new ImmutableSortedSet<>(Collections.emptyList(), null);
    private final Index index;
    private ImmutableSortedSet<NamedNode> indexed;
    private final Node node;

    private IndexedNode(Node node, Index index) {
        this.index = index;
        this.node = node;
        this.indexed = null;
    }

    private IndexedNode(Node node, Index index, ImmutableSortedSet<NamedNode> indexed) {
        this.index = index;
        this.node = node;
        this.indexed = indexed;
    }

    private void ensureIndexed() {
        if (this.indexed == null) {
            if (this.index.equals(KeyIndex.getInstance())) {
                this.indexed = FALLBACK_INDEX;
                return;
            }
            List<NamedNode> children = new ArrayList<>();
            boolean sawIndexedValue = false;
            for (NamedNode entry : this.node) {
                sawIndexedValue = sawIndexedValue || this.index.isDefinedOn(entry.getNode());
                NamedNode namedNode = new NamedNode(entry.getName(), entry.getNode());
                children.add(namedNode);
            }
            if (sawIndexedValue) {
                this.indexed = new ImmutableSortedSet<>(children, this.index);
            } else {
                this.indexed = FALLBACK_INDEX;
            }
        }
    }

    public static IndexedNode from(Node node) {
        return new IndexedNode(node, PriorityIndex.getInstance());
    }

    public static IndexedNode from(Node node, Index index) {
        return new IndexedNode(node, index);
    }

    public boolean hasIndex(Index index) {
        return this.index == index;
    }

    public Node getNode() {
        return this.node;
    }

    @Override // java.lang.Iterable
    public Iterator<NamedNode> iterator() {
        ensureIndexed();
        if (Objects.equal(this.indexed, FALLBACK_INDEX)) {
            return this.node.iterator();
        }
        return this.indexed.iterator();
    }

    public Iterator<NamedNode> reverseIterator() {
        ensureIndexed();
        if (Objects.equal(this.indexed, FALLBACK_INDEX)) {
            return this.node.reverseIterator();
        }
        return this.indexed.reverseIterator();
    }

    public IndexedNode updateChild(ChildKey key, Node child) {
        Node newNode = this.node.updateImmediateChild(key, child);
        ImmutableSortedSet<NamedNode> immutableSortedSet = this.indexed;
        ImmutableSortedSet<NamedNode> immutableSortedSet2 = FALLBACK_INDEX;
        if (Objects.equal(immutableSortedSet, immutableSortedSet2) && !this.index.isDefinedOn(child)) {
            return new IndexedNode(newNode, this.index, immutableSortedSet2);
        }
        ImmutableSortedSet<NamedNode> immutableSortedSet3 = this.indexed;
        if (immutableSortedSet3 == null || Objects.equal(immutableSortedSet3, immutableSortedSet2)) {
            return new IndexedNode(newNode, this.index, null);
        }
        Node oldChild = this.node.getImmediateChild(key);
        ImmutableSortedSet<NamedNode> newIndexed = this.indexed.remove(new NamedNode(key, oldChild));
        if (!child.isEmpty()) {
            newIndexed = newIndexed.insert(new NamedNode(key, child));
        }
        return new IndexedNode(newNode, this.index, newIndexed);
    }

    public IndexedNode updatePriority(Node priority) {
        return new IndexedNode(this.node.updatePriority(priority), this.index, this.indexed);
    }

    public NamedNode getFirstChild() {
        if (!(this.node instanceof ChildrenNode)) {
            return null;
        }
        ensureIndexed();
        if (Objects.equal(this.indexed, FALLBACK_INDEX)) {
            ChildKey firstKey = ((ChildrenNode) this.node).getFirstChildKey();
            return new NamedNode(firstKey, this.node.getImmediateChild(firstKey));
        }
        return this.indexed.getMinEntry();
    }

    public NamedNode getLastChild() {
        if (!(this.node instanceof ChildrenNode)) {
            return null;
        }
        ensureIndexed();
        if (Objects.equal(this.indexed, FALLBACK_INDEX)) {
            ChildKey lastKey = ((ChildrenNode) this.node).getLastChildKey();
            return new NamedNode(lastKey, this.node.getImmediateChild(lastKey));
        }
        return this.indexed.getMaxEntry();
    }

    public ChildKey getPredecessorChildName(ChildKey childKey, Node childNode, Index index) {
        if (!this.index.equals(KeyIndex.getInstance()) && !this.index.equals(index)) {
            throw new IllegalArgumentException("Index not available in IndexedNode!");
        }
        ensureIndexed();
        if (Objects.equal(this.indexed, FALLBACK_INDEX)) {
            return this.node.getPredecessorChildKey(childKey);
        }
        NamedNode node = this.indexed.getPredecessorEntry(new NamedNode(childKey, childNode));
        if (node != null) {
            return node.getName();
        }
        return null;
    }
}
