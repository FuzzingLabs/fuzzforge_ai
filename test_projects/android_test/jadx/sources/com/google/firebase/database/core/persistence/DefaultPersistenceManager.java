package com.google.firebase.database.core.persistence;

import com.google.firebase.database.core.CompoundWrite;
import com.google.firebase.database.core.Context;
import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.UserWriteRecord;
import com.google.firebase.database.core.utilities.Clock;
import com.google.firebase.database.core.utilities.DefaultClock;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.core.view.CacheNode;
import com.google.firebase.database.core.view.QuerySpec;
import com.google.firebase.database.logging.LogWrapper;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.EmptyNode;
import com.google.firebase.database.snapshot.IndexedNode;
import com.google.firebase.database.snapshot.Node;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/* loaded from: classes.dex */
public class DefaultPersistenceManager implements PersistenceManager {
    private final CachePolicy cachePolicy;
    private final LogWrapper logger;
    private long serverCacheUpdatesSinceLastPruneCheck;
    private final PersistenceStorageEngine storageLayer;
    private final TrackedQueryManager trackedQueryManager;

    public DefaultPersistenceManager(Context ctx, PersistenceStorageEngine engine, CachePolicy cachePolicy) {
        this(ctx, engine, cachePolicy, new DefaultClock());
    }

    public DefaultPersistenceManager(Context ctx, PersistenceStorageEngine engine, CachePolicy cachePolicy, Clock clock) {
        this.serverCacheUpdatesSinceLastPruneCheck = 0L;
        this.storageLayer = engine;
        LogWrapper logger = ctx.getLogger("Persistence");
        this.logger = logger;
        this.trackedQueryManager = new TrackedQueryManager(engine, logger, clock);
        this.cachePolicy = cachePolicy;
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public void saveUserOverwrite(Path path, Node node, long writeId) {
        this.storageLayer.saveUserOverwrite(path, node, writeId);
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public void saveUserMerge(Path path, CompoundWrite children, long writeId) {
        this.storageLayer.saveUserMerge(path, children, writeId);
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public void removeUserWrite(long writeId) {
        this.storageLayer.removeUserWrite(writeId);
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public void removeAllUserWrites() {
        this.storageLayer.removeAllUserWrites();
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public void applyUserWriteToServerCache(Path path, Node node) {
        if (!this.trackedQueryManager.hasActiveDefaultQuery(path)) {
            this.storageLayer.overwriteServerCache(path, node);
            this.trackedQueryManager.ensureCompleteTrackedQuery(path);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public void applyUserWriteToServerCache(Path path, CompoundWrite merge) {
        Iterator<Map.Entry<Path, Node>> it = merge.iterator();
        while (it.hasNext()) {
            Map.Entry<Path, Node> write = it.next();
            Path writePath = path.child(write.getKey());
            Node writeNode = write.getValue();
            applyUserWriteToServerCache(writePath, writeNode);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public List<UserWriteRecord> loadUserWrites() {
        return this.storageLayer.loadUserWrites();
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public CacheNode serverCache(QuerySpec query) {
        boolean complete;
        Set<ChildKey> trackedKeys;
        if (this.trackedQueryManager.isQueryComplete(query)) {
            complete = true;
            TrackedQuery trackedQuery = this.trackedQueryManager.findTrackedQuery(query);
            if (!query.loadsAllData() && trackedQuery != null && trackedQuery.complete) {
                trackedKeys = this.storageLayer.loadTrackedQueryKeys(trackedQuery.f247id);
            } else {
                trackedKeys = null;
            }
        } else {
            complete = false;
            trackedKeys = this.trackedQueryManager.getKnownCompleteChildren(query.getPath());
        }
        Node serverCacheNode = this.storageLayer.serverCache(query.getPath());
        if (trackedKeys != null) {
            Node filteredNode = EmptyNode.Empty();
            for (ChildKey key : trackedKeys) {
                filteredNode = filteredNode.updateImmediateChild(key, serverCacheNode.getImmediateChild(key));
            }
            return new CacheNode(IndexedNode.from(filteredNode, query.getIndex()), complete, true);
        }
        return new CacheNode(IndexedNode.from(serverCacheNode, query.getIndex()), complete, false);
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public void updateServerCache(QuerySpec query, Node node) {
        if (query.loadsAllData()) {
            this.storageLayer.overwriteServerCache(query.getPath(), node);
        } else {
            this.storageLayer.mergeIntoServerCache(query.getPath(), node);
        }
        setQueryComplete(query);
        doPruneCheckAfterServerUpdate();
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public void updateServerCache(Path path, CompoundWrite children) {
        this.storageLayer.mergeIntoServerCache(path, children);
        doPruneCheckAfterServerUpdate();
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public void setQueryActive(QuerySpec query) {
        this.trackedQueryManager.setQueryActive(query);
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public void setQueryInactive(QuerySpec query) {
        this.trackedQueryManager.setQueryInactive(query);
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public void setQueryComplete(QuerySpec query) {
        if (query.loadsAllData()) {
            this.trackedQueryManager.setQueriesComplete(query.getPath());
        } else {
            this.trackedQueryManager.setQueryCompleteIfExists(query);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public void setTrackedQueryKeys(QuerySpec query, Set<ChildKey> keys) {
        Utilities.hardAssert(!query.loadsAllData(), "We should only track keys for filtered queries.");
        TrackedQuery trackedQuery = this.trackedQueryManager.findTrackedQuery(query);
        Utilities.hardAssert(trackedQuery != null && trackedQuery.active, "We only expect tracked keys for currently-active queries.");
        this.storageLayer.saveTrackedQueryKeys(trackedQuery.f247id, keys);
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public void updateTrackedQueryKeys(QuerySpec query, Set<ChildKey> added, Set<ChildKey> removed) {
        Utilities.hardAssert(!query.loadsAllData(), "We should only track keys for filtered queries.");
        TrackedQuery trackedQuery = this.trackedQueryManager.findTrackedQuery(query);
        Utilities.hardAssert(trackedQuery != null && trackedQuery.active, "We only expect tracked keys for currently-active queries.");
        this.storageLayer.updateTrackedQueryKeys(trackedQuery.f247id, added, removed);
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceManager
    public <T> T runInTransaction(Callable<T> callable) {
        this.storageLayer.beginTransaction();
        try {
            T result = callable.call();
            this.storageLayer.setTransactionSuccessful();
            return result;
        } finally {
        }
    }

    private void doPruneCheckAfterServerUpdate() {
        long j = this.serverCacheUpdatesSinceLastPruneCheck + 1;
        this.serverCacheUpdatesSinceLastPruneCheck = j;
        if (this.cachePolicy.shouldCheckCacheSize(j)) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Reached prune check threshold.", new Object[0]);
            }
            this.serverCacheUpdatesSinceLastPruneCheck = 0L;
            boolean canPrune = true;
            long cacheSize = this.storageLayer.serverCacheEstimatedSizeInBytes();
            if (this.logger.logsDebug()) {
                this.logger.debug("Cache size: " + cacheSize, new Object[0]);
            }
            while (canPrune && this.cachePolicy.shouldPrune(cacheSize, this.trackedQueryManager.countOfPrunableQueries())) {
                PruneForest pruneForest = this.trackedQueryManager.pruneOldQueries(this.cachePolicy);
                if (pruneForest.prunesAnything()) {
                    this.storageLayer.pruneCache(Path.getEmptyPath(), pruneForest);
                } else {
                    canPrune = false;
                }
                cacheSize = this.storageLayer.serverCacheEstimatedSizeInBytes();
                if (this.logger.logsDebug()) {
                    this.logger.debug("Cache size after prune: " + cacheSize, new Object[0]);
                }
            }
        }
    }
}
