package com.google.firebase.database;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.core.DatabaseConfig;
import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.Repo;
import com.google.firebase.database.core.RepoInfo;
import com.google.firebase.database.snapshot.IndexedNode;
import com.google.firebase.database.snapshot.Node;

/* loaded from: classes.dex */
public class InternalHelpers {
    public static DatabaseReference createReference(Repo repo, Path path) {
        return new DatabaseReference(repo, path);
    }

    public static DataSnapshot createDataSnapshot(DatabaseReference ref, IndexedNode node) {
        return new DataSnapshot(ref, node);
    }

    public static FirebaseDatabase createDatabaseForTests(FirebaseApp app2, RepoInfo repoInfo, DatabaseConfig config) {
        return FirebaseDatabase.createForTests(app2, repoInfo, config);
    }

    public static MutableData createMutableData(Node node) {
        return new MutableData(node);
    }
}
