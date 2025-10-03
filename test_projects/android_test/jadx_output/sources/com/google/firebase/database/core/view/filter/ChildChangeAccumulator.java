package com.google.firebase.database.core.view.filter;

import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.core.view.Change;
import com.google.firebase.database.core.view.Event;
import com.google.firebase.database.snapshot.ChildKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class ChildChangeAccumulator {
    private final Map<ChildKey, Change> changeMap = new HashMap();

    public void trackChildChange(Change change) {
        Event.EventType type = change.getEventType();
        ChildKey childKey = change.getChildKey();
        Utilities.hardAssert(type == Event.EventType.CHILD_ADDED || type == Event.EventType.CHILD_CHANGED || type == Event.EventType.CHILD_REMOVED, "Only child changes supported for tracking");
        Utilities.hardAssert(!change.getChildKey().isPriorityChildName());
        if (this.changeMap.containsKey(childKey)) {
            Change oldChange = this.changeMap.get(childKey);
            Event.EventType oldType = oldChange.getEventType();
            if (type == Event.EventType.CHILD_ADDED && oldType == Event.EventType.CHILD_REMOVED) {
                this.changeMap.put(change.getChildKey(), Change.childChangedChange(childKey, change.getIndexedNode(), oldChange.getIndexedNode()));
                return;
            }
            if (type == Event.EventType.CHILD_REMOVED && oldType == Event.EventType.CHILD_ADDED) {
                this.changeMap.remove(childKey);
                return;
            }
            if (type == Event.EventType.CHILD_REMOVED && oldType == Event.EventType.CHILD_CHANGED) {
                this.changeMap.put(childKey, Change.childRemovedChange(childKey, oldChange.getOldIndexedNode()));
                return;
            }
            if (type == Event.EventType.CHILD_CHANGED && oldType == Event.EventType.CHILD_ADDED) {
                this.changeMap.put(childKey, Change.childAddedChange(childKey, change.getIndexedNode()));
                return;
            }
            if (type == Event.EventType.CHILD_CHANGED && oldType == Event.EventType.CHILD_CHANGED) {
                this.changeMap.put(childKey, Change.childChangedChange(childKey, change.getIndexedNode(), oldChange.getOldIndexedNode()));
                return;
            }
            throw new IllegalStateException("Illegal combination of changes: " + change + " occurred after " + oldChange);
        }
        this.changeMap.put(change.getChildKey(), change);
    }

    public List<Change> getChanges() {
        return new ArrayList(this.changeMap.values());
    }
}
