package com.google.firebase.database.core.view;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.core.EventRegistration;
import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.WriteTreeRef;
import com.google.firebase.database.core.operation.Operation;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.core.view.ViewProcessor;
import com.google.firebase.database.core.view.filter.IndexedFilter;
import com.google.firebase.database.core.view.filter.NodeFilter;
import com.google.firebase.database.snapshot.EmptyNode;
import com.google.firebase.database.snapshot.IndexedNode;
import com.google.firebase.database.snapshot.NamedNode;
import com.google.firebase.database.snapshot.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/* loaded from: classes.dex */
public class View {
    private final EventGenerator eventGenerator;
    private final List<EventRegistration> eventRegistrations;
    private final ViewProcessor processor;
    private final QuerySpec query;
    private ViewCache viewCache;

    public View(QuerySpec query, ViewCache initialViewCache) {
        this.query = query;
        IndexedFilter indexFilter = new IndexedFilter(query.getIndex());
        NodeFilter filter = query.getParams().getNodeFilter();
        this.processor = new ViewProcessor(filter);
        CacheNode initialServerCache = initialViewCache.getServerCache();
        CacheNode initialEventCache = initialViewCache.getEventCache();
        IndexedNode emptyIndexedNode = IndexedNode.from(EmptyNode.Empty(), query.getIndex());
        IndexedNode serverSnap = indexFilter.updateFullNode(emptyIndexedNode, initialServerCache.getIndexedNode(), null);
        IndexedNode eventSnap = filter.updateFullNode(emptyIndexedNode, initialEventCache.getIndexedNode(), null);
        CacheNode newServerCache = new CacheNode(serverSnap, initialServerCache.isFullyInitialized(), indexFilter.filtersNodes());
        CacheNode newEventCache = new CacheNode(eventSnap, initialEventCache.isFullyInitialized(), filter.filtersNodes());
        this.viewCache = new ViewCache(newEventCache, newServerCache);
        this.eventRegistrations = new ArrayList();
        this.eventGenerator = new EventGenerator(query);
    }

    public static class OperationResult {
        public final List<Change> changes;
        public final List<DataEvent> events;

        public OperationResult(List<DataEvent> events, List<Change> changes) {
            this.events = events;
            this.changes = changes;
        }
    }

    public QuerySpec getQuery() {
        return this.query;
    }

    public Node getCompleteNode() {
        return this.viewCache.getCompleteEventSnap();
    }

    public Node getServerCache() {
        return this.viewCache.getServerCache().getNode();
    }

    public Node getEventCache() {
        return this.viewCache.getEventCache().getNode();
    }

    public Node getCompleteServerCache(Path path) {
        Node cache = this.viewCache.getCompleteServerSnap();
        if (cache != null) {
            if (this.query.loadsAllData() || (!path.isEmpty() && !cache.getImmediateChild(path.getFront()).isEmpty())) {
                return cache.getChild(path);
            }
            return null;
        }
        return null;
    }

    public boolean isEmpty() {
        return this.eventRegistrations.isEmpty();
    }

    public void addEventRegistration(EventRegistration registration) {
        this.eventRegistrations.add(registration);
    }

    public List<Event> removeEventRegistration(EventRegistration registration, DatabaseError cancelError) {
        List<Event> cancelEvents;
        if (cancelError != null) {
            cancelEvents = new ArrayList<>();
            Utilities.hardAssert(registration == null, "A cancel should cancel all event registrations");
            Path path = this.query.getPath();
            for (EventRegistration eventRegistration : this.eventRegistrations) {
                cancelEvents.add(new CancelEvent(eventRegistration, cancelError, path));
            }
        } else {
            cancelEvents = Collections.emptyList();
        }
        if (registration != null) {
            int indexToDelete = -1;
            for (int i = 0; i < this.eventRegistrations.size(); i++) {
                EventRegistration candidate = this.eventRegistrations.get(i);
                if (candidate.isSameListener(registration)) {
                    indexToDelete = i;
                    if (candidate.isZombied()) {
                        break;
                    }
                }
            }
            if (indexToDelete != -1) {
                EventRegistration deletedRegistration = this.eventRegistrations.get(indexToDelete);
                this.eventRegistrations.remove(indexToDelete);
                deletedRegistration.zombify();
            }
        } else {
            for (EventRegistration eventRegistration2 : this.eventRegistrations) {
                eventRegistration2.zombify();
            }
            this.eventRegistrations.clear();
        }
        return cancelEvents;
    }

    public OperationResult applyOperation(Operation operation, WriteTreeRef writesCache, Node optCompleteServerCache) {
        if (operation.getType() == Operation.OperationType.Merge && operation.getSource().getQueryParams() != null) {
            Utilities.hardAssert(this.viewCache.getCompleteServerSnap() != null, "We should always have a full cache before handling merges");
            Utilities.hardAssert(this.viewCache.getCompleteEventSnap() != null, "Missing event cache, even though we have a server cache");
        }
        ViewCache oldViewCache = this.viewCache;
        ViewProcessor.ProcessorResult result = this.processor.applyOperation(oldViewCache, operation, writesCache, optCompleteServerCache);
        Utilities.hardAssert(result.viewCache.getServerCache().isFullyInitialized() || !oldViewCache.getServerCache().isFullyInitialized(), "Once a server snap is complete, it should never go back");
        this.viewCache = result.viewCache;
        List<DataEvent> events = generateEventsForChanges(result.changes, result.viewCache.getEventCache().getIndexedNode(), null);
        return new OperationResult(events, result.changes);
    }

    public List<DataEvent> getInitialEvents(EventRegistration registration) {
        CacheNode eventSnap = this.viewCache.getEventCache();
        List<Change> initialChanges = new ArrayList<>();
        for (NamedNode child : eventSnap.getNode()) {
            initialChanges.add(Change.childAddedChange(child.getName(), child.getNode()));
        }
        if (eventSnap.isFullyInitialized()) {
            initialChanges.add(Change.valueChange(eventSnap.getIndexedNode()));
        }
        return generateEventsForChanges(initialChanges, eventSnap.getIndexedNode(), registration);
    }

    private List<DataEvent> generateEventsForChanges(List<Change> changes, IndexedNode eventCache, EventRegistration registration) {
        List<EventRegistration> registrations;
        if (registration == null) {
            registrations = this.eventRegistrations;
        } else {
            registrations = Arrays.asList(registration);
        }
        return this.eventGenerator.generateEventsForChanges(changes, eventCache, registrations);
    }

    List<EventRegistration> getEventRegistrations() {
        return this.eventRegistrations;
    }
}
