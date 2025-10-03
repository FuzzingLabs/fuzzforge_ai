package com.google.firebase.database.core.utilities;

import com.google.firebase.database.snapshot.BooleanNode;
import com.google.firebase.database.snapshot.ChildrenNode;
import com.google.firebase.database.snapshot.DoubleNode;
import com.google.firebase.database.snapshot.LeafNode;
import com.google.firebase.database.snapshot.LongNode;
import com.google.firebase.database.snapshot.NamedNode;
import com.google.firebase.database.snapshot.Node;
import com.google.firebase.database.snapshot.StringNode;

/* loaded from: classes.dex */
public class NodeSizeEstimator {
    private static final int LEAF_PRIORITY_OVERHEAD = 24;

    private static long estimateLeafNodeSize(LeafNode<?> node) {
        long valueSize;
        if (node instanceof DoubleNode) {
            valueSize = 8;
        } else if (node instanceof LongNode) {
            valueSize = 8;
        } else if (node instanceof BooleanNode) {
            valueSize = 4;
        } else if (node instanceof StringNode) {
            valueSize = 2 + ((String) node.getValue()).length();
        } else {
            throw new IllegalArgumentException("Unknown leaf node type: " + node.getClass());
        }
        if (node.getPriority().isEmpty()) {
            return valueSize;
        }
        return 24 + valueSize + estimateLeafNodeSize((LeafNode) node.getPriority());
    }

    public static long estimateSerializedNodeSize(Node node) {
        if (node.isEmpty()) {
            return 4L;
        }
        if (node.isLeafNode()) {
            return estimateLeafNodeSize((LeafNode) node);
        }
        Utilities.hardAssert(node instanceof ChildrenNode, "Unexpected node type: " + node.getClass());
        long sum = 1;
        for (NamedNode entry : node) {
            sum = sum + entry.getName().asString().length() + 4 + estimateSerializedNodeSize(entry.getNode());
        }
        if (!node.getPriority().isEmpty()) {
            return sum + 12 + estimateLeafNodeSize((LeafNode) node.getPriority());
        }
        return sum;
    }

    public static int nodeCount(Node node) {
        if (node.isEmpty()) {
            return 0;
        }
        if (node.isLeafNode()) {
            return 1;
        }
        Utilities.hardAssert(node instanceof ChildrenNode, "Unexpected node type: " + node.getClass());
        int sum = 0;
        for (NamedNode entry : node) {
            sum += nodeCount(entry.getNode());
        }
        return sum;
    }
}
