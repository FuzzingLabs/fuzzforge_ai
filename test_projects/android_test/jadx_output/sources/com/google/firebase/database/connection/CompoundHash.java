package com.google.firebase.database.connection;

import java.util.Collections;
import java.util.List;

/* loaded from: classes.dex */
public class CompoundHash {
    private final List<String> hashes;
    private final List<List<String>> posts;

    public CompoundHash(List<List<String>> posts, List<String> hashes) {
        if (posts.size() != hashes.size() - 1) {
            throw new IllegalArgumentException("Number of posts need to be n-1 for n hashes in CompoundHash");
        }
        this.posts = posts;
        this.hashes = hashes;
    }

    public List<List<String>> getPosts() {
        return Collections.unmodifiableList(this.posts);
    }

    public List<String> getHashes() {
        return Collections.unmodifiableList(this.hashes);
    }
}
