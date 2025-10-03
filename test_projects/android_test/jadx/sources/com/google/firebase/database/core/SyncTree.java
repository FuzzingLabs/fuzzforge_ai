package com.google.firebase.database.core;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.InternalHelpers;
import com.google.firebase.database.Query;
import com.google.firebase.database.collection.LLRBNode;
import com.google.firebase.database.connection.CompoundHash;
import com.google.firebase.database.connection.ListenHashProvider;
import com.google.firebase.database.core.operation.AckUserWrite;
import com.google.firebase.database.core.operation.ListenComplete;
import com.google.firebase.database.core.operation.Merge;
import com.google.firebase.database.core.operation.Operation;
import com.google.firebase.database.core.operation.OperationSource;
import com.google.firebase.database.core.operation.Overwrite;
import com.google.firebase.database.core.persistence.PersistenceManager;
import com.google.firebase.database.core.utilities.Clock;
import com.google.firebase.database.core.utilities.ImmutableTree;
import com.google.firebase.database.core.utilities.NodeSizeEstimator;
import com.google.firebase.database.core.utilities.Pair;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.core.view.CacheNode;
import com.google.firebase.database.core.view.Change;
import com.google.firebase.database.core.view.DataEvent;
import com.google.firebase.database.core.view.Event;
import com.google.firebase.database.core.view.QuerySpec;
import com.google.firebase.database.core.view.View;
import com.google.firebase.database.logging.LogWrapper;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.EmptyNode;
import com.google.firebase.database.snapshot.IndexedNode;
import com.google.firebase.database.snapshot.NamedNode;
import com.google.firebase.database.snapshot.Node;
import com.google.firebase.database.snapshot.RangeMerge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/* loaded from: classes.dex */
public class SyncTree {
    private static final long SIZE_THRESHOLD_FOR_COMPOUND_HASH = 1024;
    private final ListenProvider listenProvider;
    private final LogWrapper logger;
    private final PersistenceManager persistenceManager;
    private long nextQueryTag = 1;
    private ImmutableTree<SyncPoint> syncPointTree = ImmutableTree.emptyInstance();
    private final WriteTree pendingWriteTree = new WriteTree();
    private final Map<Tag, QuerySpec> tagToQueryMap = new HashMap();
    private final Map<QuerySpec, Tag> queryToTagMap = new HashMap();
    private final Set<QuerySpec> keepSyncedQueries = new HashSet();

    public interface CompletionListener {
        List<? extends Event> onListenComplete(DatabaseError databaseError);
    }

    public interface ListenProvider {
        void startListening(QuerySpec querySpec, Tag tag, ListenHashProvider listenHashProvider, CompletionListener completionListener);

        void stopListening(QuerySpec querySpec, Tag tag);
    }

    private class ListenContainer implements ListenHashProvider, CompletionListener {
        private final Tag tag;
        private final View view;

        public ListenContainer(View view) {
            this.view = view;
            this.tag = SyncTree.this.tagForQuery(view.getQuery());
        }

        @Override // com.google.firebase.database.connection.ListenHashProvider
        public CompoundHash getCompoundHash() {
            com.google.firebase.database.snapshot.CompoundHash hash = com.google.firebase.database.snapshot.CompoundHash.fromNode(this.view.getServerCache());
            List<Path> pathPosts = hash.getPosts();
            List<List<String>> posts = new ArrayList<>(pathPosts.size());
            for (Path path : pathPosts) {
                posts.add(path.asList());
            }
            return new CompoundHash(posts, hash.getHashes());
        }

        @Override // com.google.firebase.database.connection.ListenHashProvider
        public String getSimpleHash() {
            return this.view.getServerCache().getHash();
        }

        @Override // com.google.firebase.database.connection.ListenHashProvider
        public boolean shouldIncludeCompoundHash() {
            return NodeSizeEstimator.estimateSerializedNodeSize(this.view.getServerCache()) > 1024;
        }

        @Override // com.google.firebase.database.core.SyncTree.CompletionListener
        public List<? extends Event> onListenComplete(DatabaseError error) {
            if (error != null) {
                SyncTree.this.logger.warn("Listen at " + this.view.getQuery().getPath() + " failed: " + error.toString());
                return SyncTree.this.removeAllEventRegistrations(this.view.getQuery(), error);
            }
            QuerySpec query = this.view.getQuery();
            Tag tag = this.tag;
            if (tag != null) {
                return SyncTree.this.applyTaggedListenComplete(tag);
            }
            return SyncTree.this.applyListenComplete(query.getPath());
        }
    }

    public SyncTree(Context context, PersistenceManager persistenceManager, ListenProvider listenProvider) {
        this.listenProvider = listenProvider;
        this.persistenceManager = persistenceManager;
        this.logger = context.getLogger("SyncTree");
    }

    public boolean isEmpty() {
        return this.syncPointTree.isEmpty();
    }

    public List<? extends Event> applyUserOverwrite(final Path path, final Node newDataUnresolved, final Node newData, final long writeId, final boolean visible, final boolean persist) {
        Utilities.hardAssert(visible || !persist, "We shouldn't be persisting non-visible writes.");
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() { // from class: com.google.firebase.database.core.SyncTree.1
            @Override // java.util.concurrent.Callable
            public List<? extends Event> call() {
                if (persist) {
                    SyncTree.this.persistenceManager.saveUserOverwrite(path, newDataUnresolved, writeId);
                }
                SyncTree.this.pendingWriteTree.addOverwrite(path, newData, Long.valueOf(writeId), visible);
                if (visible) {
                    return SyncTree.this.applyOperationToSyncPoints(new Overwrite(OperationSource.USER, path, newData));
                }
                return Collections.emptyList();
            }
        });
    }

    public List<? extends Event> applyUserMerge(final Path path, final CompoundWrite unresolvedChildren, final CompoundWrite children, final long writeId, final boolean persist) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() { // from class: com.google.firebase.database.core.SyncTree.2
            @Override // java.util.concurrent.Callable
            public List<? extends Event> call() throws Exception {
                if (persist) {
                    SyncTree.this.persistenceManager.saveUserMerge(path, unresolvedChildren, writeId);
                }
                SyncTree.this.pendingWriteTree.addMerge(path, children, Long.valueOf(writeId));
                return SyncTree.this.applyOperationToSyncPoints(new Merge(OperationSource.USER, path, children));
            }
        });
    }

    public List<? extends Event> ackUserWrite(final long writeId, final boolean revert, final boolean persist, final Clock serverClock) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() { // from class: com.google.firebase.database.core.SyncTree.3
            @Override // java.util.concurrent.Callable
            public List<? extends Event> call() {
                if (persist) {
                    SyncTree.this.persistenceManager.removeUserWrite(writeId);
                }
                UserWriteRecord write = SyncTree.this.pendingWriteTree.getWrite(writeId);
                boolean needToReevaluate = SyncTree.this.pendingWriteTree.removeWrite(writeId);
                if (write.isVisible() && !revert) {
                    Map<String, Object> serverValues = ServerValues.generateServerValues(serverClock);
                    if (write.isOverwrite()) {
                        Node resolvedNode = ServerValues.resolveDeferredValueSnapshot(write.getOverwrite(), SyncTree.this, write.getPath(), serverValues);
                        SyncTree.this.persistenceManager.applyUserWriteToServerCache(write.getPath(), resolvedNode);
                    } else {
                        CompoundWrite resolvedMerge = ServerValues.resolveDeferredValueMerge(write.getMerge(), SyncTree.this, write.getPath(), serverValues);
                        SyncTree.this.persistenceManager.applyUserWriteToServerCache(write.getPath(), resolvedMerge);
                    }
                }
                if (!needToReevaluate) {
                    return Collections.emptyList();
                }
                ImmutableTree<Boolean> affectedTree = ImmutableTree.emptyInstance();
                if (write.isOverwrite()) {
                    affectedTree = affectedTree.set(Path.getEmptyPath(), true);
                } else {
                    Iterator<Map.Entry<Path, Node>> it = write.getMerge().iterator();
                    while (it.hasNext()) {
                        Map.Entry<Path, Node> entry = it.next();
                        affectedTree = affectedTree.set(entry.getKey(), true);
                    }
                }
                return SyncTree.this.applyOperationToSyncPoints(new AckUserWrite(write.getPath(), affectedTree, revert));
            }
        });
    }

    public List<? extends Event> removeAllWrites() {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() { // from class: com.google.firebase.database.core.SyncTree.4
            @Override // java.util.concurrent.Callable
            public List<? extends Event> call() throws Exception {
                SyncTree.this.persistenceManager.removeAllUserWrites();
                List<UserWriteRecord> purgedWrites = SyncTree.this.pendingWriteTree.purgeAllWrites();
                if (purgedWrites.isEmpty()) {
                    return Collections.emptyList();
                }
                ImmutableTree<Boolean> affectedTree = new ImmutableTree<>(true);
                return SyncTree.this.applyOperationToSyncPoints(new AckUserWrite(Path.getEmptyPath(), affectedTree, true));
            }
        });
    }

    public List<? extends Event> applyServerOverwrite(final Path path, final Node newData) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() { // from class: com.google.firebase.database.core.SyncTree.5
            @Override // java.util.concurrent.Callable
            public List<? extends Event> call() {
                SyncTree.this.persistenceManager.updateServerCache(QuerySpec.defaultQueryAtPath(path), newData);
                return SyncTree.this.applyOperationToSyncPoints(new Overwrite(OperationSource.SERVER, path, newData));
            }
        });
    }

    public List<? extends Event> applyServerMerge(final Path path, final Map<Path, Node> changedChildren) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() { // from class: com.google.firebase.database.core.SyncTree.6
            @Override // java.util.concurrent.Callable
            public List<? extends Event> call() {
                CompoundWrite merge = CompoundWrite.fromPathMerge(changedChildren);
                SyncTree.this.persistenceManager.updateServerCache(path, merge);
                return SyncTree.this.applyOperationToSyncPoints(new Merge(OperationSource.SERVER, path, merge));
            }
        });
    }

    public List<? extends Event> applyServerRangeMerges(Path path, List<RangeMerge> rangeMerges) {
        SyncPoint syncPoint = this.syncPointTree.get(path);
        if (syncPoint == null) {
            return Collections.emptyList();
        }
        View view = syncPoint.getCompleteView();
        if (view != null) {
            Node serverNode = view.getServerCache();
            for (RangeMerge merge : rangeMerges) {
                serverNode = merge.applyTo(serverNode);
            }
            return applyServerOverwrite(path, serverNode);
        }
        return Collections.emptyList();
    }

    public List<? extends Event> applyTaggedRangeMerges(Path path, List<RangeMerge> rangeMerges, Tag tag) {
        QuerySpec query = queryForTag(tag);
        if (query != null) {
            Utilities.hardAssert(path.equals(query.getPath()));
            SyncPoint syncPoint = this.syncPointTree.get(query.getPath());
            Utilities.hardAssert(syncPoint != null, "Missing sync point for query tag that we're tracking");
            View view = syncPoint.viewForQuery(query);
            Utilities.hardAssert(view != null, "Missing view for query tag that we're tracking");
            Node serverNode = view.getServerCache();
            for (RangeMerge merge : rangeMerges) {
                serverNode = merge.applyTo(serverNode);
            }
            return applyTaggedQueryOverwrite(path, serverNode, tag);
        }
        return Collections.emptyList();
    }

    public List<? extends Event> applyListenComplete(final Path path) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() { // from class: com.google.firebase.database.core.SyncTree.7
            @Override // java.util.concurrent.Callable
            public List<? extends Event> call() {
                SyncTree.this.persistenceManager.setQueryComplete(QuerySpec.defaultQueryAtPath(path));
                return SyncTree.this.applyOperationToSyncPoints(new ListenComplete(OperationSource.SERVER, path));
            }
        });
    }

    public List<? extends Event> applyTaggedListenComplete(final Tag tag) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() { // from class: com.google.firebase.database.core.SyncTree.8
            @Override // java.util.concurrent.Callable
            public List<? extends Event> call() {
                QuerySpec query = SyncTree.this.queryForTag(tag);
                if (query != null) {
                    SyncTree.this.persistenceManager.setQueryComplete(query);
                    Operation op = new ListenComplete(OperationSource.forServerTaggedQuery(query.getParams()), Path.getEmptyPath());
                    return SyncTree.this.applyTaggedOperation(query, op);
                }
                return Collections.emptyList();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public List<? extends Event> applyTaggedOperation(QuerySpec query, Operation operation) {
        Path queryPath = query.getPath();
        SyncPoint syncPoint = this.syncPointTree.get(queryPath);
        Utilities.hardAssert(syncPoint != null, "Missing sync point for query tag that we're tracking");
        WriteTreeRef writesCache = this.pendingWriteTree.childWrites(queryPath);
        return syncPoint.applyOperation(operation, writesCache, null);
    }

    public List<? extends Event> applyTaggedQueryOverwrite(final Path path, final Node snap, final Tag tag) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() { // from class: com.google.firebase.database.core.SyncTree.9
            @Override // java.util.concurrent.Callable
            public List<? extends Event> call() {
                QuerySpec query = SyncTree.this.queryForTag(tag);
                if (query != null) {
                    Path relativePath = Path.getRelative(query.getPath(), path);
                    QuerySpec queryToOverwrite = relativePath.isEmpty() ? query : QuerySpec.defaultQueryAtPath(path);
                    SyncTree.this.persistenceManager.updateServerCache(queryToOverwrite, snap);
                    Operation op = new Overwrite(OperationSource.forServerTaggedQuery(query.getParams()), relativePath, snap);
                    return SyncTree.this.applyTaggedOperation(query, op);
                }
                return Collections.emptyList();
            }
        });
    }

    public List<? extends Event> applyTaggedQueryMerge(final Path path, final Map<Path, Node> changedChildren, final Tag tag) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() { // from class: com.google.firebase.database.core.SyncTree.10
            @Override // java.util.concurrent.Callable
            public List<? extends Event> call() {
                QuerySpec query = SyncTree.this.queryForTag(tag);
                if (query != null) {
                    Path relativePath = Path.getRelative(query.getPath(), path);
                    CompoundWrite merge = CompoundWrite.fromPathMerge(changedChildren);
                    SyncTree.this.persistenceManager.updateServerCache(path, merge);
                    Operation op = new Merge(OperationSource.forServerTaggedQuery(query.getParams()), relativePath, merge);
                    return SyncTree.this.applyTaggedOperation(query, op);
                }
                return Collections.emptyList();
            }
        });
    }

    public void setQueryActive(final QuerySpec query) {
        this.persistenceManager.runInTransaction(new Callable<Void>() { // from class: com.google.firebase.database.core.SyncTree.11
            @Override // java.util.concurrent.Callable
            public Void call() {
                SyncTree.this.persistenceManager.setQueryActive(query);
                return null;
            }
        });
    }

    public void setQueryInactive(final QuerySpec query) {
        this.persistenceManager.runInTransaction(new Callable<Void>() { // from class: com.google.firebase.database.core.SyncTree.12
            @Override // java.util.concurrent.Callable
            public Void call() {
                SyncTree.this.persistenceManager.setQueryInactive(query);
                return null;
            }
        });
    }

    public DataSnapshot persistenceServerCache(Query query) {
        return InternalHelpers.createDataSnapshot(query.getRef(), this.persistenceManager.serverCache(query.getSpec()).getIndexedNode());
    }

    public Node getServerValue(final QuerySpec query) {
        return (Node) this.persistenceManager.runInTransaction(new Callable() { // from class: com.google.firebase.database.core.SyncTree$$ExternalSyntheticLambda0
            @Override // java.util.concurrent.Callable
            public final Object call() {
                return SyncTree.this.m233x4ba5d756(query);
            }
        });
    }

    /* renamed from: lambda$getServerValue$0$com-google-firebase-database-core-SyncTree */
    public /* synthetic */ Node m233x4ba5d756(QuerySpec query) throws Exception {
        Node completeServerCache;
        Node completeServerCache2;
        Path path = query.getPath();
        Node serverCacheNode = null;
        boolean foundAncestorDefaultView = false;
        ImmutableTree<SyncPoint> tree = this.syncPointTree;
        Path currentPath = path;
        while (true) {
            if (tree.isEmpty()) {
                break;
            }
            SyncPoint currentSyncPoint = tree.getValue();
            if (currentSyncPoint != null) {
                if (serverCacheNode != null) {
                    completeServerCache2 = serverCacheNode;
                } else {
                    completeServerCache2 = currentSyncPoint.getCompleteServerCache(currentPath);
                }
                serverCacheNode = completeServerCache2;
                if (!foundAncestorDefaultView && !currentSyncPoint.hasCompleteView()) {
                    r6 = false;
                }
                foundAncestorDefaultView = r6;
            }
            ChildKey front = currentPath.isEmpty() ? ChildKey.fromString("") : currentPath.getFront();
            tree = tree.getChild(front);
            currentPath = currentPath.popFront();
        }
        SyncPoint syncPoint = this.syncPointTree.get(path);
        if (syncPoint == null) {
            syncPoint = new SyncPoint(this.persistenceManager);
            this.syncPointTree = this.syncPointTree.set(path, syncPoint);
        } else {
            if (serverCacheNode != null) {
                completeServerCache = serverCacheNode;
            } else {
                completeServerCache = syncPoint.getCompleteServerCache(Path.getEmptyPath());
            }
            serverCacheNode = completeServerCache;
        }
        CacheNode serverCache = new CacheNode(IndexedNode.from(serverCacheNode != null ? serverCacheNode : EmptyNode.Empty(), query.getIndex()), serverCacheNode != null, false);
        WriteTreeRef writesCache = this.pendingWriteTree.childWrites(path);
        View view = syncPoint.getView(query, writesCache, serverCache);
        return view.getCompleteNode();
    }

    public List<? extends Event> addEventRegistration(final EventRegistration eventRegistration) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<? extends Event>>() { // from class: com.google.firebase.database.core.SyncTree.13
            @Override // java.util.concurrent.Callable
            public List<? extends Event> call() {
                Node completeServerCache;
                CacheNode persistentServerCache;
                Node completeCache;
                Node completeServerCache2;
                QuerySpec query = eventRegistration.getQuerySpec();
                Path path = query.getPath();
                Node serverCacheNode = null;
                boolean foundAncestorDefaultView = false;
                ImmutableTree<SyncPoint> tree = SyncTree.this.syncPointTree;
                Path currentPath = path;
                while (true) {
                    boolean z = true;
                    if (tree.isEmpty()) {
                        break;
                    }
                    SyncPoint currentSyncPoint = tree.getValue();
                    if (currentSyncPoint != null) {
                        if (serverCacheNode != null) {
                            completeServerCache2 = serverCacheNode;
                        } else {
                            completeServerCache2 = currentSyncPoint.getCompleteServerCache(currentPath);
                        }
                        serverCacheNode = completeServerCache2;
                        if (!foundAncestorDefaultView && !currentSyncPoint.hasCompleteView()) {
                            z = false;
                        }
                        foundAncestorDefaultView = z;
                    }
                    ChildKey front = currentPath.isEmpty() ? ChildKey.fromString("") : currentPath.getFront();
                    tree = tree.getChild(front);
                    currentPath = currentPath.popFront();
                }
                SyncPoint syncPoint = (SyncPoint) SyncTree.this.syncPointTree.get(path);
                if (syncPoint == null) {
                    syncPoint = new SyncPoint(SyncTree.this.persistenceManager);
                    SyncTree syncTree = SyncTree.this;
                    syncTree.syncPointTree = syncTree.syncPointTree.set(path, syncPoint);
                } else {
                    foundAncestorDefaultView = foundAncestorDefaultView || syncPoint.hasCompleteView();
                    if (serverCacheNode != null) {
                        completeServerCache = serverCacheNode;
                    } else {
                        completeServerCache = syncPoint.getCompleteServerCache(Path.getEmptyPath());
                    }
                    serverCacheNode = completeServerCache;
                }
                SyncTree.this.persistenceManager.setQueryActive(query);
                if (serverCacheNode == null) {
                    persistentServerCache = SyncTree.this.persistenceManager.serverCache(query);
                    if (!persistentServerCache.isFullyInitialized()) {
                        Node serverCacheNode2 = EmptyNode.Empty();
                        ImmutableTree<SyncPoint> subtree = SyncTree.this.syncPointTree.subtree(path);
                        Iterator<Map.Entry<ChildKey, ImmutableTree<SyncPoint>>> it = subtree.getChildren().iterator();
                        while (it.hasNext()) {
                            Map.Entry<ChildKey, ImmutableTree<SyncPoint>> child = it.next();
                            SyncPoint childSyncPoint = child.getValue().getValue();
                            if (childSyncPoint != null && (completeCache = childSyncPoint.getCompleteServerCache(Path.getEmptyPath())) != null) {
                                serverCacheNode2 = serverCacheNode2.updateImmediateChild(child.getKey(), completeCache);
                            }
                        }
                        for (NamedNode child2 : persistentServerCache.getNode()) {
                            if (!serverCacheNode2.hasChild(child2.getName())) {
                                serverCacheNode2 = serverCacheNode2.updateImmediateChild(child2.getName(), child2.getNode());
                            }
                        }
                        persistentServerCache = new CacheNode(IndexedNode.from(serverCacheNode2, query.getIndex()), false, false);
                    }
                } else {
                    persistentServerCache = new CacheNode(IndexedNode.from(serverCacheNode, query.getIndex()), true, false);
                }
                boolean viewAlreadyExists = syncPoint.viewExistsForQuery(query);
                if (!viewAlreadyExists && !query.loadsAllData()) {
                    Utilities.hardAssert(true ^ SyncTree.this.queryToTagMap.containsKey(query), "View does not exist but we have a tag");
                    Tag tag = SyncTree.this.getNextQueryTag();
                    SyncTree.this.queryToTagMap.put(query, tag);
                    SyncTree.this.tagToQueryMap.put(tag, query);
                }
                WriteTreeRef writesCache = SyncTree.this.pendingWriteTree.childWrites(path);
                List<? extends Event> events = syncPoint.addEventRegistration(eventRegistration, writesCache, persistentServerCache);
                if (!viewAlreadyExists && !foundAncestorDefaultView) {
                    View view = syncPoint.viewForQuery(query);
                    SyncTree.this.setupListener(query, view);
                }
                return events;
            }
        });
    }

    public List<Event> removeEventRegistration(EventRegistration eventRegistration) {
        return removeEventRegistration(eventRegistration.getQuerySpec(), eventRegistration, null);
    }

    public List<Event> removeAllEventRegistrations(QuerySpec query, DatabaseError error) {
        return removeEventRegistration(query, null, error);
    }

    private List<Event> removeEventRegistration(final QuerySpec query, final EventRegistration eventRegistration, final DatabaseError cancelError) {
        return (List) this.persistenceManager.runInTransaction(new Callable<List<Event>>() { // from class: com.google.firebase.database.core.SyncTree.14
            @Override // java.util.concurrent.Callable
            public List<Event> call() {
                Path path = query.getPath();
                SyncPoint maybeSyncPoint = (SyncPoint) SyncTree.this.syncPointTree.get(path);
                List<Event> cancelEvents = new ArrayList<>();
                if (maybeSyncPoint != null && (query.isDefault() || maybeSyncPoint.viewExistsForQuery(query))) {
                    Pair<List<QuerySpec>, List<Event>> removedAndEvents = maybeSyncPoint.removeEventRegistration(query, eventRegistration, cancelError);
                    if (maybeSyncPoint.isEmpty()) {
                        SyncTree syncTree = SyncTree.this;
                        syncTree.syncPointTree = syncTree.syncPointTree.remove(path);
                    }
                    List<QuerySpec> removed = removedAndEvents.getFirst();
                    cancelEvents = removedAndEvents.getSecond();
                    boolean removingDefault = false;
                    for (QuerySpec queryRemoved : removed) {
                        SyncTree.this.persistenceManager.setQueryInactive(query);
                        removingDefault = removingDefault || queryRemoved.loadsAllData();
                    }
                    ImmutableTree<SyncPoint> currentTree = SyncTree.this.syncPointTree;
                    boolean covered = currentTree.getValue() != null && currentTree.getValue().hasCompleteView();
                    Iterator<ChildKey> it = path.iterator();
                    while (it.hasNext()) {
                        ChildKey component = it.next();
                        currentTree = currentTree.getChild(component);
                        covered = covered || (currentTree.getValue() != null && currentTree.getValue().hasCompleteView());
                        if (covered || currentTree.isEmpty()) {
                            break;
                        }
                    }
                    if (removingDefault && !covered) {
                        ImmutableTree<SyncPoint> subtree = SyncTree.this.syncPointTree.subtree(path);
                        if (!subtree.isEmpty()) {
                            List<View> newViews = SyncTree.this.collectDistinctViewsForSubTree(subtree);
                            for (View view : newViews) {
                                ListenContainer container = SyncTree.this.new ListenContainer(view);
                                QuerySpec newQuery = view.getQuery();
                                SyncTree.this.listenProvider.startListening(SyncTree.this.queryForListening(newQuery), container.tag, container, container);
                                path = path;
                                maybeSyncPoint = maybeSyncPoint;
                            }
                        }
                    }
                    if (!covered && !removed.isEmpty() && cancelError == null) {
                        if (removingDefault) {
                            SyncTree.this.listenProvider.stopListening(SyncTree.this.queryForListening(query), null);
                        } else {
                            for (QuerySpec queryToRemove : removed) {
                                Tag tag = SyncTree.this.tagForQuery(queryToRemove);
                                Utilities.hardAssert(tag != null);
                                SyncTree.this.listenProvider.stopListening(SyncTree.this.queryForListening(queryToRemove), tag);
                            }
                        }
                    }
                    SyncTree.this.removeTags(removed);
                }
                return cancelEvents;
            }
        });
    }

    private static class KeepSyncedEventRegistration extends EventRegistration {
        private QuerySpec spec;

        public KeepSyncedEventRegistration(QuerySpec spec) {
            this.spec = spec;
        }

        @Override // com.google.firebase.database.core.EventRegistration
        public boolean respondsTo(Event.EventType eventType) {
            return false;
        }

        @Override // com.google.firebase.database.core.EventRegistration
        public DataEvent createEvent(Change change, QuerySpec query) {
            return null;
        }

        @Override // com.google.firebase.database.core.EventRegistration
        public void fireEvent(DataEvent dataEvent) {
        }

        @Override // com.google.firebase.database.core.EventRegistration
        public void fireCancelEvent(DatabaseError error) {
        }

        @Override // com.google.firebase.database.core.EventRegistration
        public EventRegistration clone(QuerySpec newQuery) {
            return new KeepSyncedEventRegistration(newQuery);
        }

        @Override // com.google.firebase.database.core.EventRegistration
        public boolean isSameListener(EventRegistration other) {
            return other instanceof KeepSyncedEventRegistration;
        }

        @Override // com.google.firebase.database.core.EventRegistration
        public QuerySpec getQuerySpec() {
            return this.spec;
        }

        public boolean equals(Object other) {
            return (other instanceof KeepSyncedEventRegistration) && ((KeepSyncedEventRegistration) other).spec.equals(this.spec);
        }

        public int hashCode() {
            return this.spec.hashCode();
        }
    }

    public void keepSynced(QuerySpec query, boolean keep) {
        if (keep && !this.keepSyncedQueries.contains(query)) {
            addEventRegistration(new KeepSyncedEventRegistration(query));
            this.keepSyncedQueries.add(query);
        } else if (!keep && this.keepSyncedQueries.contains(query)) {
            removeEventRegistration(new KeepSyncedEventRegistration(query));
            this.keepSyncedQueries.remove(query);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public List<View> collectDistinctViewsForSubTree(ImmutableTree<SyncPoint> subtree) {
        ArrayList<View> accumulator = new ArrayList<>();
        collectDistinctViewsForSubTree(subtree, accumulator);
        return accumulator;
    }

    private void collectDistinctViewsForSubTree(ImmutableTree<SyncPoint> subtree, List<View> accumulator) {
        SyncPoint maybeSyncPoint = subtree.getValue();
        if (maybeSyncPoint != null && maybeSyncPoint.hasCompleteView()) {
            accumulator.add(maybeSyncPoint.getCompleteView());
            return;
        }
        if (maybeSyncPoint != null) {
            accumulator.addAll(maybeSyncPoint.getQueryViews());
        }
        Iterator<Map.Entry<ChildKey, ImmutableTree<SyncPoint>>> it = subtree.getChildren().iterator();
        while (it.hasNext()) {
            Map.Entry<ChildKey, ImmutableTree<SyncPoint>> entry = it.next();
            collectDistinctViewsForSubTree(entry.getValue(), accumulator);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeTags(List<QuerySpec> queries) {
        for (QuerySpec removedQuery : queries) {
            if (!removedQuery.loadsAllData()) {
                Tag tag = tagForQuery(removedQuery);
                Utilities.hardAssert(tag != null);
                this.queryToTagMap.remove(removedQuery);
                this.tagToQueryMap.remove(tag);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public QuerySpec queryForListening(QuerySpec query) {
        if (query.loadsAllData() && !query.isDefault()) {
            return QuerySpec.defaultQueryAtPath(query.getPath());
        }
        return query;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setupListener(QuerySpec query, View view) {
        Path path = query.getPath();
        Tag tag = tagForQuery(query);
        ListenContainer container = new ListenContainer(view);
        this.listenProvider.startListening(queryForListening(query), tag, container, container);
        ImmutableTree<SyncPoint> subtree = this.syncPointTree.subtree(path);
        if (tag != null) {
            Utilities.hardAssert(!subtree.getValue().hasCompleteView(), "If we're adding a query, it shouldn't be shadowed");
        } else {
            subtree.foreach(new ImmutableTree.TreeVisitor<SyncPoint, Void>() { // from class: com.google.firebase.database.core.SyncTree.15
                @Override // com.google.firebase.database.core.utilities.ImmutableTree.TreeVisitor
                public Void onNodeValue(Path relativePath, SyncPoint maybeChildSyncPoint, Void accum) {
                    if (!relativePath.isEmpty() && maybeChildSyncPoint.hasCompleteView()) {
                        QuerySpec query2 = maybeChildSyncPoint.getCompleteView().getQuery();
                        SyncTree.this.listenProvider.stopListening(SyncTree.this.queryForListening(query2), SyncTree.this.tagForQuery(query2));
                        return null;
                    }
                    for (View syncPointView : maybeChildSyncPoint.getQueryViews()) {
                        QuerySpec childQuery = syncPointView.getQuery();
                        SyncTree.this.listenProvider.stopListening(SyncTree.this.queryForListening(childQuery), SyncTree.this.tagForQuery(childQuery));
                    }
                    return null;
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public QuerySpec queryForTag(Tag tag) {
        return this.tagToQueryMap.get(tag);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Tag tagForQuery(QuerySpec query) {
        return this.queryToTagMap.get(query);
    }

    public Node calcCompleteEventCacheFromRoot(Path path, List<Long> writeIdsToExclude) {
        SyncPoint currentSyncPoint = this.syncPointTree.getValue();
        Node serverCache = null;
        if (currentSyncPoint != null) {
            serverCache = currentSyncPoint.getCompleteServerCache(Path.getEmptyPath());
        }
        if (serverCache != null) {
            return this.pendingWriteTree.calcCompleteEventCache(path, serverCache, writeIdsToExclude, true);
        }
        return calcCompleteEventCache(path, writeIdsToExclude);
    }

    public Node calcCompleteEventCache(Path path, List<Long> writeIdsToExclude) {
        ImmutableTree<SyncPoint> tree = this.syncPointTree;
        tree.getValue();
        Node serverCache = null;
        Path pathToFollow = path;
        Path pathSoFar = Path.getEmptyPath();
        do {
            ChildKey front = pathToFollow.getFront();
            pathToFollow = pathToFollow.popFront();
            pathSoFar = pathSoFar.child(front);
            Path relativePath = Path.getRelative(pathSoFar, path);
            tree = front != null ? tree.getChild(front) : ImmutableTree.emptyInstance();
            SyncPoint currentSyncPoint = tree.getValue();
            if (currentSyncPoint != null) {
                serverCache = currentSyncPoint.getCompleteServerCache(relativePath);
            }
            if (pathToFollow.isEmpty()) {
                break;
            }
        } while (serverCache == null);
        return this.pendingWriteTree.calcCompleteEventCache(path, serverCache, writeIdsToExclude, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Tag getNextQueryTag() {
        long j = this.nextQueryTag;
        this.nextQueryTag = 1 + j;
        return new Tag(j);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public List<Event> applyOperationToSyncPoints(Operation operation) {
        return applyOperationHelper(operation, this.syncPointTree, null, this.pendingWriteTree.childWrites(Path.getEmptyPath()));
    }

    private List<Event> applyOperationHelper(Operation operation, ImmutableTree<SyncPoint> syncPointTree, Node serverCache, WriteTreeRef writesCache) {
        if (operation.getPath().isEmpty()) {
            return applyOperationDescendantsHelper(operation, syncPointTree, serverCache, writesCache);
        }
        SyncPoint syncPoint = syncPointTree.getValue();
        if (serverCache == null && syncPoint != null) {
            serverCache = syncPoint.getCompleteServerCache(Path.getEmptyPath());
        }
        List<Event> events = new ArrayList<>();
        ChildKey childKey = operation.getPath().getFront();
        Operation childOperation = operation.operationForChild(childKey);
        ImmutableTree<SyncPoint> childTree = syncPointTree.getChildren().get(childKey);
        if (childTree != null && childOperation != null) {
            Node childServerCache = serverCache != null ? serverCache.getImmediateChild(childKey) : null;
            WriteTreeRef childWritesCache = writesCache.child(childKey);
            events.addAll(applyOperationHelper(childOperation, childTree, childServerCache, childWritesCache));
        }
        if (syncPoint != null) {
            events.addAll(syncPoint.applyOperation(operation, writesCache, serverCache));
        }
        return events;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public List<Event> applyOperationDescendantsHelper(final Operation operation, ImmutableTree<SyncPoint> syncPointTree, Node serverCache, final WriteTreeRef writesCache) {
        Node resolvedServerCache;
        SyncPoint syncPoint = syncPointTree.getValue();
        if (serverCache == null && syncPoint != null) {
            resolvedServerCache = syncPoint.getCompleteServerCache(Path.getEmptyPath());
        } else {
            resolvedServerCache = serverCache;
        }
        final List<Event> events = new ArrayList<>();
        final Node node = resolvedServerCache;
        syncPointTree.getChildren().inOrderTraversal(new LLRBNode.NodeVisitor<ChildKey, ImmutableTree<SyncPoint>>() { // from class: com.google.firebase.database.core.SyncTree.16
            @Override // com.google.firebase.database.collection.LLRBNode.NodeVisitor
            public void visitEntry(ChildKey key, ImmutableTree<SyncPoint> childTree) {
                Node childServerCache = null;
                Node node2 = node;
                if (node2 != null) {
                    childServerCache = node2.getImmediateChild(key);
                }
                WriteTreeRef childWritesCache = writesCache.child(key);
                Operation childOperation = operation.operationForChild(key);
                if (childOperation != null) {
                    events.addAll(SyncTree.this.applyOperationDescendantsHelper(childOperation, childTree, childServerCache, childWritesCache));
                }
            }
        });
        if (syncPoint != null) {
            events.addAll(syncPoint.applyOperation(operation, writesCache, resolvedServerCache));
        }
        return events;
    }

    ImmutableTree<SyncPoint> getSyncPointTree() {
        return this.syncPointTree;
    }
}
