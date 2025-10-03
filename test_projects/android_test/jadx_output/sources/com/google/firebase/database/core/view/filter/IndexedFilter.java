package com.google.firebase.database.core.view.filter;

import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.core.view.Change;
import com.google.firebase.database.core.view.filter.NodeFilter;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.Index;
import com.google.firebase.database.snapshot.IndexedNode;
import com.google.firebase.database.snapshot.NamedNode;
import com.google.firebase.database.snapshot.Node;

/* loaded from: classes.dex */
public class IndexedFilter implements NodeFilter {
    private final Index index;

    public IndexedFilter(Index index) {
        this.index = index;
    }

    @Override // com.google.firebase.database.core.view.filter.NodeFilter
    public IndexedNode updateChild(IndexedNode indexedNode, ChildKey key, Node newChild, Path affectedPath, NodeFilter.CompleteChildSource source, ChildChangeAccumulator optChangeAccumulator) {
        Utilities.hardAssert(indexedNode.hasIndex(this.index), "The index must match the filter");
        Node snap = indexedNode.getNode();
        Node oldChild = snap.getImmediateChild(key);
        if (oldChild.getChild(affectedPath).equals(newChild.getChild(affectedPath)) && oldChild.isEmpty() == newChild.isEmpty()) {
            return indexedNode;
        }
        if (optChangeAccumulator != null) {
            if (newChild.isEmpty()) {
                if (snap.hasChild(key)) {
                    optChangeAccumulator.trackChildChange(Change.childRemovedChange(key, oldChild));
                } else {
                    Utilities.hardAssert(snap.isLeafNode(), "A child remove without an old child only makes sense on a leaf node");
                }
            } else if (oldChild.isEmpty()) {
                optChangeAccumulator.trackChildChange(Change.childAddedChange(key, newChild));
            } else {
                optChangeAccumulator.trackChildChange(Change.childChangedChange(key, newChild, oldChild));
            }
        }
        if (snap.isLeafNode() && newChild.isEmpty()) {
            return indexedNode;
        }
        return indexedNode.updateChild(key, newChild);
    }

    @Override // com.google.firebase.database.core.view.filter.NodeFilter
    public IndexedNode updateFullNode(IndexedNode oldSnap, IndexedNode newSnap, ChildChangeAccumulator optChangeAccumulator) {
        Utilities.hardAssert(newSnap.hasIndex(this.index), "Can't use IndexedNode that doesn't have filter's index");
        if (optChangeAccumulator != null) {
            for (NamedNode child : oldSnap.getNode()) {
                if (!newSnap.getNode().hasChild(child.getName())) {
                    optChangeAccumulator.trackChildChange(Change.childRemovedChange(child.getName(), child.getNode()));
                }
            }
            if (!newSnap.getNode().isLeafNode()) {
                for (NamedNode child2 : newSnap.getNode()) {
                    if (oldSnap.getNode().hasChild(child2.getName())) {
                        Node oldChild = oldSnap.getNode().getImmediateChild(child2.getName());
                        if (!oldChild.equals(child2.getNode())) {
                            optChangeAccumulator.trackChildChange(Change.childChangedChange(child2.getName(), child2.getNode(), oldChild));
                        }
                    } else {
                        optChangeAccumulator.trackChildChange(Change.childAddedChange(child2.getName(), child2.getNode()));
                    }
                }
            }
        }
        return newSnap;
    }

    @Override // com.google.firebase.database.core.view.filter.NodeFilter
    public IndexedNode updatePriority(IndexedNode oldSnap, Node newPriority) {
        if (oldSnap.getNode().isEmpty()) {
            return oldSnap;
        }
        return oldSnap.updatePriority(newPriority);
    }

    @Override // com.google.firebase.database.core.view.filter.NodeFilter
    public NodeFilter getIndexedFilter() {
        return this;
    }

    @Override // com.google.firebase.database.core.view.filter.NodeFilter
    public Index getIndex() {
        return this.index;
    }

    @Override // com.google.firebase.database.core.view.filter.NodeFilter
    public boolean filtersNodes() {
        return false;
    }
}
