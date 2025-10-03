package com.google.firebase.database.core.persistence;

import com.google.firebase.database.core.CompoundWrite;
import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.UserWriteRecord;
import com.google.firebase.database.core.view.CacheNode;
import com.google.firebase.database.core.view.QuerySpec;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.Node;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/* loaded from: classes.dex */
public interface PersistenceManager {
    void applyUserWriteToServerCache(Path path, CompoundWrite compoundWrite);

    void applyUserWriteToServerCache(Path path, Node node);

    List<UserWriteRecord> loadUserWrites();

    void removeAllUserWrites();

    void removeUserWrite(long j);

    <T> T runInTransaction(Callable<T> callable);

    void saveUserMerge(Path path, CompoundWrite compoundWrite, long j);

    void saveUserOverwrite(Path path, Node node, long j);

    CacheNode serverCache(QuerySpec querySpec);

    void setQueryActive(QuerySpec querySpec);

    void setQueryComplete(QuerySpec querySpec);

    void setQueryInactive(QuerySpec querySpec);

    void setTrackedQueryKeys(QuerySpec querySpec, Set<ChildKey> set);

    void updateServerCache(Path path, CompoundWrite compoundWrite);

    void updateServerCache(QuerySpec querySpec, Node node);

    void updateTrackedQueryKeys(QuerySpec querySpec, Set<ChildKey> set, Set<ChildKey> set2);
}
