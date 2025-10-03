package com.google.firebase.database.core.persistence;

import com.google.firebase.database.collection.ImmutableSortedMap;
import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.utilities.ImmutableTree;
import com.google.firebase.database.core.utilities.Predicate;
import com.google.firebase.database.snapshot.ChildKey;
import java.util.Set;

/* loaded from: classes.dex */
public class PruneForest {
    private final ImmutableTree<Boolean> pruneForest;
    private static final Predicate<Boolean> KEEP_PREDICATE = new Predicate<Boolean>() { // from class: com.google.firebase.database.core.persistence.PruneForest.1
        @Override // com.google.firebase.database.core.utilities.Predicate
        public boolean evaluate(Boolean prune) {
            return !prune.booleanValue();
        }
    };
    private static final Predicate<Boolean> PRUNE_PREDICATE = new Predicate<Boolean>() { // from class: com.google.firebase.database.core.persistence.PruneForest.2
        @Override // com.google.firebase.database.core.utilities.Predicate
        public boolean evaluate(Boolean prune) {
            return prune.booleanValue();
        }
    };
    private static final ImmutableTree<Boolean> PRUNE_TREE = new ImmutableTree<>(true);
    private static final ImmutableTree<Boolean> KEEP_TREE = new ImmutableTree<>(false);

    public PruneForest() {
        this.pruneForest = ImmutableTree.emptyInstance();
    }

    private PruneForest(ImmutableTree<Boolean> pruneForest) {
        this.pruneForest = pruneForest;
    }

    public boolean prunesAnything() {
        return this.pruneForest.containsMatchingValue(PRUNE_PREDICATE);
    }

    public boolean shouldPruneUnkeptDescendants(Path path) {
        Boolean shouldPrune = this.pruneForest.leafMostValue(path);
        return shouldPrune != null && shouldPrune.booleanValue();
    }

    public boolean shouldKeep(Path path) {
        Boolean shouldPrune = this.pruneForest.leafMostValue(path);
        return (shouldPrune == null || shouldPrune.booleanValue()) ? false : true;
    }

    public boolean affectsPath(Path path) {
        return (this.pruneForest.rootMostValue(path) == null && this.pruneForest.subtree(path).isEmpty()) ? false : true;
    }

    public PruneForest child(ChildKey key) {
        ImmutableTree<Boolean> childPruneTree = this.pruneForest.getChild(key);
        if (childPruneTree == null) {
            childPruneTree = new ImmutableTree<>(this.pruneForest.getValue());
        } else if (childPruneTree.getValue() == null && this.pruneForest.getValue() != null) {
            childPruneTree = childPruneTree.set(Path.getEmptyPath(), this.pruneForest.getValue());
        }
        return new PruneForest(childPruneTree);
    }

    public PruneForest child(Path path) {
        if (path.isEmpty()) {
            return this;
        }
        return child(path.getFront()).child(path.popFront());
    }

    public <T> T foldKeptNodes(T t, final ImmutableTree.TreeVisitor<Void, T> treeVisitor) {
        return (T) this.pruneForest.fold(t, new ImmutableTree.TreeVisitor<Boolean, T>() { // from class: com.google.firebase.database.core.persistence.PruneForest.3
            /* JADX WARN: Multi-variable type inference failed */
            @Override // com.google.firebase.database.core.utilities.ImmutableTree.TreeVisitor
            public /* bridge */ /* synthetic */ Object onNodeValue(Path path, Boolean bool, Object obj) {
                return onNodeValue2(path, bool, (Boolean) obj);
            }

            /* renamed from: onNodeValue, reason: avoid collision after fix types in other method */
            public T onNodeValue2(Path path, Boolean bool, T t2) {
                if (!bool.booleanValue()) {
                    return (T) treeVisitor.onNodeValue(path, null, t2);
                }
                return t2;
            }
        });
    }

    public PruneForest prune(Path path) {
        if (this.pruneForest.rootMostValueMatching(path, KEEP_PREDICATE) != null) {
            throw new IllegalArgumentException("Can't prune path that was kept previously!");
        }
        if (this.pruneForest.rootMostValueMatching(path, PRUNE_PREDICATE) != null) {
            return this;
        }
        ImmutableTree<Boolean> newPruneTree = this.pruneForest.setTree(path, PRUNE_TREE);
        return new PruneForest(newPruneTree);
    }

    public PruneForest keep(Path path) {
        if (this.pruneForest.rootMostValueMatching(path, KEEP_PREDICATE) != null) {
            return this;
        }
        ImmutableTree<Boolean> newPruneTree = this.pruneForest.setTree(path, KEEP_TREE);
        return new PruneForest(newPruneTree);
    }

    public PruneForest keepAll(Path path, Set<ChildKey> children) {
        if (this.pruneForest.rootMostValueMatching(path, KEEP_PREDICATE) != null) {
            return this;
        }
        return doAll(path, children, KEEP_TREE);
    }

    public PruneForest pruneAll(Path path, Set<ChildKey> children) {
        if (this.pruneForest.rootMostValueMatching(path, KEEP_PREDICATE) != null) {
            throw new IllegalArgumentException("Can't prune path that was kept previously!");
        }
        if (this.pruneForest.rootMostValueMatching(path, PRUNE_PREDICATE) != null) {
            return this;
        }
        return doAll(path, children, PRUNE_TREE);
    }

    private PruneForest doAll(Path path, Set<ChildKey> children, ImmutableTree<Boolean> keepOrPruneTree) {
        ImmutableTree<Boolean> subtree = this.pruneForest.subtree(path);
        ImmutableSortedMap<ChildKey, ImmutableTree<Boolean>> childrenMap = subtree.getChildren();
        for (ChildKey key : children) {
            childrenMap = childrenMap.insert(key, keepOrPruneTree);
        }
        return new PruneForest(this.pruneForest.setTree(path, new ImmutableTree<>(subtree.getValue(), childrenMap)));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PruneForest)) {
            return false;
        }
        PruneForest that = (PruneForest) o;
        return this.pruneForest.equals(that.pruneForest);
    }

    public int hashCode() {
        return this.pruneForest.hashCode();
    }

    public String toString() {
        return "{PruneForest:" + this.pruneForest.toString() + "}";
    }
}
