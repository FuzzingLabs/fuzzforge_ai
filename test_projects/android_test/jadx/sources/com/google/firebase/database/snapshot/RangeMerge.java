package com.google.firebase.database.snapshot;

import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.utilities.Utilities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/* loaded from: classes.dex */
public class RangeMerge {
    private final Path optExclusiveStart;
    private final Path optInclusiveEnd;
    private final Node snap;

    public RangeMerge(Path optExclusiveStart, Path optInclusiveEnd, Node snap) {
        this.optExclusiveStart = optExclusiveStart;
        this.optInclusiveEnd = optInclusiveEnd;
        this.snap = snap;
    }

    public RangeMerge(com.google.firebase.database.connection.RangeMerge rangeMerge) {
        List<String> optStartPathList = rangeMerge.getOptExclusiveStart();
        this.optExclusiveStart = optStartPathList != null ? new Path(optStartPathList) : null;
        List<String> optEndPathList = rangeMerge.getOptInclusiveEnd();
        this.optInclusiveEnd = optEndPathList != null ? new Path(optEndPathList) : null;
        this.snap = NodeUtilities.NodeFromJSON(rangeMerge.getSnap());
    }

    public Node applyTo(Node node) {
        return updateRangeInNode(Path.getEmptyPath(), node, this.snap);
    }

    Path getStart() {
        return this.optExclusiveStart;
    }

    Path getEnd() {
        return this.optInclusiveEnd;
    }

    private Node updateRangeInNode(Path currentPath, Node node, Node updateNode) {
        Path path = currentPath;
        Path path2 = this.optExclusiveStart;
        boolean z = true;
        int startComparison = path2 == null ? 1 : path.compareTo(path2);
        Path path3 = this.optInclusiveEnd;
        int endComparison = path3 == null ? -1 : path.compareTo(path3);
        Path path4 = this.optExclusiveStart;
        boolean startInNode = path4 != null && path.contains(path4);
        Path path5 = this.optInclusiveEnd;
        boolean endInNode = path5 != null && path.contains(path5);
        if (startComparison > 0 && endComparison < 0 && !endInNode) {
            return updateNode;
        }
        if (startComparison > 0 && endInNode && updateNode.isLeafNode()) {
            return updateNode;
        }
        if (startComparison > 0 && endComparison == 0) {
            Utilities.hardAssert(endInNode);
            Utilities.hardAssert(true ^ updateNode.isLeafNode());
            return node.isLeafNode() ? EmptyNode.Empty() : node;
        }
        if (startInNode || endInNode) {
            HashSet hashSet = new HashSet();
            for (NamedNode child : node) {
                hashSet.add(child.getName());
            }
            for (NamedNode child2 : updateNode) {
                hashSet.add(child2.getName());
            }
            List<ChildKey> inOrder = new ArrayList<>(hashSet.size() + 1);
            inOrder.addAll(hashSet);
            if (!updateNode.getPriority().isEmpty() || !node.getPriority().isEmpty()) {
                inOrder.add(ChildKey.getPriorityKey());
            }
            Node newNode = node;
            for (ChildKey key : inOrder) {
                Node currentChild = node.getImmediateChild(key);
                Node updatedChild = updateRangeInNode(path.child(key), node.getImmediateChild(key), updateNode.getImmediateChild(key));
                if (updatedChild != currentChild) {
                    newNode = newNode.updateImmediateChild(key, updatedChild);
                }
                path = currentPath;
            }
            return newNode;
        }
        if (endComparison <= 0 && startComparison > 0) {
            z = false;
        }
        Utilities.hardAssert(z);
        return node;
    }

    public String toString() {
        return "RangeMerge{optExclusiveStart=" + this.optExclusiveStart + ", optInclusiveEnd=" + this.optInclusiveEnd + ", snap=" + this.snap + '}';
    }
}
