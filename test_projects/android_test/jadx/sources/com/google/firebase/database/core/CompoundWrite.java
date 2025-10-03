package com.google.firebase.database.core;

import com.google.firebase.database.core.utilities.ImmutableTree;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.NamedNode;
import com.google.firebase.database.snapshot.Node;
import com.google.firebase.database.snapshot.NodeUtilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public final class CompoundWrite implements Iterable<Map.Entry<Path, Node>> {
    private static final CompoundWrite EMPTY = new CompoundWrite(new ImmutableTree(null));
    private final ImmutableTree<Node> writeTree;

    private CompoundWrite(ImmutableTree<Node> writeTree) {
        this.writeTree = writeTree;
    }

    public static CompoundWrite emptyWrite() {
        return EMPTY;
    }

    public static CompoundWrite fromValue(Map<String, Object> merge) {
        ImmutableTree<Node> writeTree = ImmutableTree.emptyInstance();
        for (Map.Entry<String, Object> entry : merge.entrySet()) {
            ImmutableTree<Node> tree = new ImmutableTree<>(NodeUtilities.NodeFromJSON(entry.getValue()));
            writeTree = writeTree.setTree(new Path(entry.getKey()), tree);
        }
        return new CompoundWrite(writeTree);
    }

    public static CompoundWrite fromChildMerge(Map<ChildKey, Node> merge) {
        ImmutableTree<Node> writeTree = ImmutableTree.emptyInstance();
        for (Map.Entry<ChildKey, Node> entry : merge.entrySet()) {
            ImmutableTree<Node> tree = new ImmutableTree<>(entry.getValue());
            writeTree = writeTree.setTree(new Path(entry.getKey()), tree);
        }
        return new CompoundWrite(writeTree);
    }

    public static CompoundWrite fromPathMerge(Map<Path, Node> merge) {
        ImmutableTree<Node> writeTree = ImmutableTree.emptyInstance();
        for (Map.Entry<Path, Node> entry : merge.entrySet()) {
            ImmutableTree<Node> tree = new ImmutableTree<>(entry.getValue());
            writeTree = writeTree.setTree(entry.getKey(), tree);
        }
        return new CompoundWrite(writeTree);
    }

    public CompoundWrite addWrite(Path path, Node node) {
        if (path.isEmpty()) {
            return new CompoundWrite(new ImmutableTree(node));
        }
        Path rootMostPath = this.writeTree.findRootMostPathWithValue(path);
        if (rootMostPath != null) {
            Path relativePath = Path.getRelative(rootMostPath, path);
            Node value = this.writeTree.get(rootMostPath);
            ChildKey back = relativePath.getBack();
            if (back != null && back.isPriorityChildName() && value.getChild(relativePath.getParent()).isEmpty()) {
                return this;
            }
            return new CompoundWrite(this.writeTree.set(rootMostPath, value.updateChild(relativePath, node)));
        }
        ImmutableTree<Node> subtree = new ImmutableTree<>(node);
        ImmutableTree<Node> newWriteTree = this.writeTree.setTree(path, subtree);
        return new CompoundWrite(newWriteTree);
    }

    public CompoundWrite addWrite(ChildKey key, Node node) {
        return addWrite(new Path(key), node);
    }

    public CompoundWrite addWrites(final Path path, CompoundWrite updates) {
        return (CompoundWrite) updates.writeTree.fold(this, new ImmutableTree.TreeVisitor<Node, CompoundWrite>() { // from class: com.google.firebase.database.core.CompoundWrite.1
            @Override // com.google.firebase.database.core.utilities.ImmutableTree.TreeVisitor
            public CompoundWrite onNodeValue(Path relativePath, Node value, CompoundWrite accum) {
                return accum.addWrite(path.child(relativePath), value);
            }
        });
    }

    public CompoundWrite removeWrite(Path path) {
        if (path.isEmpty()) {
            return EMPTY;
        }
        ImmutableTree<Node> newWriteTree = this.writeTree.setTree(path, ImmutableTree.emptyInstance());
        return new CompoundWrite(newWriteTree);
    }

    public boolean hasCompleteWrite(Path path) {
        return getCompleteNode(path) != null;
    }

    public Node rootWrite() {
        return this.writeTree.getValue();
    }

    public Node getCompleteNode(Path path) {
        Path rootMost = this.writeTree.findRootMostPathWithValue(path);
        if (rootMost != null) {
            return this.writeTree.get(rootMost).getChild(Path.getRelative(rootMost, path));
        }
        return null;
    }

    public List<NamedNode> getCompleteChildren() {
        List<NamedNode> children = new ArrayList<>();
        if (this.writeTree.getValue() != null) {
            for (NamedNode entry : this.writeTree.getValue()) {
                children.add(new NamedNode(entry.getName(), entry.getNode()));
            }
        } else {
            Iterator<Map.Entry<ChildKey, ImmutableTree<Node>>> it = this.writeTree.getChildren().iterator();
            while (it.hasNext()) {
                Map.Entry<ChildKey, ImmutableTree<Node>> entry2 = it.next();
                ImmutableTree<Node> childTree = entry2.getValue();
                if (childTree.getValue() != null) {
                    children.add(new NamedNode(entry2.getKey(), childTree.getValue()));
                }
            }
        }
        return children;
    }

    public CompoundWrite childCompoundWrite(Path path) {
        if (path.isEmpty()) {
            return this;
        }
        Node shadowingNode = getCompleteNode(path);
        if (shadowingNode != null) {
            return new CompoundWrite(new ImmutableTree(shadowingNode));
        }
        return new CompoundWrite(this.writeTree.subtree(path));
    }

    public Map<ChildKey, CompoundWrite> childCompoundWrites() {
        Map<ChildKey, CompoundWrite> children = new HashMap<>();
        Iterator<Map.Entry<ChildKey, ImmutableTree<Node>>> it = this.writeTree.getChildren().iterator();
        while (it.hasNext()) {
            Map.Entry<ChildKey, ImmutableTree<Node>> entries = it.next();
            children.put(entries.getKey(), new CompoundWrite(entries.getValue()));
        }
        return children;
    }

    public boolean isEmpty() {
        return this.writeTree.isEmpty();
    }

    private Node applySubtreeWrite(Path relativePath, ImmutableTree<Node> writeTree, Node node) {
        if (writeTree.getValue() != null) {
            return node.updateChild(relativePath, writeTree.getValue());
        }
        Node priorityWrite = null;
        Iterator<Map.Entry<ChildKey, ImmutableTree<Node>>> it = writeTree.getChildren().iterator();
        while (it.hasNext()) {
            Map.Entry<ChildKey, ImmutableTree<Node>> childTreeEntry = it.next();
            ImmutableTree<Node> childTree = childTreeEntry.getValue();
            ChildKey childKey = childTreeEntry.getKey();
            if (childKey.isPriorityChildName()) {
                Utilities.hardAssert(childTree.getValue() != null, "Priority writes must always be leaf nodes");
                Node priorityWrite2 = childTree.getValue();
                priorityWrite = priorityWrite2;
            } else {
                node = applySubtreeWrite(relativePath.child(childKey), childTree, node);
            }
        }
        if (!node.getChild(relativePath).isEmpty() && priorityWrite != null) {
            return node.updateChild(relativePath.child(ChildKey.getPriorityKey()), priorityWrite);
        }
        return node;
    }

    public Node apply(Node node) {
        return applySubtreeWrite(Path.getEmptyPath(), this.writeTree, node);
    }

    public Map<String, Object> getValue(final boolean exportFormat) {
        final Map<String, Object> writes = new HashMap<>();
        this.writeTree.foreach(new ImmutableTree.TreeVisitor<Node, Void>() { // from class: com.google.firebase.database.core.CompoundWrite.2
            @Override // com.google.firebase.database.core.utilities.ImmutableTree.TreeVisitor
            public Void onNodeValue(Path relativePath, Node value, Void accum) {
                writes.put(relativePath.wireFormat(), value.getValue(exportFormat));
                return null;
            }
        });
        return writes;
    }

    @Override // java.lang.Iterable
    public Iterator<Map.Entry<Path, Node>> iterator() {
        return this.writeTree.iterator();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o != null && o.getClass() == getClass()) {
            return ((CompoundWrite) o).getValue(true).equals(getValue(true));
        }
        return false;
    }

    public int hashCode() {
        return getValue(true).hashCode();
    }

    public String toString() {
        return "CompoundWrite{" + getValue(true).toString() + "}";
    }
}
