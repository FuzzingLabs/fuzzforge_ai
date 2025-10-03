package com.google.firebase.database.core.view;

import com.google.firebase.database.core.Path;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.IndexedNode;
import com.google.firebase.database.snapshot.Node;

/* loaded from: classes.dex */
public class CacheNode {
    private final boolean filtered;
    private final boolean fullyInitialized;
    private final IndexedNode indexedNode;

    public CacheNode(IndexedNode node, boolean fullyInitialized, boolean filtered) {
        this.indexedNode = node;
        this.fullyInitialized = fullyInitialized;
        this.filtered = filtered;
    }

    public boolean isFullyInitialized() {
        return this.fullyInitialized;
    }

    public boolean isFiltered() {
        return this.filtered;
    }

    public boolean isCompleteForPath(Path path) {
        if (path.isEmpty()) {
            return isFullyInitialized() && !this.filtered;
        }
        ChildKey childKey = path.getFront();
        return isCompleteForChild(childKey);
    }

    public boolean isCompleteForChild(ChildKey key) {
        return (isFullyInitialized() && !this.filtered) || this.indexedNode.getNode().hasChild(key);
    }

    public Node getNode() {
        return this.indexedNode.getNode();
    }

    public IndexedNode getIndexedNode() {
        return this.indexedNode;
    }
}
