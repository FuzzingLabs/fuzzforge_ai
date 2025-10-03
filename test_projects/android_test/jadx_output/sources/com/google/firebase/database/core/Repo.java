package com.google.firebase.database.core;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.InternalHelpers;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.connection.HostInfo;
import com.google.firebase.database.connection.ListenHashProvider;
import com.google.firebase.database.connection.PersistentConnection;
import com.google.firebase.database.connection.RangeMerge;
import com.google.firebase.database.connection.RequestResultCallback;
import com.google.firebase.database.core.SparseSnapshotTree;
import com.google.firebase.database.core.SyncTree;
import com.google.firebase.database.core.TokenProvider;
import com.google.firebase.database.core.persistence.NoopPersistenceManager;
import com.google.firebase.database.core.persistence.PersistenceManager;
import com.google.firebase.database.core.utilities.DefaultClock;
import com.google.firebase.database.core.utilities.DefaultRunLoop;
import com.google.firebase.database.core.utilities.OffsetClock;
import com.google.firebase.database.core.utilities.Tree;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.core.view.Event;
import com.google.firebase.database.core.view.EventRaiser;
import com.google.firebase.database.core.view.QuerySpec;
import com.google.firebase.database.logging.LogWrapper;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.EmptyNode;
import com.google.firebase.database.snapshot.IndexedNode;
import com.google.firebase.database.snapshot.Node;
import com.google.firebase.database.snapshot.NodeUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class Repo implements PersistentConnection.Delegate {
    private static final String INTERRUPT_REASON = "repo_interrupt";
    private static final int TRANSACTION_MAX_RETRIES = 25;
    private static final String TRANSACTION_OVERRIDE_BY_SET = "overriddenBySet";
    private static final String TRANSACTION_TOO_MANY_RETRIES = "maxretries";
    private PersistentConnection connection;
    private final Context ctx;
    private final LogWrapper dataLogger;
    private FirebaseDatabase database;
    private final EventRaiser eventRaiser;
    private SnapshotHolder infoData;
    private SyncTree infoSyncTree;
    private SparseSnapshotTree onDisconnect;
    private final LogWrapper operationLogger;
    private final RepoInfo repoInfo;
    private SyncTree serverSyncTree;
    private final LogWrapper transactionLogger;
    private Tree<List<TransactionData>> transactionQueueTree;
    private final OffsetClock serverClock = new OffsetClock(new DefaultClock(), 0);
    private boolean hijackHash = false;
    public long dataUpdateCount = 0;
    private long nextWriteId = 1;
    private boolean loggedTransactionPersistenceWarning = false;
    private long transactionOrder = 0;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public enum TransactionStatus {
        INITIALIZING,
        RUN,
        SENT,
        COMPLETED,
        SENT_NEEDS_ABORT,
        NEEDS_ABORT
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Repo(RepoInfo repoInfo, Context ctx, FirebaseDatabase database) {
        this.repoInfo = repoInfo;
        this.ctx = ctx;
        this.database = database;
        this.operationLogger = ctx.getLogger("RepoOperation");
        this.transactionLogger = ctx.getLogger("Transaction");
        this.dataLogger = ctx.getLogger("DataOperation");
        this.eventRaiser = new EventRaiser(ctx);
        scheduleNow(new Runnable() { // from class: com.google.firebase.database.core.Repo.1
            @Override // java.lang.Runnable
            public void run() {
                Repo.this.deferredInitialization();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void deferredInitialization() {
        HostInfo hostInfo = new HostInfo(this.repoInfo.host, this.repoInfo.namespace, this.repoInfo.secure);
        this.connection = this.ctx.newPersistentConnection(hostInfo, this);
        this.ctx.getAuthTokenProvider().addTokenChangeListener(((DefaultRunLoop) this.ctx.getRunLoop()).getExecutorService(), new TokenProvider.TokenChangeListener() { // from class: com.google.firebase.database.core.Repo.2
            @Override // com.google.firebase.database.core.TokenProvider.TokenChangeListener
            public void onTokenChange() {
                Repo.this.operationLogger.debug("Auth token changed, triggering auth token refresh", new Object[0]);
                Repo.this.connection.refreshAuthToken();
            }

            @Override // com.google.firebase.database.core.TokenProvider.TokenChangeListener
            public void onTokenChange(String token) {
                Repo.this.operationLogger.debug("Auth token changed, triggering auth token refresh", new Object[0]);
                Repo.this.connection.refreshAuthToken(token);
            }
        });
        this.ctx.getAppCheckTokenProvider().addTokenChangeListener(((DefaultRunLoop) this.ctx.getRunLoop()).getExecutorService(), new TokenProvider.TokenChangeListener() { // from class: com.google.firebase.database.core.Repo.3
            @Override // com.google.firebase.database.core.TokenProvider.TokenChangeListener
            public void onTokenChange() {
                Repo.this.operationLogger.debug("App check token changed, triggering app check token refresh", new Object[0]);
                Repo.this.connection.refreshAppCheckToken();
            }

            @Override // com.google.firebase.database.core.TokenProvider.TokenChangeListener
            public void onTokenChange(String token) {
                Repo.this.operationLogger.debug("App check token changed, triggering app check token refresh", new Object[0]);
                Repo.this.connection.refreshAppCheckToken(token);
            }
        });
        this.connection.initialize();
        PersistenceManager persistenceManager = this.ctx.getPersistenceManager(this.repoInfo.host);
        this.infoData = new SnapshotHolder();
        this.onDisconnect = new SparseSnapshotTree();
        this.transactionQueueTree = new Tree<>();
        this.infoSyncTree = new SyncTree(this.ctx, new NoopPersistenceManager(), new SyncTree.ListenProvider() { // from class: com.google.firebase.database.core.Repo.4
            @Override // com.google.firebase.database.core.SyncTree.ListenProvider
            public void startListening(final QuerySpec query, Tag tag, ListenHashProvider hash, final SyncTree.CompletionListener onComplete) {
                Repo.this.scheduleNow(new Runnable() { // from class: com.google.firebase.database.core.Repo.4.1
                    @Override // java.lang.Runnable
                    public void run() {
                        Node node = Repo.this.infoData.getNode(query.getPath());
                        if (!node.isEmpty()) {
                            List<? extends Event> infoEvents = Repo.this.infoSyncTree.applyServerOverwrite(query.getPath(), node);
                            Repo.this.postEvents(infoEvents);
                            onComplete.onListenComplete(null);
                        }
                    }
                });
            }

            @Override // com.google.firebase.database.core.SyncTree.ListenProvider
            public void stopListening(QuerySpec query, Tag tag) {
            }
        });
        this.serverSyncTree = new SyncTree(this.ctx, persistenceManager, new SyncTree.ListenProvider() { // from class: com.google.firebase.database.core.Repo.5
            @Override // com.google.firebase.database.core.SyncTree.ListenProvider
            public void startListening(QuerySpec query, Tag tag, ListenHashProvider hash, final SyncTree.CompletionListener onListenComplete) {
                Repo.this.connection.listen(query.getPath().asList(), query.getParams().getWireProtocolParams(), hash, tag != null ? Long.valueOf(tag.getTagNumber()) : null, new RequestResultCallback() { // from class: com.google.firebase.database.core.Repo.5.1
                    @Override // com.google.firebase.database.connection.RequestResultCallback
                    public void onRequestResult(String optErrorCode, String optErrorMessage) {
                        DatabaseError error = Repo.fromErrorCode(optErrorCode, optErrorMessage);
                        List<? extends Event> events = onListenComplete.onListenComplete(error);
                        Repo.this.postEvents(events);
                    }
                });
            }

            @Override // com.google.firebase.database.core.SyncTree.ListenProvider
            public void stopListening(QuerySpec query, Tag tag) {
                Repo.this.connection.unlisten(query.getPath().asList(), query.getParams().getWireProtocolParams());
            }
        });
        restoreWrites(persistenceManager);
        updateInfo(Constants.DOT_INFO_AUTHENTICATED, false);
        updateInfo(Constants.DOT_INFO_CONNECTED, false);
    }

    private void restoreWrites(PersistenceManager persistenceManager) {
        List<UserWriteRecord> writes = persistenceManager.loadUserWrites();
        Map<String, Object> serverValues = ServerValues.generateServerValues(this.serverClock);
        long lastWriteId = Long.MIN_VALUE;
        for (final UserWriteRecord write : writes) {
            RequestResultCallback onComplete = new RequestResultCallback() { // from class: com.google.firebase.database.core.Repo.6
                @Override // com.google.firebase.database.connection.RequestResultCallback
                public void onRequestResult(String optErrorCode, String optErrorMessage) {
                    DatabaseError error = Repo.fromErrorCode(optErrorCode, optErrorMessage);
                    Repo.this.warnIfWriteFailed("Persisted write", write.getPath(), error);
                    Repo.this.ackWriteAndRerunTransactions(write.getWriteId(), write.getPath(), error);
                }
            };
            if (lastWriteId >= write.getWriteId()) {
                throw new IllegalStateException("Write ids were not in order.");
            }
            lastWriteId = write.getWriteId();
            this.nextWriteId = write.getWriteId() + 1;
            if (write.isOverwrite()) {
                if (this.operationLogger.logsDebug()) {
                    this.operationLogger.debug("Restoring overwrite with id " + write.getWriteId(), new Object[0]);
                }
                this.connection.put(write.getPath().asList(), write.getOverwrite().getValue(true), onComplete);
                Node resolved = ServerValues.resolveDeferredValueSnapshot(write.getOverwrite(), this.serverSyncTree, write.getPath(), serverValues);
                this.serverSyncTree.applyUserOverwrite(write.getPath(), write.getOverwrite(), resolved, write.getWriteId(), true, false);
            } else {
                if (this.operationLogger.logsDebug()) {
                    this.operationLogger.debug("Restoring merge with id " + write.getWriteId(), new Object[0]);
                }
                this.connection.merge(write.getPath().asList(), write.getMerge().getValue(true), onComplete);
                CompoundWrite resolved2 = ServerValues.resolveDeferredValueMerge(write.getMerge(), this.serverSyncTree, write.getPath(), serverValues);
                this.serverSyncTree.applyUserMerge(write.getPath(), write.getMerge(), resolved2, write.getWriteId(), false);
            }
        }
    }

    public FirebaseDatabase getDatabase() {
        return this.database;
    }

    public String toString() {
        return this.repoInfo.toString();
    }

    public RepoInfo getRepoInfo() {
        return this.repoInfo;
    }

    public void scheduleNow(Runnable r) {
        this.ctx.requireStarted();
        this.ctx.getRunLoop().scheduleNow(r);
    }

    public void postEvent(Runnable r) {
        this.ctx.requireStarted();
        this.ctx.getEventTarget().postEvent(r);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void postEvents(List<? extends Event> events) {
        if (!events.isEmpty()) {
            this.eventRaiser.raiseEvents(events);
        }
    }

    public long getServerTime() {
        return this.serverClock.millis();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean hasListeners() {
        return (this.infoSyncTree.isEmpty() && this.serverSyncTree.isEmpty()) ? false : true;
    }

    @Override // com.google.firebase.database.connection.PersistentConnection.Delegate
    public void onDataUpdate(List<String> pathSegments, Object message, boolean isMerge, Long optTag) {
        List<? extends Event> events;
        Path path = new Path(pathSegments);
        if (this.operationLogger.logsDebug()) {
            this.operationLogger.debug("onDataUpdate: " + path, new Object[0]);
        }
        if (this.dataLogger.logsDebug()) {
            this.operationLogger.debug("onDataUpdate: " + path + " " + message, new Object[0]);
        }
        this.dataUpdateCount++;
        try {
            if (optTag != null) {
                Tag tag = new Tag(optTag.longValue());
                if (isMerge) {
                    Map<Path, Node> taggedChildren = new HashMap<>();
                    Map<String, Object> rawMergeData = (Map) message;
                    for (Map.Entry<String, Object> entry : rawMergeData.entrySet()) {
                        Node newChildNode = NodeUtilities.NodeFromJSON(entry.getValue());
                        taggedChildren.put(new Path(entry.getKey()), newChildNode);
                    }
                    events = this.serverSyncTree.applyTaggedQueryMerge(path, taggedChildren, tag);
                } else {
                    Node taggedSnap = NodeUtilities.NodeFromJSON(message);
                    events = this.serverSyncTree.applyTaggedQueryOverwrite(path, taggedSnap, tag);
                }
            } else if (isMerge) {
                Map<Path, Node> changedChildren = new HashMap<>();
                Map<String, Object> rawMergeData2 = (Map) message;
                for (Map.Entry<String, Object> entry2 : rawMergeData2.entrySet()) {
                    Node newChildNode2 = NodeUtilities.NodeFromJSON(entry2.getValue());
                    changedChildren.put(new Path(entry2.getKey()), newChildNode2);
                }
                events = this.serverSyncTree.applyServerMerge(path, changedChildren);
            } else {
                Node snap = NodeUtilities.NodeFromJSON(message);
                events = this.serverSyncTree.applyServerOverwrite(path, snap);
            }
            if (events.size() > 0) {
                rerunTransactions(path);
            }
            postEvents(events);
        } catch (DatabaseException e) {
            this.operationLogger.error("FIREBASE INTERNAL ERROR", e);
        }
    }

    @Override // com.google.firebase.database.connection.PersistentConnection.Delegate
    public void onRangeMergeUpdate(List<String> pathSegments, List<RangeMerge> merges, Long tagNumber) {
        List<? extends Event> events;
        Path path = new Path(pathSegments);
        if (this.operationLogger.logsDebug()) {
            this.operationLogger.debug("onRangeMergeUpdate: " + path, new Object[0]);
        }
        if (this.dataLogger.logsDebug()) {
            this.operationLogger.debug("onRangeMergeUpdate: " + path + " " + merges, new Object[0]);
        }
        this.dataUpdateCount++;
        List<com.google.firebase.database.snapshot.RangeMerge> parsedMerges = new ArrayList<>(merges.size());
        for (RangeMerge merge : merges) {
            parsedMerges.add(new com.google.firebase.database.snapshot.RangeMerge(merge));
        }
        if (tagNumber != null) {
            events = this.serverSyncTree.applyTaggedRangeMerges(path, parsedMerges, new Tag(tagNumber.longValue()));
        } else {
            events = this.serverSyncTree.applyServerRangeMerges(path, parsedMerges);
        }
        if (events.size() > 0) {
            rerunTransactions(path);
        }
        postEvents(events);
    }

    void callOnComplete(final DatabaseReference.CompletionListener onComplete, final DatabaseError error, Path path) {
        final DatabaseReference ref;
        if (onComplete != null) {
            ChildKey last = path.getBack();
            if (last != null && last.isPriorityChildName()) {
                ref = InternalHelpers.createReference(this, path.getParent());
            } else {
                ref = InternalHelpers.createReference(this, path);
            }
            postEvent(new Runnable() { // from class: com.google.firebase.database.core.Repo.7
                @Override // java.lang.Runnable
                public void run() {
                    onComplete.onComplete(error, ref);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void ackWriteAndRerunTransactions(long writeId, Path path, DatabaseError error) {
        if (error == null || error.getCode() != -25) {
            boolean success = error == null;
            List<? extends Event> clearEvents = this.serverSyncTree.ackUserWrite(writeId, !success, true, this.serverClock);
            if (clearEvents.size() > 0) {
                rerunTransactions(path);
            }
            postEvents(clearEvents);
        }
    }

    public void setValue(final Path path, Node newValueUnresolved, final DatabaseReference.CompletionListener onComplete) {
        if (this.operationLogger.logsDebug()) {
            this.operationLogger.debug("set: " + path, new Object[0]);
        }
        if (this.dataLogger.logsDebug()) {
            this.dataLogger.debug("set: " + path + " " + newValueUnresolved, new Object[0]);
        }
        Map<String, Object> serverValues = ServerValues.generateServerValues(this.serverClock);
        Node existing = this.serverSyncTree.calcCompleteEventCache(path, new ArrayList());
        Node newValue = ServerValues.resolveDeferredValueSnapshot(newValueUnresolved, existing, serverValues);
        final long writeId = getNextWriteId();
        List<? extends Event> events = this.serverSyncTree.applyUserOverwrite(path, newValueUnresolved, newValue, writeId, true, true);
        postEvents(events);
        this.connection.put(path.asList(), newValueUnresolved.getValue(true), new RequestResultCallback() { // from class: com.google.firebase.database.core.Repo.8
            @Override // com.google.firebase.database.connection.RequestResultCallback
            public void onRequestResult(String optErrorCode, String optErrorMessage) {
                DatabaseError error = Repo.fromErrorCode(optErrorCode, optErrorMessage);
                Repo.this.warnIfWriteFailed("setValue", path, error);
                Repo.this.ackWriteAndRerunTransactions(writeId, path, error);
                Repo.this.callOnComplete(onComplete, error, path);
            }
        });
        Path affectedPath = abortTransactions(path, -9);
        rerunTransactions(affectedPath);
    }

    public Task<DataSnapshot> getValue(final Query query) {
        final TaskCompletionSource<DataSnapshot> source = new TaskCompletionSource<>();
        scheduleNow(new Runnable() { // from class: com.google.firebase.database.core.Repo.9
            @Override // java.lang.Runnable
            public void run() {
                Node serverValue = Repo.this.serverSyncTree.getServerValue(query.getSpec());
                if (serverValue == null) {
                    Repo.this.serverSyncTree.setQueryActive(query.getSpec());
                    Repo.this.connection.get(query.getPath().asList(), query.getSpec().getParams().getWireProtocolParams()).addOnCompleteListener(((DefaultRunLoop) Repo.this.ctx.getRunLoop()).getExecutorService(), new OnCompleteListener<Object>() { // from class: com.google.firebase.database.core.Repo.9.1
                        @Override // com.google.android.gms.tasks.OnCompleteListener
                        public void onComplete(Task<Object> task) {
                            if (!task.isSuccessful()) {
                                Repo.this.operationLogger.info("get for query " + query.getPath() + " falling back to disk cache after error: " + task.getException().getMessage());
                                DataSnapshot cached = Repo.this.serverSyncTree.persistenceServerCache(query);
                                if (!cached.exists()) {
                                    source.setException(task.getException());
                                } else {
                                    source.setResult(cached);
                                }
                            } else {
                                Node serverNode = NodeUtilities.NodeFromJSON(task.getResult());
                                Repo.this.postEvents(Repo.this.serverSyncTree.applyServerOverwrite(query.getPath(), serverNode));
                                source.setResult(InternalHelpers.createDataSnapshot(query.getRef(), IndexedNode.from(serverNode, query.getSpec().getIndex())));
                            }
                            Repo.this.serverSyncTree.setQueryInactive(query.getSpec());
                        }
                    });
                } else {
                    source.setResult(InternalHelpers.createDataSnapshot(query.getRef(), IndexedNode.from(serverValue)));
                }
            }
        });
        return source.getTask();
    }

    public void updateChildren(final Path path, CompoundWrite updates, final DatabaseReference.CompletionListener onComplete, Map<String, Object> unParsedUpdates) {
        if (this.operationLogger.logsDebug()) {
            this.operationLogger.debug("update: " + path, new Object[0]);
        }
        if (this.dataLogger.logsDebug()) {
            this.dataLogger.debug("update: " + path + " " + unParsedUpdates, new Object[0]);
        }
        if (!updates.isEmpty()) {
            Map<String, Object> serverValues = ServerValues.generateServerValues(this.serverClock);
            CompoundWrite resolved = ServerValues.resolveDeferredValueMerge(updates, this.serverSyncTree, path, serverValues);
            final long writeId = getNextWriteId();
            List<? extends Event> events = this.serverSyncTree.applyUserMerge(path, updates, resolved, writeId, true);
            postEvents(events);
            this.connection.merge(path.asList(), unParsedUpdates, new RequestResultCallback() { // from class: com.google.firebase.database.core.Repo.10
                @Override // com.google.firebase.database.connection.RequestResultCallback
                public void onRequestResult(String optErrorCode, String optErrorMessage) {
                    DatabaseError error = Repo.fromErrorCode(optErrorCode, optErrorMessage);
                    Repo.this.warnIfWriteFailed("updateChildren", path, error);
                    Repo.this.ackWriteAndRerunTransactions(writeId, path, error);
                    Repo.this.callOnComplete(onComplete, error, path);
                }
            });
            Iterator<Map.Entry<Path, Node>> it = updates.iterator();
            while (it.hasNext()) {
                Map.Entry<Path, Node> update = it.next();
                Path pathFromRoot = path.child(update.getKey());
                Path affectedPath = abortTransactions(pathFromRoot, -9);
                rerunTransactions(affectedPath);
            }
            return;
        }
        if (this.operationLogger.logsDebug()) {
            this.operationLogger.debug("update called with no changes. No-op", new Object[0]);
        }
        callOnComplete(onComplete, null, path);
    }

    public void purgeOutstandingWrites() {
        if (this.operationLogger.logsDebug()) {
            this.operationLogger.debug("Purging writes", new Object[0]);
        }
        List<? extends Event> events = this.serverSyncTree.removeAllWrites();
        postEvents(events);
        abortTransactions(Path.getEmptyPath(), -25);
        this.connection.purgeOutstandingWrites();
    }

    public void removeEventCallback(EventRegistration eventRegistration) {
        List<Event> events;
        if (Constants.DOT_INFO.equals(eventRegistration.getQuerySpec().getPath().getFront())) {
            events = this.infoSyncTree.removeEventRegistration(eventRegistration);
        } else {
            events = this.serverSyncTree.removeEventRegistration(eventRegistration);
        }
        postEvents(events);
    }

    public void onDisconnectSetValue(final Path path, final Node newValue, final DatabaseReference.CompletionListener onComplete) {
        this.connection.onDisconnectPut(path.asList(), newValue.getValue(true), new RequestResultCallback() { // from class: com.google.firebase.database.core.Repo.11
            @Override // com.google.firebase.database.connection.RequestResultCallback
            public void onRequestResult(String optErrorCode, String optErrorMessage) {
                DatabaseError error = Repo.fromErrorCode(optErrorCode, optErrorMessage);
                Repo.this.warnIfWriteFailed("onDisconnect().setValue", path, error);
                if (error == null) {
                    Repo.this.onDisconnect.remember(path, newValue);
                }
                Repo.this.callOnComplete(onComplete, error, path);
            }
        });
    }

    public void onDisconnectUpdate(final Path path, final Map<Path, Node> newChildren, final DatabaseReference.CompletionListener listener, Map<String, Object> unParsedUpdates) {
        this.connection.onDisconnectMerge(path.asList(), unParsedUpdates, new RequestResultCallback() { // from class: com.google.firebase.database.core.Repo.12
            @Override // com.google.firebase.database.connection.RequestResultCallback
            public void onRequestResult(String optErrorCode, String optErrorMessage) {
                DatabaseError error = Repo.fromErrorCode(optErrorCode, optErrorMessage);
                Repo.this.warnIfWriteFailed("onDisconnect().updateChildren", path, error);
                if (error == null) {
                    for (Map.Entry<Path, Node> entry : newChildren.entrySet()) {
                        Repo.this.onDisconnect.remember(path.child(entry.getKey()), entry.getValue());
                    }
                }
                Repo.this.callOnComplete(listener, error, path);
            }
        });
    }

    public void onDisconnectCancel(final Path path, final DatabaseReference.CompletionListener onComplete) {
        this.connection.onDisconnectCancel(path.asList(), new RequestResultCallback() { // from class: com.google.firebase.database.core.Repo.13
            @Override // com.google.firebase.database.connection.RequestResultCallback
            public void onRequestResult(String optErrorCode, String optErrorMessage) {
                DatabaseError error = Repo.fromErrorCode(optErrorCode, optErrorMessage);
                if (error == null) {
                    Repo.this.onDisconnect.forget(path);
                }
                Repo.this.callOnComplete(onComplete, error, path);
            }
        });
    }

    @Override // com.google.firebase.database.connection.PersistentConnection.Delegate
    public void onConnect() {
        onServerInfoUpdate(Constants.DOT_INFO_CONNECTED, true);
    }

    @Override // com.google.firebase.database.connection.PersistentConnection.Delegate
    public void onDisconnect() {
        onServerInfoUpdate(Constants.DOT_INFO_CONNECTED, false);
        runOnDisconnectEvents();
    }

    @Override // com.google.firebase.database.connection.PersistentConnection.Delegate
    public void onConnectionStatus(boolean connectionOk) {
        onServerInfoUpdate(Constants.DOT_INFO_AUTHENTICATED, Boolean.valueOf(connectionOk));
    }

    public void onServerInfoUpdate(ChildKey key, Object value) {
        updateInfo(key, value);
    }

    @Override // com.google.firebase.database.connection.PersistentConnection.Delegate
    public void onServerInfoUpdate(Map<String, Object> updates) {
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            updateInfo(ChildKey.fromString(entry.getKey()), entry.getValue());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void interrupt() {
        this.connection.interrupt(INTERRUPT_REASON);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void resume() {
        this.connection.resume(INTERRUPT_REASON);
    }

    public void addEventCallback(EventRegistration eventRegistration) {
        List<? extends Event> events;
        ChildKey front = eventRegistration.getQuerySpec().getPath().getFront();
        if (front != null && front.equals(Constants.DOT_INFO)) {
            events = this.infoSyncTree.addEventRegistration(eventRegistration);
        } else {
            events = this.serverSyncTree.addEventRegistration(eventRegistration);
        }
        postEvents(events);
    }

    public void keepSynced(QuerySpec query, boolean keep) {
        Utilities.hardAssert(query.getPath().isEmpty() || !query.getPath().getFront().equals(Constants.DOT_INFO));
        this.serverSyncTree.keepSynced(query, keep);
    }

    PersistentConnection getConnection() {
        return this.connection;
    }

    private void updateInfo(ChildKey childKey, Object value) {
        if (childKey.equals(Constants.DOT_INFO_SERVERTIME_OFFSET)) {
            this.serverClock.setOffset(((Long) value).longValue());
        }
        Path path = new Path(Constants.DOT_INFO, childKey);
        try {
            Node node = NodeUtilities.NodeFromJSON(value);
            this.infoData.update(path, node);
            List<? extends Event> events = this.infoSyncTree.applyServerOverwrite(path, node);
            postEvents(events);
        } catch (DatabaseException e) {
            this.operationLogger.error("Failed to parse info update", e);
        }
    }

    private long getNextWriteId() {
        long j = this.nextWriteId;
        this.nextWriteId = 1 + j;
        return j;
    }

    private void runOnDisconnectEvents() {
        final Map<String, Object> serverValues = ServerValues.generateServerValues(this.serverClock);
        final List<Event> events = new ArrayList<>();
        this.onDisconnect.forEachTree(Path.getEmptyPath(), new SparseSnapshotTree.SparseSnapshotTreeVisitor() { // from class: com.google.firebase.database.core.Repo.14
            @Override // com.google.firebase.database.core.SparseSnapshotTree.SparseSnapshotTreeVisitor
            public void visitTree(Path prefixPath, Node node) {
                Node existing = Repo.this.serverSyncTree.calcCompleteEventCache(prefixPath, new ArrayList());
                Node resolvedNode = ServerValues.resolveDeferredValueSnapshot(node, existing, (Map<String, Object>) serverValues);
                events.addAll(Repo.this.serverSyncTree.applyServerOverwrite(prefixPath, resolvedNode));
                Path affectedPath = Repo.this.abortTransactions(prefixPath, -9);
                Repo.this.rerunTransactions(affectedPath);
            }
        });
        this.onDisconnect = new SparseSnapshotTree();
        postEvents(events);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void warnIfWriteFailed(String writeType, Path path, DatabaseError error) {
        if (error != null && error.getCode() != -1 && error.getCode() != -25) {
            this.operationLogger.warn(writeType + " at " + path.toString() + " failed: " + error.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class TransactionData implements Comparable<TransactionData> {
        private DatabaseError abortReason;
        private boolean applyLocally;
        private Node currentInputSnapshot;
        private Node currentOutputSnapshotRaw;
        private Node currentOutputSnapshotResolved;
        private long currentWriteId;
        private Transaction.Handler handler;
        private long order;
        private ValueEventListener outstandingListener;
        private Path path;
        private int retryCount;
        private TransactionStatus status;

        static /* synthetic */ int access$2108(TransactionData x0) {
            int i = x0.retryCount;
            x0.retryCount = i + 1;
            return i;
        }

        private TransactionData(Path path, Transaction.Handler handler, ValueEventListener outstandingListener, TransactionStatus status, boolean applyLocally, long order) {
            this.path = path;
            this.handler = handler;
            this.outstandingListener = outstandingListener;
            this.status = status;
            this.retryCount = 0;
            this.applyLocally = applyLocally;
            this.order = order;
            this.abortReason = null;
            this.currentInputSnapshot = null;
            this.currentOutputSnapshotRaw = null;
            this.currentOutputSnapshotResolved = null;
        }

        @Override // java.lang.Comparable
        public int compareTo(TransactionData o) {
            long j = this.order;
            long j2 = o.order;
            if (j < j2) {
                return -1;
            }
            if (j == j2) {
                return 0;
            }
            return 1;
        }
    }

    public void startTransaction(Path path, final Transaction.Handler handler, boolean applyLocally) {
        DatabaseError error;
        Transaction.Result result;
        List<TransactionData> nodeQueue;
        if (this.operationLogger.logsDebug()) {
            this.operationLogger.debug("transaction: " + path, new Object[0]);
        }
        if (this.dataLogger.logsDebug()) {
            this.operationLogger.debug("transaction: " + path, new Object[0]);
        }
        if (this.ctx.isPersistenceEnabled() && !this.loggedTransactionPersistenceWarning) {
            this.loggedTransactionPersistenceWarning = true;
            this.transactionLogger.info("runTransaction() usage detected while persistence is enabled. Please be aware that transactions *will not* be persisted across database restarts.  See https://www.firebase.com/docs/android/guide/offline-capabilities.html#section-handling-transactions-offline for more details.");
        }
        DatabaseReference watchRef = InternalHelpers.createReference(this, path);
        ValueEventListener listener = new ValueEventListener() { // from class: com.google.firebase.database.core.Repo.15
            @Override // com.google.firebase.database.ValueEventListener
            public void onDataChange(DataSnapshot snapshot) {
            }

            @Override // com.google.firebase.database.ValueEventListener
            public void onCancelled(DatabaseError error2) {
            }
        };
        addEventCallback(new ValueEventRegistration(this, listener, watchRef.getSpec()));
        TransactionData transaction = new TransactionData(path, handler, listener, TransactionStatus.INITIALIZING, applyLocally, nextTransactionOrder());
        Node currentState = getLatestState(path);
        transaction.currentInputSnapshot = currentState;
        MutableData mutableCurrent = InternalHelpers.createMutableData(currentState);
        try {
            result = handler.doTransaction(mutableCurrent);
        } catch (Throwable e) {
            this.operationLogger.error("Caught Throwable.", e);
            DatabaseError error2 = DatabaseError.fromException(e);
            error = error2;
            result = Transaction.abort();
        }
        if (result == null) {
            throw new NullPointerException("Transaction returned null as result");
        }
        error = null;
        if (!result.isSuccess()) {
            transaction.currentOutputSnapshotRaw = null;
            transaction.currentOutputSnapshotResolved = null;
            final DatabaseError innerClassError = error;
            final DataSnapshot snap = InternalHelpers.createDataSnapshot(watchRef, IndexedNode.from(transaction.currentInputSnapshot));
            postEvent(new Runnable() { // from class: com.google.firebase.database.core.Repo.16
                @Override // java.lang.Runnable
                public void run() {
                    handler.onComplete(innerClassError, false, snap);
                }
            });
            return;
        }
        transaction.status = TransactionStatus.RUN;
        Tree<List<TransactionData>> queueNode = this.transactionQueueTree.subTree(path);
        List<TransactionData> nodeQueue2 = queueNode.getValue();
        if (nodeQueue2 != null) {
            nodeQueue = nodeQueue2;
        } else {
            nodeQueue = new ArrayList<>();
        }
        nodeQueue.add(transaction);
        queueNode.setValue(nodeQueue);
        Map<String, Object> serverValues = ServerValues.generateServerValues(this.serverClock);
        Node newNodeUnresolved = result.getNode();
        Node newNode = ServerValues.resolveDeferredValueSnapshot(newNodeUnresolved, transaction.currentInputSnapshot, serverValues);
        transaction.currentOutputSnapshotRaw = newNodeUnresolved;
        transaction.currentOutputSnapshotResolved = newNode;
        transaction.currentWriteId = getNextWriteId();
        List<? extends Event> events = this.serverSyncTree.applyUserOverwrite(path, newNodeUnresolved, newNode, transaction.currentWriteId, applyLocally, false);
        postEvents(events);
        sendAllReadyTransactions();
    }

    private Node getLatestState(Path path) {
        return getLatestState(path, new ArrayList());
    }

    private Node getLatestState(Path path, List<Long> excudeSets) {
        Node state = this.serverSyncTree.calcCompleteEventCache(path, excudeSets);
        if (state == null) {
            return EmptyNode.Empty();
        }
        return state;
    }

    public void setHijackHash(boolean hijackHash) {
        this.hijackHash = hijackHash;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendAllReadyTransactions() {
        Tree<List<TransactionData>> node = this.transactionQueueTree;
        pruneCompletedTransactions(node);
        sendReadyTransactions(node);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendReadyTransactions(Tree<List<TransactionData>> node) {
        if (node.getValue() == null) {
            if (node.hasChildren()) {
                node.forEachChild(new Tree.TreeVisitor<List<TransactionData>>() { // from class: com.google.firebase.database.core.Repo.17
                    @Override // com.google.firebase.database.core.utilities.Tree.TreeVisitor
                    public void visitTree(Tree<List<TransactionData>> tree) {
                        Repo.this.sendReadyTransactions(tree);
                    }
                });
                return;
            }
            return;
        }
        List<TransactionData> queue = buildTransactionQueue(node);
        Utilities.hardAssert(queue.size() > 0);
        Boolean allRun = true;
        Iterator<TransactionData> it = queue.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            TransactionData transaction = it.next();
            if (transaction.status != TransactionStatus.RUN) {
                allRun = false;
                break;
            }
        }
        if (allRun.booleanValue()) {
            sendTransactionQueue(queue, node.getPath());
        }
    }

    private void sendTransactionQueue(final List<TransactionData> queue, final Path path) {
        List<Long> setsToIgnore = new ArrayList<>();
        Iterator<TransactionData> it = queue.iterator();
        while (it.hasNext()) {
            setsToIgnore.add(Long.valueOf(it.next().currentWriteId));
        }
        Node latestState = getLatestState(path, setsToIgnore);
        Node snapToSend = latestState;
        String latestHash = "badhash";
        if (!this.hijackHash) {
            latestHash = latestState.getHash();
        }
        Iterator<TransactionData> it2 = queue.iterator();
        while (true) {
            boolean z = true;
            if (it2.hasNext()) {
                TransactionData txn = it2.next();
                if (txn.status != TransactionStatus.RUN) {
                    z = false;
                }
                Utilities.hardAssert(z);
                txn.status = TransactionStatus.SENT;
                TransactionData.access$2108(txn);
                Path relativePath = Path.getRelative(path, txn.path);
                snapToSend = snapToSend.updateChild(relativePath, txn.currentOutputSnapshotRaw);
            } else {
                Object dataToSend = snapToSend.getValue(true);
                this.connection.compareAndPut(path.asList(), dataToSend, latestHash, new RequestResultCallback() { // from class: com.google.firebase.database.core.Repo.18
                    @Override // com.google.firebase.database.connection.RequestResultCallback
                    public void onRequestResult(String optErrorCode, String optErrorMessage) {
                        DatabaseError error = Repo.fromErrorCode(optErrorCode, optErrorMessage);
                        Repo.this.warnIfWriteFailed("Transaction", path, error);
                        List<Event> events = new ArrayList<>();
                        if (error == null) {
                            List<Runnable> callbacks = new ArrayList<>();
                            for (final TransactionData txn2 : queue) {
                                txn2.status = TransactionStatus.COMPLETED;
                                events.addAll(Repo.this.serverSyncTree.ackUserWrite(txn2.currentWriteId, false, false, Repo.this.serverClock));
                                Node node = txn2.currentOutputSnapshotResolved;
                                final DataSnapshot snap = InternalHelpers.createDataSnapshot(InternalHelpers.createReference(repo, txn2.path), IndexedNode.from(node));
                                callbacks.add(new Runnable() { // from class: com.google.firebase.database.core.Repo.18.1
                                    @Override // java.lang.Runnable
                                    public void run() {
                                        txn2.handler.onComplete(null, true, snap);
                                    }
                                });
                                Repo repo = Repo.this;
                                repo.removeEventCallback(new ValueEventRegistration(repo, txn2.outstandingListener, QuerySpec.defaultQueryAtPath(txn2.path)));
                            }
                            Repo repo2 = Repo.this;
                            repo2.pruneCompletedTransactions(repo2.transactionQueueTree.subTree(path));
                            Repo.this.sendAllReadyTransactions();
                            repo.postEvents(events);
                            for (int i = 0; i < callbacks.size(); i++) {
                                Repo.this.postEvent(callbacks.get(i));
                            }
                            return;
                        }
                        if (error.getCode() == -1) {
                            for (TransactionData transaction : queue) {
                                if (transaction.status == TransactionStatus.SENT_NEEDS_ABORT) {
                                    transaction.status = TransactionStatus.NEEDS_ABORT;
                                } else {
                                    transaction.status = TransactionStatus.RUN;
                                }
                            }
                        } else {
                            for (TransactionData transaction2 : queue) {
                                transaction2.status = TransactionStatus.NEEDS_ABORT;
                                transaction2.abortReason = error;
                            }
                        }
                        Repo.this.rerunTransactions(path);
                    }
                });
                return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void pruneCompletedTransactions(Tree<List<TransactionData>> node) {
        List<TransactionData> queue = node.getValue();
        if (queue != null) {
            int i = 0;
            while (i < queue.size()) {
                TransactionData transaction = queue.get(i);
                if (transaction.status == TransactionStatus.COMPLETED) {
                    queue.remove(i);
                } else {
                    i++;
                }
            }
            if (queue.size() > 0) {
                node.setValue(queue);
            } else {
                node.setValue(null);
            }
        }
        node.forEachChild(new Tree.TreeVisitor<List<TransactionData>>() { // from class: com.google.firebase.database.core.Repo.19
            @Override // com.google.firebase.database.core.utilities.Tree.TreeVisitor
            public void visitTree(Tree<List<TransactionData>> tree) {
                Repo.this.pruneCompletedTransactions(tree);
            }
        });
    }

    private long nextTransactionOrder() {
        long j = this.transactionOrder;
        this.transactionOrder = 1 + j;
        return j;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Path rerunTransactions(Path changedPath) {
        Tree<List<TransactionData>> rootMostTransactionNode = getAncestorTransactionNode(changedPath);
        Path path = rootMostTransactionNode.getPath();
        List<TransactionData> queue = buildTransactionQueue(rootMostTransactionNode);
        rerunTransactionQueue(queue, path);
        return path;
    }

    private void rerunTransactionQueue(List<TransactionData> queue, Path path) {
        Iterator<TransactionData> it;
        Transaction.Result result;
        if (queue.isEmpty()) {
            return;
        }
        List<Runnable> callbacks = new ArrayList<>();
        List<Long> setsToIgnore = new ArrayList<>();
        Iterator<TransactionData> it2 = queue.iterator();
        while (it2.hasNext()) {
            setsToIgnore.add(Long.valueOf(it2.next().currentWriteId));
        }
        Iterator<TransactionData> it3 = queue.iterator();
        while (it3.hasNext()) {
            final TransactionData transaction = it3.next();
            Path relativePath = Path.getRelative(path, transaction.path);
            boolean abortTransaction = false;
            DatabaseError abortReason = null;
            List<Event> events = new ArrayList<>();
            Utilities.hardAssert(relativePath != null);
            if (transaction.status != TransactionStatus.NEEDS_ABORT) {
                if (transaction.status == TransactionStatus.RUN) {
                    if (transaction.retryCount < 25) {
                        Node currentNode = getLatestState(transaction.path, setsToIgnore);
                        transaction.currentInputSnapshot = currentNode;
                        MutableData mutableCurrent = InternalHelpers.createMutableData(currentNode);
                        DatabaseError error = null;
                        try {
                            result = transaction.handler.doTransaction(mutableCurrent);
                        } catch (Throwable e) {
                            this.operationLogger.error("Caught Throwable.", e);
                            error = DatabaseError.fromException(e);
                            result = Transaction.abort();
                        }
                        if (result.isSuccess()) {
                            Long oldWriteId = Long.valueOf(transaction.currentWriteId);
                            Map<String, Object> serverValues = ServerValues.generateServerValues(this.serverClock);
                            it = it3;
                            Node newDataNode = result.getNode();
                            Node newNodeResolved = ServerValues.resolveDeferredValueSnapshot(newDataNode, currentNode, serverValues);
                            transaction.currentOutputSnapshotRaw = newDataNode;
                            transaction.currentOutputSnapshotResolved = newNodeResolved;
                            transaction.currentWriteId = getNextWriteId();
                            setsToIgnore.remove(oldWriteId);
                            events.addAll(this.serverSyncTree.applyUserOverwrite(transaction.path, newDataNode, newNodeResolved, transaction.currentWriteId, transaction.applyLocally, false));
                            events.addAll(this.serverSyncTree.ackUserWrite(oldWriteId.longValue(), true, false, this.serverClock));
                        } else {
                            it = it3;
                            abortTransaction = true;
                            abortReason = error;
                            events.addAll(this.serverSyncTree.ackUserWrite(transaction.currentWriteId, true, false, this.serverClock));
                        }
                    } else {
                        abortTransaction = true;
                        abortReason = DatabaseError.fromStatus(TRANSACTION_TOO_MANY_RETRIES);
                        events.addAll(this.serverSyncTree.ackUserWrite(transaction.currentWriteId, true, false, this.serverClock));
                        it = it3;
                    }
                } else {
                    it = it3;
                }
            } else {
                abortTransaction = true;
                abortReason = transaction.abortReason;
                if (abortReason.getCode() != -25) {
                    events.addAll(this.serverSyncTree.ackUserWrite(transaction.currentWriteId, true, false, this.serverClock));
                }
                it = it3;
            }
            postEvents(events);
            if (abortTransaction) {
                transaction.status = TransactionStatus.COMPLETED;
                DatabaseReference ref = InternalHelpers.createReference(this, transaction.path);
                Node lastInput = transaction.currentInputSnapshot;
                final DataSnapshot snapshot = InternalHelpers.createDataSnapshot(ref, IndexedNode.from(lastInput));
                scheduleNow(new Runnable() { // from class: com.google.firebase.database.core.Repo.20
                    @Override // java.lang.Runnable
                    public void run() {
                        Repo repo = Repo.this;
                        repo.removeEventCallback(new ValueEventRegistration(repo, transaction.outstandingListener, QuerySpec.defaultQueryAtPath(transaction.path)));
                    }
                });
                final DatabaseError callbackError = abortReason;
                callbacks.add(new Runnable() { // from class: com.google.firebase.database.core.Repo.21
                    @Override // java.lang.Runnable
                    public void run() {
                        transaction.handler.onComplete(callbackError, false, snapshot);
                    }
                });
            }
            it3 = it;
        }
        pruneCompletedTransactions(this.transactionQueueTree);
        for (int i = 0; i < callbacks.size(); i++) {
            postEvent(callbacks.get(i));
        }
        sendAllReadyTransactions();
    }

    private Tree<List<TransactionData>> getAncestorTransactionNode(Path path) {
        Tree<List<TransactionData>> transactionNode = this.transactionQueueTree;
        while (!path.isEmpty() && transactionNode.getValue() == null) {
            transactionNode = transactionNode.subTree(new Path(path.getFront()));
            path = path.popFront();
        }
        return transactionNode;
    }

    private List<TransactionData> buildTransactionQueue(Tree<List<TransactionData>> transactionNode) {
        List<TransactionData> queue = new ArrayList<>();
        aggregateTransactionQueues(queue, transactionNode);
        Collections.sort(queue);
        return queue;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void aggregateTransactionQueues(final List<TransactionData> queue, Tree<List<TransactionData>> node) {
        List<TransactionData> childQueue = node.getValue();
        if (childQueue != null) {
            queue.addAll(childQueue);
        }
        node.forEachChild(new Tree.TreeVisitor<List<TransactionData>>() { // from class: com.google.firebase.database.core.Repo.22
            @Override // com.google.firebase.database.core.utilities.Tree.TreeVisitor
            public void visitTree(Tree<List<TransactionData>> tree) {
                Repo.this.aggregateTransactionQueues(queue, tree);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Path abortTransactions(Path path, final int reason) {
        Path affectedPath = getAncestorTransactionNode(path).getPath();
        if (this.transactionLogger.logsDebug()) {
            this.operationLogger.debug("Aborting transactions for path: " + path + ". Affected: " + affectedPath, new Object[0]);
        }
        Tree<List<TransactionData>> transactionNode = this.transactionQueueTree.subTree(path);
        transactionNode.forEachAncestor(new Tree.TreeFilter<List<TransactionData>>() { // from class: com.google.firebase.database.core.Repo.23
            @Override // com.google.firebase.database.core.utilities.Tree.TreeFilter
            public boolean filterTreeNode(Tree<List<TransactionData>> tree) {
                Repo.this.abortTransactionsAtNode(tree, reason);
                return false;
            }
        });
        abortTransactionsAtNode(transactionNode, reason);
        transactionNode.forEachDescendant(new Tree.TreeVisitor<List<TransactionData>>() { // from class: com.google.firebase.database.core.Repo.24
            @Override // com.google.firebase.database.core.utilities.Tree.TreeVisitor
            public void visitTree(Tree<List<TransactionData>> tree) {
                Repo.this.abortTransactionsAtNode(tree, reason);
            }
        });
        return affectedPath;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void abortTransactionsAtNode(Tree<List<TransactionData>> node, int reason) {
        final DatabaseError abortError;
        List<TransactionData> queue = node.getValue();
        List<Event> events = new ArrayList<>();
        if (queue != null) {
            List<Runnable> callbacks = new ArrayList<>();
            if (reason == -9) {
                abortError = DatabaseError.fromStatus(TRANSACTION_OVERRIDE_BY_SET);
            } else {
                Utilities.hardAssert(reason == -25, "Unknown transaction abort reason: " + reason);
                abortError = DatabaseError.fromCode(-25);
            }
            int lastSent = -1;
            for (int i = 0; i < queue.size(); i++) {
                final TransactionData transaction = queue.get(i);
                if (transaction.status != TransactionStatus.SENT_NEEDS_ABORT) {
                    if (transaction.status == TransactionStatus.SENT) {
                        Utilities.hardAssert(lastSent == i + (-1));
                        int lastSent2 = i;
                        transaction.status = TransactionStatus.SENT_NEEDS_ABORT;
                        transaction.abortReason = abortError;
                        lastSent = lastSent2;
                    } else {
                        Utilities.hardAssert(transaction.status == TransactionStatus.RUN);
                        removeEventCallback(new ValueEventRegistration(this, transaction.outstandingListener, QuerySpec.defaultQueryAtPath(transaction.path)));
                        if (reason == -9) {
                            events.addAll(this.serverSyncTree.ackUserWrite(transaction.currentWriteId, true, false, this.serverClock));
                        } else {
                            Utilities.hardAssert(reason == -25, "Unknown transaction abort reason: " + reason);
                        }
                        callbacks.add(new Runnable() { // from class: com.google.firebase.database.core.Repo.25
                            @Override // java.lang.Runnable
                            public void run() {
                                transaction.handler.onComplete(abortError, false, null);
                            }
                        });
                    }
                }
            }
            if (lastSent == -1) {
                node.setValue(null);
            } else {
                node.setValue(queue.subList(0, lastSent + 1));
            }
            postEvents(events);
            for (Runnable r : callbacks) {
                postEvent(r);
            }
        }
    }

    SyncTree getServerSyncTree() {
        return this.serverSyncTree;
    }

    SyncTree getInfoSyncTree() {
        return this.infoSyncTree;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static DatabaseError fromErrorCode(String optErrorCode, String optErrorReason) {
        if (optErrorCode != null) {
            return DatabaseError.fromStatus(optErrorCode, optErrorReason);
        }
        return null;
    }
}
