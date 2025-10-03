package com.google.firebase.database.core;

import com.google.firebase.database.core.view.CacheNode;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.Index;
import com.google.firebase.database.snapshot.NamedNode;
import com.google.firebase.database.snapshot.Node;
import java.util.Collections;
import java.util.List;

/* loaded from: classes.dex */
public class WriteTreeRef {
    private final Path treePath;
    private final WriteTree writeTree;

    public WriteTreeRef(Path path, WriteTree writeTree) {
        this.treePath = path;
        this.writeTree = writeTree;
    }

    public Node calcCompleteEventCache(Node completeServerCache) {
        return calcCompleteEventCache(completeServerCache, Collections.emptyList());
    }

    public Node calcCompleteEventCache(Node completeServerCache, List<Long> writeIdsToExclude) {
        return calcCompleteEventCache(completeServerCache, writeIdsToExclude, false);
    }

    public Node calcCompleteEventCache(Node completeServerCache, List<Long> writeIdsToExclude, boolean includeHiddenWrites) {
        return this.writeTree.calcCompleteEventCache(this.treePath, completeServerCache, writeIdsToExclude, includeHiddenWrites);
    }

    public Node calcCompleteEventChildren(Node completeServerChildren) {
        return this.writeTree.calcCompleteEventChildren(this.treePath, completeServerChildren);
    }

    public Node calcEventCacheAfterServerOverwrite(Path path, Node existingEventSnap, Node existingServerSnap) {
        return this.writeTree.calcEventCacheAfterServerOverwrite(this.treePath, path, existingEventSnap, existingServerSnap);
    }

    public Node shadowingWrite(Path path) {
        return this.writeTree.shadowingWrite(this.treePath.child(path));
    }

    public NamedNode calcNextNodeAfterPost(Node completeServerData, NamedNode startPost, boolean reverse, Index index) {
        return this.writeTree.calcNextNodeAfterPost(this.treePath, completeServerData, startPost, reverse, index);
    }

    public Node calcCompleteChild(ChildKey childKey, CacheNode existingServerCache) {
        return this.writeTree.calcCompleteChild(this.treePath, childKey, existingServerCache);
    }

    public WriteTreeRef child(ChildKey childKey) {
        return new WriteTreeRef(this.treePath.child(childKey), this.writeTree);
    }
}
