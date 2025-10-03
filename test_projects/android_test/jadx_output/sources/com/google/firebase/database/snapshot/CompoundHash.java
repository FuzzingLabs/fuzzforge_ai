package com.google.firebase.database.snapshot;

import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.utilities.NodeSizeEstimator;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.snapshot.ChildrenNode;
import com.google.firebase.database.snapshot.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/* loaded from: classes.dex */
public class CompoundHash {
    private final List<String> hashes;
    private final List<Path> posts;

    /* loaded from: classes.dex */
    public interface SplitStrategy {
        boolean shouldSplit(CompoundHashBuilder compoundHashBuilder);
    }

    private CompoundHash(List<Path> posts, List<String> hashes) {
        if (posts.size() != hashes.size() - 1) {
            throw new IllegalArgumentException("Number of posts need to be n-1 for n hashes in CompoundHash");
        }
        this.posts = posts;
        this.hashes = hashes;
    }

    public List<Path> getPosts() {
        return Collections.unmodifiableList(this.posts);
    }

    public List<String> getHashes() {
        return Collections.unmodifiableList(this.hashes);
    }

    /* loaded from: classes.dex */
    private static class SimpleSizeSplitStrategy implements SplitStrategy {
        private final long splitThreshold;

        public SimpleSizeSplitStrategy(Node node) {
            long estimatedNodeSize = NodeSizeEstimator.estimateSerializedNodeSize(node);
            this.splitThreshold = Math.max(512L, (long) Math.sqrt(100 * estimatedNodeSize));
        }

        @Override // com.google.firebase.database.snapshot.CompoundHash.SplitStrategy
        public boolean shouldSplit(CompoundHashBuilder state) {
            return ((long) state.currentHashLength()) > this.splitThreshold && (state.currentPath().isEmpty() || !state.currentPath().getBack().equals(ChildKey.getPriorityKey()));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class CompoundHashBuilder {
        private int currentPathDepth;
        private final SplitStrategy splitStrategy;
        private StringBuilder optHashValueBuilder = null;
        private Stack<ChildKey> currentPath = new Stack<>();
        private int lastLeafDepth = -1;
        private boolean needsComma = true;
        private final List<Path> currentPaths = new ArrayList();
        private final List<String> currentHashes = new ArrayList();

        public CompoundHashBuilder(SplitStrategy strategy) {
            this.splitStrategy = strategy;
        }

        public boolean buildingRange() {
            return this.optHashValueBuilder != null;
        }

        public int currentHashLength() {
            return this.optHashValueBuilder.length();
        }

        public Path currentPath() {
            return currentPath(this.currentPathDepth);
        }

        private Path currentPath(int depth) {
            ChildKey[] segments = new ChildKey[depth];
            for (int i = 0; i < depth; i++) {
                segments[i] = this.currentPath.get(i);
            }
            return new Path(segments);
        }

        private void ensureRange() {
            if (!buildingRange()) {
                StringBuilder sb = new StringBuilder();
                this.optHashValueBuilder = sb;
                sb.append("(");
                Iterator<ChildKey> it = currentPath(this.currentPathDepth).iterator();
                while (it.hasNext()) {
                    ChildKey key = it.next();
                    appendKey(this.optHashValueBuilder, key);
                    this.optHashValueBuilder.append(":(");
                }
                this.needsComma = false;
            }
        }

        private void appendKey(StringBuilder builder, ChildKey key) {
            builder.append(Utilities.stringHashV2Representation(key.asString()));
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void processLeaf(LeafNode<?> node) {
            ensureRange();
            this.lastLeafDepth = this.currentPathDepth;
            this.optHashValueBuilder.append(node.getHashRepresentation(Node.HashVersion.V2));
            this.needsComma = true;
            if (this.splitStrategy.shouldSplit(this)) {
                endRange();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void startChild(ChildKey key) {
            ensureRange();
            if (this.needsComma) {
                this.optHashValueBuilder.append(",");
            }
            appendKey(this.optHashValueBuilder, key);
            this.optHashValueBuilder.append(":(");
            if (this.currentPathDepth == this.currentPath.size()) {
                this.currentPath.add(key);
            } else {
                this.currentPath.set(this.currentPathDepth, key);
            }
            this.currentPathDepth++;
            this.needsComma = false;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void endChild() {
            this.currentPathDepth--;
            if (buildingRange()) {
                this.optHashValueBuilder.append(")");
            }
            this.needsComma = true;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void finishHashing() {
            Utilities.hardAssert(this.currentPathDepth == 0, "Can't finish hashing in the middle processing a child");
            if (buildingRange()) {
                endRange();
            }
            this.currentHashes.add("");
        }

        private void endRange() {
            Utilities.hardAssert(buildingRange(), "Can't end range without starting a range!");
            for (int i = 0; i < this.currentPathDepth; i++) {
                this.optHashValueBuilder.append(")");
            }
            this.optHashValueBuilder.append(")");
            Path lastLeafPath = currentPath(this.lastLeafDepth);
            String hash = Utilities.sha1HexDigest(this.optHashValueBuilder.toString());
            this.currentHashes.add(hash);
            this.currentPaths.add(lastLeafPath);
            this.optHashValueBuilder = null;
        }
    }

    public static CompoundHash fromNode(Node node) {
        return fromNode(node, new SimpleSizeSplitStrategy(node));
    }

    public static CompoundHash fromNode(Node node, SplitStrategy strategy) {
        if (node.isEmpty()) {
            return new CompoundHash(Collections.emptyList(), Collections.singletonList(""));
        }
        CompoundHashBuilder state = new CompoundHashBuilder(strategy);
        processNode(node, state);
        state.finishHashing();
        return new CompoundHash(state.currentPaths, state.currentHashes);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void processNode(Node node, final CompoundHashBuilder state) {
        if (node.isLeafNode()) {
            state.processLeaf((LeafNode) node);
            return;
        }
        if (node.isEmpty()) {
            throw new IllegalArgumentException("Can't calculate hash on empty node!");
        }
        if (!(node instanceof ChildrenNode)) {
            throw new IllegalStateException("Expected children node, but got: " + node);
        }
        ChildrenNode childrenNode = (ChildrenNode) node;
        ChildrenNode.ChildVisitor visitor = new ChildrenNode.ChildVisitor() { // from class: com.google.firebase.database.snapshot.CompoundHash.1
            @Override // com.google.firebase.database.snapshot.ChildrenNode.ChildVisitor
            public void visitChild(ChildKey name, Node child) {
                CompoundHashBuilder.this.startChild(name);
                CompoundHash.processNode(child, CompoundHashBuilder.this);
                CompoundHashBuilder.this.endChild();
            }
        };
        childrenNode.forEachChild(visitor, true);
    }
}
