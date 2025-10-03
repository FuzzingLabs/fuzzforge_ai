package com.google.firebase.database.core;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.core.operation.Operation;
import com.google.firebase.database.core.persistence.PersistenceManager;
import com.google.firebase.database.core.utilities.Pair;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.core.view.CacheNode;
import com.google.firebase.database.core.view.Change;
import com.google.firebase.database.core.view.DataEvent;
import com.google.firebase.database.core.view.Event;
import com.google.firebase.database.core.view.QueryParams;
import com.google.firebase.database.core.view.QuerySpec;
import com.google.firebase.database.core.view.View;
import com.google.firebase.database.core.view.ViewCache;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.EmptyNode;
import com.google.firebase.database.snapshot.IndexedNode;
import com.google.firebase.database.snapshot.NamedNode;
import com.google.firebase.database.snapshot.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public class SyncPoint {
    private final PersistenceManager persistenceManager;
    private final Map<QueryParams, View> views = new HashMap();

    public SyncPoint(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public boolean isEmpty() {
        return this.views.isEmpty();
    }

    private List<DataEvent> applyOperationToView(View view, Operation operation, WriteTreeRef writes, Node optCompleteServerCache) {
        View.OperationResult result = view.applyOperation(operation, writes, optCompleteServerCache);
        if (!view.getQuery().loadsAllData()) {
            Set<ChildKey> removed = new HashSet<>();
            Set<ChildKey> added = new HashSet<>();
            for (Change change : result.changes) {
                Event.EventType type = change.getEventType();
                if (type == Event.EventType.CHILD_ADDED) {
                    added.add(change.getChildKey());
                } else if (type == Event.EventType.CHILD_REMOVED) {
                    removed.add(change.getChildKey());
                }
            }
            if (!added.isEmpty() || !removed.isEmpty()) {
                this.persistenceManager.updateTrackedQueryKeys(view.getQuery(), added, removed);
            }
        }
        return result.events;
    }

    public List<DataEvent> applyOperation(Operation operation, WriteTreeRef writesCache, Node optCompleteServerCache) {
        QueryParams queryParams = operation.getSource().getQueryParams();
        if (queryParams != null) {
            View view = this.views.get(queryParams);
            Utilities.hardAssert(view != null);
            return applyOperationToView(view, operation, writesCache, optCompleteServerCache);
        }
        List<DataEvent> events = new ArrayList<>();
        for (Map.Entry<QueryParams, View> entry : this.views.entrySet()) {
            events.addAll(applyOperationToView(entry.getValue(), operation, writesCache, optCompleteServerCache));
        }
        return events;
    }

    public View getView(QuerySpec query, WriteTreeRef writesCache, CacheNode serverCache) {
        boolean eventCacheComplete;
        View view = this.views.get(query.getParams());
        if (view == null) {
            Node eventCache = writesCache.calcCompleteEventCache(serverCache.isFullyInitialized() ? serverCache.getNode() : null);
            if (eventCache != null) {
                eventCacheComplete = true;
            } else {
                eventCache = writesCache.calcCompleteEventChildren(serverCache.getNode() != null ? serverCache.getNode() : EmptyNode.Empty());
                eventCacheComplete = false;
            }
            IndexedNode indexed = IndexedNode.from(eventCache, query.getIndex());
            ViewCache viewCache = new ViewCache(new CacheNode(indexed, eventCacheComplete, false), serverCache);
            return new View(query, viewCache);
        }
        return view;
    }

    public List<DataEvent> addEventRegistration(EventRegistration eventRegistration, WriteTreeRef writesCache, CacheNode serverCache) {
        QuerySpec query = eventRegistration.getQuerySpec();
        View view = getView(query, writesCache, serverCache);
        if (!query.loadsAllData()) {
            Set<ChildKey> allChildren = new HashSet<>();
            for (NamedNode node : view.getEventCache()) {
                allChildren.add(node.getName());
            }
            this.persistenceManager.setTrackedQueryKeys(query, allChildren);
        }
        if (!this.views.containsKey(query.getParams())) {
            this.views.put(query.getParams(), view);
        }
        this.views.put(query.getParams(), view);
        view.addEventRegistration(eventRegistration);
        return view.getInitialEvents(eventRegistration);
    }

    public Pair<List<QuerySpec>, List<Event>> removeEventRegistration(QuerySpec query, EventRegistration eventRegistration, DatabaseError cancelError) {
        List<QuerySpec> removed = new ArrayList<>();
        List<Event> cancelEvents = new ArrayList<>();
        boolean hadCompleteView = hasCompleteView();
        if (query.isDefault()) {
            Iterator<Map.Entry<QueryParams, View>> iterator = this.views.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<QueryParams, View> entry = iterator.next();
                View view = entry.getValue();
                cancelEvents.addAll(view.removeEventRegistration(eventRegistration, cancelError));
                if (view.isEmpty()) {
                    iterator.remove();
                    if (!view.getQuery().loadsAllData()) {
                        removed.add(view.getQuery());
                    }
                }
            }
        } else {
            View view2 = this.views.get(query.getParams());
            if (view2 != null) {
                cancelEvents.addAll(view2.removeEventRegistration(eventRegistration, cancelError));
                if (view2.isEmpty()) {
                    this.views.remove(query.getParams());
                    if (!view2.getQuery().loadsAllData()) {
                        removed.add(view2.getQuery());
                    }
                }
            }
        }
        if (hadCompleteView && !hasCompleteView()) {
            removed.add(QuerySpec.defaultQueryAtPath(query.getPath()));
        }
        return new Pair<>(removed, cancelEvents);
    }

    public List<View> getQueryViews() {
        List<View> views = new ArrayList<>();
        for (Map.Entry<QueryParams, View> entry : this.views.entrySet()) {
            View view = entry.getValue();
            if (!view.getQuery().loadsAllData()) {
                views.add(view);
            }
        }
        return views;
    }

    public Node getCompleteServerCache(Path path) {
        for (View view : this.views.values()) {
            if (view.getCompleteServerCache(path) != null) {
                return view.getCompleteServerCache(path);
            }
        }
        return null;
    }

    public View viewForQuery(QuerySpec query) {
        if (query.loadsAllData()) {
            return getCompleteView();
        }
        return this.views.get(query.getParams());
    }

    public boolean viewExistsForQuery(QuerySpec query) {
        return viewForQuery(query) != null;
    }

    public boolean hasCompleteView() {
        return getCompleteView() != null;
    }

    public View getCompleteView() {
        for (Map.Entry<QueryParams, View> entry : this.views.entrySet()) {
            View view = entry.getValue();
            if (view.getQuery().loadsAllData()) {
                return view;
            }
        }
        return null;
    }

    Map<QueryParams, View> getViews() {
        return this.views;
    }
}
