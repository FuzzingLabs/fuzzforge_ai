package com.google.firebase.database.core;

import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.Node;
import java.util.ArrayList;

/* loaded from: classes.dex */
abstract class ValueProvider {
    public abstract ValueProvider getImmediateChild(ChildKey childKey);

    public abstract Node node();

    ValueProvider() {
    }

    public static class ExistingValueProvider extends ValueProvider {
        private final Node node;

        ExistingValueProvider(Node node) {
            this.node = node;
        }

        @Override // com.google.firebase.database.core.ValueProvider
        public ValueProvider getImmediateChild(ChildKey childKey) {
            Node child = this.node.getImmediateChild(childKey);
            return new ExistingValueProvider(child);
        }

        @Override // com.google.firebase.database.core.ValueProvider
        public Node node() {
            return this.node;
        }
    }

    public static class DeferredValueProvider extends ValueProvider {
        private final Path path;
        private final SyncTree syncTree;

        DeferredValueProvider(SyncTree syncTree, Path path) {
            this.syncTree = syncTree;
            this.path = path;
        }

        @Override // com.google.firebase.database.core.ValueProvider
        public ValueProvider getImmediateChild(ChildKey childKey) {
            Path child = this.path.child(childKey);
            return new DeferredValueProvider(this.syncTree, child);
        }

        @Override // com.google.firebase.database.core.ValueProvider
        public Node node() {
            return this.syncTree.calcCompleteEventCache(this.path, new ArrayList());
        }
    }
}
