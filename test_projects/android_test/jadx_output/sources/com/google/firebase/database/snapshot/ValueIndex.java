package com.google.firebase.database.snapshot;

/* loaded from: classes.dex */
public class ValueIndex extends Index {
    private static final ValueIndex INSTANCE = new ValueIndex();

    private ValueIndex() {
    }

    public static ValueIndex getInstance() {
        return INSTANCE;
    }

    @Override // com.google.firebase.database.snapshot.Index
    public boolean isDefinedOn(Node a) {
        return true;
    }

    @Override // com.google.firebase.database.snapshot.Index
    public NamedNode makePost(ChildKey name, Node value) {
        return new NamedNode(name, value);
    }

    @Override // com.google.firebase.database.snapshot.Index
    public NamedNode maxPost() {
        return new NamedNode(ChildKey.getMaxName(), Node.MAX_NODE);
    }

    @Override // com.google.firebase.database.snapshot.Index
    public String getQueryDefinition() {
        return ".value";
    }

    @Override // java.util.Comparator
    public int compare(NamedNode one, NamedNode two) {
        int indexCmp = one.getNode().compareTo(two.getNode());
        if (indexCmp == 0) {
            return one.getName().compareTo(two.getName());
        }
        return indexCmp;
    }

    public int hashCode() {
        return 4;
    }

    @Override // java.util.Comparator
    public boolean equals(Object o) {
        return o instanceof ValueIndex;
    }

    public String toString() {
        return "ValueIndex";
    }
}
