package com.google.firebase.database.snapshot;

/* loaded from: classes.dex */
public class PriorityIndex extends Index {
    private static final PriorityIndex INSTANCE = new PriorityIndex();

    public static PriorityIndex getInstance() {
        return INSTANCE;
    }

    private PriorityIndex() {
    }

    @Override // java.util.Comparator
    public int compare(NamedNode a, NamedNode b) {
        Node aPrio = a.getNode().getPriority();
        Node bPrio = b.getNode().getPriority();
        return NodeUtilities.nameAndPriorityCompare(a.getName(), aPrio, b.getName(), bPrio);
    }

    @Override // com.google.firebase.database.snapshot.Index
    public boolean isDefinedOn(Node a) {
        return !a.getPriority().isEmpty();
    }

    @Override // com.google.firebase.database.snapshot.Index
    public NamedNode makePost(ChildKey name, Node value) {
        return new NamedNode(name, new StringNode("[PRIORITY-POST]", value));
    }

    @Override // com.google.firebase.database.snapshot.Index
    public NamedNode maxPost() {
        return makePost(ChildKey.getMaxName(), Node.MAX_NODE);
    }

    @Override // com.google.firebase.database.snapshot.Index
    public String getQueryDefinition() {
        throw new IllegalArgumentException("Can't get query definition on priority index!");
    }

    @Override // java.util.Comparator
    public boolean equals(Object o) {
        return o instanceof PriorityIndex;
    }

    public int hashCode() {
        return 3155577;
    }

    public String toString() {
        return "PriorityIndex";
    }
}
