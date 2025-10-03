package com.google.firebase.database.snapshot;

import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.core.Path;

/* loaded from: classes.dex */
public class PriorityUtilities {
    public static Node NullPriority() {
        return EmptyNode.Empty();
    }

    public static boolean isValidPriority(Node priority) {
        return priority.getPriority().isEmpty() && (priority.isEmpty() || (priority instanceof DoubleNode) || (priority instanceof StringNode) || (priority instanceof DeferredValueNode));
    }

    public static Node parsePriority(Object value) {
        return parsePriority(null, value);
    }

    public static Node parsePriority(Path nodePath, Object value) {
        String str;
        Node priority = NodeUtilities.NodeFromJSON(value);
        if (priority instanceof LongNode) {
            priority = new DoubleNode(Double.valueOf(((Long) priority.getValue()).longValue()), NullPriority());
        }
        if (!isValidPriority(priority)) {
            StringBuilder sb = new StringBuilder();
            if (nodePath != null) {
                str = "Path '" + nodePath + "'";
            } else {
                str = "Node";
            }
            sb.append(str);
            sb.append(" contains invalid priority: Must be a string, double, ServerValue, or null");
            throw new DatabaseException(sb.toString());
        }
        return priority;
    }
}
