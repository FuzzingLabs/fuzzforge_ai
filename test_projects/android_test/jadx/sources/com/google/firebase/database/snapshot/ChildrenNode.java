package com.google.firebase.database.snapshot;

import com.google.firebase.database.collection.ImmutableSortedMap;
import com.google.firebase.database.collection.LLRBNode;
import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.snapshot.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class ChildrenNode implements Node {
    public static Comparator<ChildKey> NAME_ONLY_COMPARATOR = new Comparator<ChildKey>() { // from class: com.google.firebase.database.snapshot.ChildrenNode.1
        @Override // java.util.Comparator
        public int compare(ChildKey o1, ChildKey o2) {
            return o1.compareTo(o2);
        }
    };
    private final ImmutableSortedMap<ChildKey, Node> children;
    private String lazyHash;
    private final Node priority;

    private static class NamedNodeIterator implements Iterator<NamedNode> {
        private final Iterator<Map.Entry<ChildKey, Node>> iterator;

        public NamedNodeIterator(Iterator<Map.Entry<ChildKey, Node>> iterator) {
            this.iterator = iterator;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // java.util.Iterator
        public NamedNode next() {
            Map.Entry<ChildKey, Node> entry = this.iterator.next();
            return new NamedNode(entry.getKey(), entry.getValue());
        }

        @Override // java.util.Iterator
        public void remove() {
            this.iterator.remove();
        }
    }

    public static abstract class ChildVisitor extends LLRBNode.NodeVisitor<ChildKey, Node> {
        public abstract void visitChild(ChildKey childKey, Node node);

        @Override // com.google.firebase.database.collection.LLRBNode.NodeVisitor
        public void visitEntry(ChildKey key, Node value) {
            visitChild(key, value);
        }
    }

    protected ChildrenNode() {
        this.lazyHash = null;
        this.children = ImmutableSortedMap.Builder.emptyMap(NAME_ONLY_COMPARATOR);
        this.priority = PriorityUtilities.NullPriority();
    }

    protected ChildrenNode(ImmutableSortedMap<ChildKey, Node> children, Node priority) {
        this.lazyHash = null;
        if (children.isEmpty() && !priority.isEmpty()) {
            throw new IllegalArgumentException("Can't create empty ChildrenNode with priority!");
        }
        this.priority = priority;
        this.children = children;
    }

    @Override // com.google.firebase.database.snapshot.Node
    public boolean hasChild(ChildKey name) {
        return !getImmediateChild(name).isEmpty();
    }

    @Override // com.google.firebase.database.snapshot.Node
    public boolean isEmpty() {
        return this.children.isEmpty();
    }

    @Override // com.google.firebase.database.snapshot.Node
    public int getChildCount() {
        return this.children.size();
    }

    @Override // com.google.firebase.database.snapshot.Node
    public Object getValue() {
        return getValue(false);
    }

    @Override // com.google.firebase.database.snapshot.Node
    public Object getValue(boolean useExportFormat) {
        if (isEmpty()) {
            return null;
        }
        int numKeys = 0;
        int maxKey = 0;
        boolean allIntegerKeys = true;
        Map<String, Object> result = new HashMap<>();
        Iterator<Map.Entry<ChildKey, Node>> it = this.children.iterator();
        while (it.hasNext()) {
            Map.Entry<ChildKey, Node> entry = it.next();
            String key = entry.getKey().asString();
            result.put(key, entry.getValue().getValue(useExportFormat));
            numKeys++;
            if (allIntegerKeys) {
                if (key.length() > 1 && key.charAt(0) == '0') {
                    allIntegerKeys = false;
                } else {
                    Integer keyAsInt = Utilities.tryParseInt(key);
                    if (keyAsInt != null && keyAsInt.intValue() >= 0) {
                        if (keyAsInt.intValue() > maxKey) {
                            maxKey = keyAsInt.intValue();
                        }
                    } else {
                        allIntegerKeys = false;
                    }
                }
            }
        }
        if (!useExportFormat && allIntegerKeys && maxKey < numKeys * 2) {
            List<Object> arrayResult = new ArrayList<>(maxKey + 1);
            for (int i = 0; i <= maxKey; i++) {
                arrayResult.add(result.get("" + i));
            }
            return arrayResult;
        }
        if (useExportFormat && !this.priority.isEmpty()) {
            result.put(".priority", this.priority.getValue());
        }
        return result;
    }

    @Override // com.google.firebase.database.snapshot.Node
    public ChildKey getPredecessorChildKey(ChildKey childKey) {
        return this.children.getPredecessorKey(childKey);
    }

    @Override // com.google.firebase.database.snapshot.Node
    public ChildKey getSuccessorChildKey(ChildKey childKey) {
        return this.children.getSuccessorKey(childKey);
    }

    @Override // com.google.firebase.database.snapshot.Node
    public String getHashRepresentation(Node.HashVersion version) {
        if (version != Node.HashVersion.V1) {
            throw new IllegalArgumentException("Hashes on children nodes only supported for V1");
        }
        StringBuilder toHash = new StringBuilder();
        if (!this.priority.isEmpty()) {
            toHash.append("priority:");
            toHash.append(this.priority.getHashRepresentation(Node.HashVersion.V1));
            toHash.append(":");
        }
        List<NamedNode> nodes = new ArrayList<>();
        boolean sawPriority = false;
        Iterator<NamedNode> it = iterator();
        while (it.hasNext()) {
            NamedNode node = it.next();
            nodes.add(node);
            sawPriority = sawPriority || !node.getNode().getPriority().isEmpty();
        }
        if (sawPriority) {
            Collections.sort(nodes, PriorityIndex.getInstance());
        }
        for (NamedNode node2 : nodes) {
            String hashString = node2.getNode().getHash();
            if (!hashString.equals("")) {
                toHash.append(":");
                toHash.append(node2.getName().asString());
                toHash.append(":");
                toHash.append(hashString);
            }
        }
        return toHash.toString();
    }

    @Override // com.google.firebase.database.snapshot.Node
    public String getHash() {
        if (this.lazyHash == null) {
            String hashString = getHashRepresentation(Node.HashVersion.V1);
            this.lazyHash = hashString.isEmpty() ? "" : Utilities.sha1HexDigest(hashString);
        }
        return this.lazyHash;
    }

    @Override // com.google.firebase.database.snapshot.Node
    public boolean isLeafNode() {
        return false;
    }

    @Override // com.google.firebase.database.snapshot.Node
    public Node getPriority() {
        return this.priority;
    }

    @Override // com.google.firebase.database.snapshot.Node
    public Node updatePriority(Node priority) {
        if (this.children.isEmpty()) {
            return EmptyNode.Empty();
        }
        return new ChildrenNode(this.children, priority);
    }

    @Override // com.google.firebase.database.snapshot.Node
    public Node getImmediateChild(ChildKey name) {
        if (name.isPriorityChildName() && !this.priority.isEmpty()) {
            return this.priority;
        }
        if (this.children.containsKey(name)) {
            return this.children.get(name);
        }
        return EmptyNode.Empty();
    }

    @Override // com.google.firebase.database.snapshot.Node
    public Node getChild(Path path) {
        ChildKey front = path.getFront();
        if (front == null) {
            return this;
        }
        return getImmediateChild(front).getChild(path.popFront());
    }

    public void forEachChild(ChildVisitor visitor) {
        forEachChild(visitor, false);
    }

    public void forEachChild(final ChildVisitor visitor, boolean includePriority) {
        if (!includePriority || getPriority().isEmpty()) {
            this.children.inOrderTraversal(visitor);
        } else {
            this.children.inOrderTraversal(new LLRBNode.NodeVisitor<ChildKey, Node>() { // from class: com.google.firebase.database.snapshot.ChildrenNode.2
                boolean passedPriorityKey = false;

                @Override // com.google.firebase.database.collection.LLRBNode.NodeVisitor
                public void visitEntry(ChildKey key, Node value) {
                    if (!this.passedPriorityKey && key.compareTo(ChildKey.getPriorityKey()) > 0) {
                        this.passedPriorityKey = true;
                        visitor.visitChild(ChildKey.getPriorityKey(), ChildrenNode.this.getPriority());
                    }
                    visitor.visitChild(key, value);
                }
            });
        }
    }

    public ChildKey getFirstChildKey() {
        return this.children.getMinKey();
    }

    public ChildKey getLastChildKey() {
        return this.children.getMaxKey();
    }

    @Override // com.google.firebase.database.snapshot.Node
    public Node updateChild(Path path, Node newChildNode) {
        ChildKey front = path.getFront();
        if (front == null) {
            return newChildNode;
        }
        if (front.isPriorityChildName()) {
            Utilities.hardAssert(PriorityUtilities.isValidPriority(newChildNode));
            return updatePriority(newChildNode);
        }
        Node newImmediateChild = getImmediateChild(front).updateChild(path.popFront(), newChildNode);
        return updateImmediateChild(front, newImmediateChild);
    }

    @Override // java.lang.Iterable
    public Iterator<NamedNode> iterator() {
        return new NamedNodeIterator(this.children.iterator());
    }

    @Override // com.google.firebase.database.snapshot.Node
    public Iterator<NamedNode> reverseIterator() {
        return new NamedNodeIterator(this.children.reverseIterator());
    }

    @Override // com.google.firebase.database.snapshot.Node
    public Node updateImmediateChild(ChildKey key, Node newChildNode) {
        if (key.isPriorityChildName()) {
            return updatePriority(newChildNode);
        }
        ImmutableSortedMap<ChildKey, Node> newChildren = this.children;
        if (newChildren.containsKey(key)) {
            newChildren = newChildren.remove(key);
        }
        if (!newChildNode.isEmpty()) {
            newChildren = newChildren.insert(key, newChildNode);
        }
        if (newChildren.isEmpty()) {
            return EmptyNode.Empty();
        }
        return new ChildrenNode(newChildren, this.priority);
    }

    @Override // java.lang.Comparable
    public int compareTo(Node o) {
        if (isEmpty()) {
            return o.isEmpty() ? 0 : -1;
        }
        if (o.isLeafNode() || o.isEmpty()) {
            return 1;
        }
        return o == Node.MAX_NODE ? -1 : 0;
    }

    public boolean equals(Object otherObj) {
        if (otherObj == null) {
            return false;
        }
        if (otherObj == this) {
            return true;
        }
        if (!(otherObj instanceof ChildrenNode)) {
            return false;
        }
        ChildrenNode other = (ChildrenNode) otherObj;
        if (!getPriority().equals(other.getPriority()) || this.children.size() != other.children.size()) {
            return false;
        }
        Iterator<Map.Entry<ChildKey, Node>> thisIterator = this.children.iterator();
        Iterator<Map.Entry<ChildKey, Node>> otherIterator = other.children.iterator();
        while (thisIterator.hasNext() && otherIterator.hasNext()) {
            Map.Entry<ChildKey, Node> thisNameNode = thisIterator.next();
            Map.Entry<ChildKey, Node> otherNamedNode = otherIterator.next();
            if (!thisNameNode.getKey().equals(otherNamedNode.getKey()) || !thisNameNode.getValue().equals(otherNamedNode.getValue())) {
                return false;
            }
        }
        if (!thisIterator.hasNext() && !otherIterator.hasNext()) {
            return true;
        }
        throw new IllegalStateException("Something went wrong internally.");
    }

    public int hashCode() {
        int hashCode = 0;
        Iterator<NamedNode> it = iterator();
        while (it.hasNext()) {
            NamedNode entry = it.next();
            hashCode = (((hashCode * 31) + entry.getName().hashCode()) * 17) + entry.getNode().hashCode();
        }
        return hashCode;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder, 0);
        return builder.toString();
    }

    private static void addIndentation(StringBuilder builder, int indentation) {
        for (int i = 0; i < indentation; i++) {
            builder.append(" ");
        }
    }

    private void toString(StringBuilder builder, int indentation) {
        if (this.children.isEmpty() && this.priority.isEmpty()) {
            builder.append("{ }");
            return;
        }
        builder.append("{\n");
        Iterator<Map.Entry<ChildKey, Node>> it = this.children.iterator();
        while (it.hasNext()) {
            Map.Entry<ChildKey, Node> childEntry = it.next();
            addIndentation(builder, indentation + 2);
            builder.append(childEntry.getKey().asString());
            builder.append("=");
            if (childEntry.getValue() instanceof ChildrenNode) {
                ChildrenNode childrenNode = (ChildrenNode) childEntry.getValue();
                childrenNode.toString(builder, indentation + 2);
            } else {
                builder.append(childEntry.getValue().toString());
            }
            builder.append("\n");
        }
        if (!this.priority.isEmpty()) {
            addIndentation(builder, indentation + 2);
            builder.append(".priority=");
            builder.append(this.priority.toString());
            builder.append("\n");
        }
        addIndentation(builder, indentation);
        builder.append("}");
    }
}
