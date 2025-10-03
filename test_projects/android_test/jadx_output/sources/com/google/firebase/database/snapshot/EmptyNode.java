package com.google.firebase.database.snapshot;

import com.google.firebase.database.core.Path;
import com.google.firebase.database.snapshot.Node;
import java.util.Collections;
import java.util.Iterator;

/* loaded from: classes.dex */
public class EmptyNode extends ChildrenNode implements Node {
    private static final EmptyNode empty = new EmptyNode();

    private EmptyNode() {
    }

    public static EmptyNode Empty() {
        return empty;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public boolean isLeafNode() {
        return false;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public Node getPriority() {
        return this;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public Node getChild(Path path) {
        return this;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public Node getImmediateChild(ChildKey name) {
        return this;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public Node updateImmediateChild(ChildKey name, Node node) {
        if (node.isEmpty()) {
            return this;
        }
        if (name.isPriorityChildName()) {
            return this;
        }
        return new ChildrenNode().updateImmediateChild(name, node);
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public Node updateChild(Path path, Node node) {
        if (path.isEmpty()) {
            return node;
        }
        ChildKey name = path.getFront();
        Node newImmediateChild = getImmediateChild(name).updateChild(path.popFront(), node);
        return updateImmediateChild(name, newImmediateChild);
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public EmptyNode updatePriority(Node priority) {
        return this;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public boolean hasChild(ChildKey name) {
        return false;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public boolean isEmpty() {
        return true;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public int getChildCount() {
        return 0;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public Object getValue() {
        return null;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public Object getValue(boolean useExportFormat) {
        return null;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public ChildKey getPredecessorChildKey(ChildKey childKey) {
        return null;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public ChildKey getSuccessorChildKey(ChildKey childKey) {
        return null;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public String getHash() {
        return "";
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public String getHashRepresentation(Node.HashVersion version) {
        return "";
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, java.lang.Iterable
    public Iterator<NamedNode> iterator() {
        return Collections.emptyList().iterator();
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode, com.google.firebase.database.snapshot.Node
    public Iterator<NamedNode> reverseIterator() {
        return Collections.emptyList().iterator();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.google.firebase.database.snapshot.ChildrenNode, java.lang.Comparable
    public int compareTo(Node o) {
        return o.isEmpty() ? 0 : -1;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode
    public boolean equals(Object o) {
        if (o instanceof EmptyNode) {
            return true;
        }
        return (o instanceof Node) && ((Node) o).isEmpty() && getPriority().equals(((Node) o).getPriority());
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode
    public int hashCode() {
        return 0;
    }

    @Override // com.google.firebase.database.snapshot.ChildrenNode
    public String toString() {
        return "<Empty Node>";
    }
}
