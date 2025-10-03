package com.google.firebase.database.core;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.InternalHelpers;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class RepoManager {
    private static final RepoManager instance = new RepoManager();
    private final Map<Context, Map<String, Repo>> repos = new HashMap();

    public static Repo getRepo(Context ctx, RepoInfo info) throws DatabaseException {
        return instance.getLocalRepo(ctx, info);
    }

    public static Repo createRepo(Context ctx, RepoInfo info, FirebaseDatabase database) throws DatabaseException {
        return instance.createLocalRepo(ctx, info, database);
    }

    public static void interrupt(Context ctx) {
        instance.interruptInternal(ctx);
    }

    public static void interrupt(final Repo repo) {
        repo.scheduleNow(new Runnable() { // from class: com.google.firebase.database.core.RepoManager.1
            @Override // java.lang.Runnable
            public void run() {
                Repo.this.interrupt();
            }
        });
    }

    public static void resume(final Repo repo) {
        repo.scheduleNow(new Runnable() { // from class: com.google.firebase.database.core.RepoManager.2
            @Override // java.lang.Runnable
            public void run() {
                Repo.this.resume();
            }
        });
    }

    public static void resume(Context ctx) {
        instance.resumeInternal(ctx);
    }

    public static void clear() {
        instance.clearRepos();
    }

    private void clearRepos() {
        synchronized (this.repos) {
            this.repos.clear();
        }
    }

    private Repo getLocalRepo(Context ctx, RepoInfo info) throws DatabaseException {
        Repo repo;
        ctx.freeze();
        String repoHash = "https://" + info.host + "/" + info.namespace;
        synchronized (this.repos) {
            if (!this.repos.containsKey(ctx) || !this.repos.get(ctx).containsKey(repoHash)) {
                InternalHelpers.createDatabaseForTests(FirebaseApp.getInstance(), info, (DatabaseConfig) ctx);
            }
            repo = this.repos.get(ctx).get(repoHash);
        }
        return repo;
    }

    private Repo createLocalRepo(Context ctx, RepoInfo info, FirebaseDatabase database) throws DatabaseException {
        Repo repo;
        ctx.freeze();
        String repoHash = "https://" + info.host + "/" + info.namespace;
        synchronized (this.repos) {
            if (!this.repos.containsKey(ctx)) {
                this.repos.put(ctx, new HashMap<>());
            }
            Map<String, Repo> innerMap = this.repos.get(ctx);
            if (!innerMap.containsKey(repoHash)) {
                repo = new Repo(info, ctx, database);
                innerMap.put(repoHash, repo);
            } else {
                throw new IllegalStateException("createLocalRepo() called for existing repo.");
            }
        }
        return repo;
    }

    private void interruptInternal(final Context ctx) {
        RunLoop runLoop = ctx.getRunLoop();
        if (runLoop != null) {
            runLoop.scheduleNow(new Runnable() { // from class: com.google.firebase.database.core.RepoManager.3
                @Override // java.lang.Runnable
                public void run() {
                    synchronized (RepoManager.this.repos) {
                        boolean allEmpty = true;
                        if (RepoManager.this.repos.containsKey(ctx)) {
                            for (Repo repo : ((Map) RepoManager.this.repos.get(ctx)).values()) {
                                repo.interrupt();
                                allEmpty = allEmpty && !repo.hasListeners();
                            }
                            if (allEmpty) {
                                ctx.stop();
                            }
                        }
                    }
                }
            });
        }
    }

    private void resumeInternal(final Context ctx) {
        RunLoop runLoop = ctx.getRunLoop();
        if (runLoop != null) {
            runLoop.scheduleNow(new Runnable() { // from class: com.google.firebase.database.core.RepoManager.4
                @Override // java.lang.Runnable
                public void run() {
                    synchronized (RepoManager.this.repos) {
                        if (RepoManager.this.repos.containsKey(ctx)) {
                            for (Repo repo : ((Map) RepoManager.this.repos.get(ctx)).values()) {
                                repo.resume();
                            }
                        }
                    }
                }
            });
        }
    }
}
