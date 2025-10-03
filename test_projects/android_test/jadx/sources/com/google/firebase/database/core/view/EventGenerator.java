package com.google.firebase.database.core.view;

import com.google.firebase.database.core.EventRegistration;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.core.view.Event;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.Index;
import com.google.firebase.database.snapshot.IndexedNode;
import com.google.firebase.database.snapshot.NamedNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/* loaded from: classes.dex */
public class EventGenerator {
    private final Index index;
    private final QuerySpec query;

    public EventGenerator(QuerySpec query) {
        this.query = query;
        this.index = query.getIndex();
    }

    private void generateEventsForType(List<DataEvent> events, Event.EventType type, List<Change> changes, List<EventRegistration> eventRegistrations, IndexedNode eventCache) {
        List<Change> filteredChanges = new ArrayList<>();
        for (Change change : changes) {
            if (change.getEventType().equals(type)) {
                filteredChanges.add(change);
            }
        }
        Collections.sort(filteredChanges, changeComparator());
        for (Change change2 : filteredChanges) {
            for (EventRegistration registration : eventRegistrations) {
                if (registration.respondsTo(type)) {
                    events.add(generateEvent(change2, registration, eventCache));
                }
            }
        }
    }

    private DataEvent generateEvent(Change change, EventRegistration registration, IndexedNode eventCache) {
        Change newChange;
        if (change.getEventType().equals(Event.EventType.VALUE) || change.getEventType().equals(Event.EventType.CHILD_REMOVED)) {
            newChange = change;
        } else {
            ChildKey prevChildKey = eventCache.getPredecessorChildName(change.getChildKey(), change.getIndexedNode().getNode(), this.index);
            newChange = change.changeWithPrevName(prevChildKey);
        }
        return registration.createEvent(newChange, this.query);
    }

    public List<DataEvent> generateEventsForChanges(List<Change> changes, IndexedNode eventCache, List<EventRegistration> eventRegistrations) {
        List<DataEvent> events = new ArrayList<>();
        List<Change> moves = new ArrayList<>();
        for (Change change : changes) {
            if (change.getEventType().equals(Event.EventType.CHILD_CHANGED) && this.index.indexedValueChanged(change.getOldIndexedNode().getNode(), change.getIndexedNode().getNode())) {
                moves.add(Change.childMovedChange(change.getChildKey(), change.getIndexedNode()));
            }
        }
        generateEventsForType(events, Event.EventType.CHILD_REMOVED, changes, eventRegistrations, eventCache);
        generateEventsForType(events, Event.EventType.CHILD_ADDED, changes, eventRegistrations, eventCache);
        generateEventsForType(events, Event.EventType.CHILD_MOVED, moves, eventRegistrations, eventCache);
        generateEventsForType(events, Event.EventType.CHILD_CHANGED, changes, eventRegistrations, eventCache);
        generateEventsForType(events, Event.EventType.VALUE, changes, eventRegistrations, eventCache);
        return events;
    }

    private Comparator<Change> changeComparator() {
        return new Comparator<Change>() { // from class: com.google.firebase.database.core.view.EventGenerator.1
            @Override // java.util.Comparator
            public int compare(Change a, Change b) {
                Utilities.hardAssert((a.getChildKey() == null || b.getChildKey() == null) ? false : true);
                NamedNode namedNodeA = new NamedNode(a.getChildKey(), a.getIndexedNode().getNode());
                NamedNode namedNodeB = new NamedNode(b.getChildKey(), b.getIndexedNode().getNode());
                return EventGenerator.this.index.compare(namedNodeA, namedNodeB);
            }
        };
    }
}
