package com.google.firebase.database.core;

import com.google.firebase.database.core.ValueProvider;
import com.google.firebase.database.core.utilities.Clock;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.ChildrenNode;
import com.google.firebase.database.snapshot.Node;
import com.google.firebase.database.snapshot.NodeUtilities;
import com.google.firebase.database.snapshot.PriorityUtilities;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* loaded from: classes.dex */
public class ServerValues {
    public static final String NAME_OP_INCREMENT = "increment";
    public static final String NAME_OP_TIMESTAMP = "timestamp";
    public static final String NAME_SUBKEY_SERVERVALUE = ".sv";

    public static Map<String, Object> generateServerValues(Clock clock) {
        Map<String, Object> values = new HashMap<>();
        values.put(NAME_OP_TIMESTAMP, Long.valueOf(clock.millis()));
        return values;
    }

    public static Object resolveDeferredLeafValue(Object value, ValueProvider existing, Map<String, Object> serverValues) {
        if (!(value instanceof Map)) {
            return value;
        }
        Map mapValue = (Map) value;
        if (!mapValue.containsKey(NAME_SUBKEY_SERVERVALUE)) {
            return value;
        }
        Object op = mapValue.get(NAME_SUBKEY_SERVERVALUE);
        Object res = null;
        if (op instanceof String) {
            res = resolveScalarDeferredValue((String) op, serverValues);
        } else if (op instanceof Map) {
            res = resolveComplexDeferredValue((Map) op, existing, serverValues);
        }
        if (res == null) {
            return value;
        }
        return res;
    }

    static Object resolveScalarDeferredValue(String op, Map<String, Object> serverValues) {
        if (NAME_OP_TIMESTAMP.equals(op) && serverValues.containsKey(op)) {
            return serverValues.get(op);
        }
        return null;
    }

    static Object resolveComplexDeferredValue(Map<String, Object> op, ValueProvider existing, Map<String, Object> serverValues) {
        if (!op.containsKey(NAME_OP_INCREMENT)) {
            return null;
        }
        Object incrObject = op.get(NAME_OP_INCREMENT);
        if (!(incrObject instanceof Number)) {
            return null;
        }
        Number increment = (Number) incrObject;
        Node existingNode = existing.node();
        if (!existingNode.isLeafNode() || !(existingNode.getValue() instanceof Number)) {
            return increment;
        }
        Number existingVal = (Number) existingNode.getValue();
        if (canBeRepresentedAsLong(increment) && canBeRepresentedAsLong(existingVal)) {
            long x = increment.longValue();
            long y = existingVal.longValue();
            long r = x + y;
            if (((x ^ r) & (y ^ r)) >= 0) {
                return Long.valueOf(r);
            }
        }
        return Double.valueOf(increment.doubleValue() + existingVal.doubleValue());
    }

    public static Node resolveDeferredValueSnapshot(Node data, Node existing, Map<String, Object> serverValues) {
        return resolveDeferredValueSnapshot(data, new ValueProvider.ExistingValueProvider(existing), serverValues);
    }

    public static Node resolveDeferredValueSnapshot(Node data, SyncTree syncTree, Path path, Map<String, Object> serverValues) {
        return resolveDeferredValueSnapshot(data, new ValueProvider.DeferredValueProvider(syncTree, path), serverValues);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Node resolveDeferredValueSnapshot(Node data, final ValueProvider existing, final Map<String, Object> serverValues) {
        Object rawPriority = data.getPriority().getValue();
        Object priority = resolveDeferredLeafValue(rawPriority, existing.getImmediateChild(ChildKey.fromString(".priority")), serverValues);
        if (data.isLeafNode()) {
            Object value = resolveDeferredLeafValue(data.getValue(), existing, serverValues);
            if (!value.equals(data.getValue()) || !Utilities.equals(priority, rawPriority)) {
                return NodeUtilities.NodeFromJSON(value, PriorityUtilities.parsePriority(priority));
            }
            return data;
        }
        if (data.isEmpty()) {
            return data;
        }
        ChildrenNode childNode = (ChildrenNode) data;
        final SnapshotHolder holder = new SnapshotHolder(childNode);
        childNode.forEachChild(new ChildrenNode.ChildVisitor() { // from class: com.google.firebase.database.core.ServerValues.1
            @Override // com.google.firebase.database.snapshot.ChildrenNode.ChildVisitor
            public void visitChild(ChildKey name, Node child) {
                Node newChildNode = ServerValues.resolveDeferredValueSnapshot(child, ValueProvider.this.getImmediateChild(name), (Map<String, Object>) serverValues);
                if (newChildNode != child) {
                    holder.update(new Path(name.asString()), newChildNode);
                }
            }
        });
        if (!holder.getRootNode().getPriority().equals(priority)) {
            return holder.getRootNode().updatePriority(PriorityUtilities.parsePriority(priority));
        }
        return holder.getRootNode();
    }

    public static CompoundWrite resolveDeferredValueMerge(CompoundWrite merge, SyncTree syncTree, Path path, Map<String, Object> serverValues) {
        CompoundWrite write = CompoundWrite.emptyWrite();
        Iterator<Map.Entry<Path, Node>> it = merge.iterator();
        while (it.hasNext()) {
            Map.Entry<Path, Node> entry = it.next();
            ValueProvider deferredValue = new ValueProvider.DeferredValueProvider(syncTree, path.child(entry.getKey()));
            write = write.addWrite(entry.getKey(), resolveDeferredValueSnapshot(entry.getValue(), deferredValue, serverValues));
        }
        return write;
    }

    private static boolean canBeRepresentedAsLong(Number x) {
        return ((x instanceof Double) || (x instanceof Float)) ? false : true;
    }
}
