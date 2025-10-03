package com.google.firebase.database.core.view.filter;

import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.view.QueryParams;
import com.google.firebase.database.core.view.filter.NodeFilter;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.EmptyNode;
import com.google.firebase.database.snapshot.Index;
import com.google.firebase.database.snapshot.IndexedNode;
import com.google.firebase.database.snapshot.NamedNode;
import com.google.firebase.database.snapshot.Node;
import com.google.firebase.database.snapshot.PriorityUtilities;
import java.util.Iterator;

/* loaded from: classes.dex */
public class RangedFilter implements NodeFilter {
    private final NamedNode endPost;
    private final Index index;
    private final IndexedFilter indexedFilter;
    private final NamedNode startPost;

    public RangedFilter(QueryParams params) {
        this.indexedFilter = new IndexedFilter(params.getIndex());
        this.index = params.getIndex();
        this.startPost = getStartPost(params);
        this.endPost = getEndPost(params);
    }

    public NamedNode getStartPost() {
        return this.startPost;
    }

    public NamedNode getEndPost() {
        return this.endPost;
    }

    private static NamedNode getStartPost(QueryParams params) {
        if (params.hasStart()) {
            ChildKey startName = params.getIndexStartName();
            return params.getIndex().makePost(startName, params.getIndexStartValue());
        }
        return params.getIndex().minPost();
    }

    private static NamedNode getEndPost(QueryParams params) {
        if (params.hasEnd()) {
            ChildKey endName = params.getIndexEndName();
            return params.getIndex().makePost(endName, params.getIndexEndValue());
        }
        return params.getIndex().maxPost();
    }

    public boolean matches(NamedNode node) {
        if (this.index.compare(getStartPost(), node) <= 0 && this.index.compare(node, getEndPost()) <= 0) {
            return true;
        }
        return false;
    }

    @Override // com.google.firebase.database.core.view.filter.NodeFilter
    public IndexedNode updateChild(IndexedNode snap, ChildKey key, Node newChild, Path affectedPath, NodeFilter.CompleteChildSource source, ChildChangeAccumulator optChangeAccumulator) {
        if (!matches(new NamedNode(key, newChild))) {
            newChild = EmptyNode.Empty();
        }
        return this.indexedFilter.updateChild(snap, key, newChild, affectedPath, source, optChangeAccumulator);
    }

    @Override // com.google.firebase.database.core.view.filter.NodeFilter
    public IndexedNode updateFullNode(IndexedNode oldSnap, IndexedNode newSnap, ChildChangeAccumulator optChangeAccumulator) {
        IndexedNode filtered;
        if (newSnap.getNode().isLeafNode()) {
            filtered = IndexedNode.from(EmptyNode.Empty(), this.index);
        } else {
            filtered = newSnap.updatePriority(PriorityUtilities.NullPriority());
            Iterator<NamedNode> it = newSnap.iterator();
            while (it.hasNext()) {
                NamedNode child = it.next();
                if (!matches(child)) {
                    filtered = filtered.updateChild(child.getName(), EmptyNode.Empty());
                }
            }
        }
        return this.indexedFilter.updateFullNode(oldSnap, filtered, optChangeAccumulator);
    }

    @Override // com.google.firebase.database.core.view.filter.NodeFilter
    public IndexedNode updatePriority(IndexedNode oldSnap, Node newPriority) {
        return oldSnap;
    }

    @Override // com.google.firebase.database.core.view.filter.NodeFilter
    public NodeFilter getIndexedFilter() {
        return this.indexedFilter;
    }

    @Override // com.google.firebase.database.core.view.filter.NodeFilter
    public Index getIndex() {
        return this.index;
    }

    @Override // com.google.firebase.database.core.view.filter.NodeFilter
    public boolean filtersNodes() {
        return true;
    }
}
