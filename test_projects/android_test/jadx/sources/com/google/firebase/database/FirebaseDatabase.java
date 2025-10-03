package com.google.firebase.database;

import android.text.TextUtils;
import com.google.android.gms.common.internal.Preconditions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.Logger;
import com.google.firebase.database.core.DatabaseConfig;
import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.Repo;
import com.google.firebase.database.core.RepoInfo;
import com.google.firebase.database.core.RepoManager;
import com.google.firebase.database.core.utilities.ParsedUrl;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.core.utilities.Validation;
import com.google.firebase.emulators.EmulatedServiceSettings;

/* loaded from: classes.dex */
public class FirebaseDatabase {
    private static final String SDK_VERSION = "20.0.3";

    /* renamed from: app, reason: collision with root package name */
    private final FirebaseApp f294app;
    private final DatabaseConfig config;
    private EmulatedServiceSettings emulatorSettings;
    private Repo repo;
    private final RepoInfo repoInfo;

    public static FirebaseDatabase getInstance() {
        FirebaseApp instance = FirebaseApp.getInstance();
        if (instance == null) {
            throw new DatabaseException("You must call FirebaseApp.initialize() first.");
        }
        return getInstance(instance);
    }

    public static FirebaseDatabase getInstance(String url) {
        FirebaseApp instance = FirebaseApp.getInstance();
        if (instance == null) {
            throw new DatabaseException("You must call FirebaseApp.initialize() first.");
        }
        return getInstance(instance, url);
    }

    public static FirebaseDatabase getInstance(FirebaseApp app2) {
        String databaseUrl = app2.getOptions().getDatabaseUrl();
        if (databaseUrl == null) {
            if (app2.getOptions().getProjectId() == null) {
                throw new DatabaseException("Failed to get FirebaseDatabase instance: Can't determine Firebase Database URL. Be sure to include a Project ID in your configuration.");
            }
            databaseUrl = "https://" + app2.getOptions().getProjectId() + "-default-rtdb.firebaseio.com";
        }
        return getInstance(app2, databaseUrl);
    }

    public static synchronized FirebaseDatabase getInstance(FirebaseApp app2, String url) {
        FirebaseDatabase firebaseDatabase;
        synchronized (FirebaseDatabase.class) {
            if (TextUtils.isEmpty(url)) {
                throw new DatabaseException("Failed to get FirebaseDatabase instance: Specify DatabaseURL within FirebaseApp or from your getInstance() call.");
            }
            Preconditions.checkNotNull(app2, "Provided FirebaseApp must not be null.");
            FirebaseDatabaseComponent component = (FirebaseDatabaseComponent) app2.get(FirebaseDatabaseComponent.class);
            Preconditions.checkNotNull(component, "Firebase Database component is not present.");
            ParsedUrl parsedUrl = Utilities.parseUrl(url);
            if (!parsedUrl.path.isEmpty()) {
                throw new DatabaseException("Specified Database URL '" + url + "' is invalid. It should point to the root of a Firebase Database but it includes a path: " + parsedUrl.path.toString());
            }
            firebaseDatabase = component.get(parsedUrl.repoInfo);
        }
        return firebaseDatabase;
    }

    static FirebaseDatabase createForTests(FirebaseApp app2, RepoInfo repoInfo, DatabaseConfig config) {
        FirebaseDatabase db = new FirebaseDatabase(app2, repoInfo, config);
        db.ensureRepo();
        return db;
    }

    FirebaseDatabase(FirebaseApp app2, RepoInfo repoInfo, DatabaseConfig config) {
        this.f294app = app2;
        this.repoInfo = repoInfo;
        this.config = config;
    }

    public FirebaseApp getApp() {
        return this.f294app;
    }

    public DatabaseReference getReference() {
        ensureRepo();
        return new DatabaseReference(this.repo, Path.getEmptyPath());
    }

    public DatabaseReference getReference(String path) {
        ensureRepo();
        if (path == null) {
            throw new NullPointerException("Can't pass null for argument 'pathString' in FirebaseDatabase.getReference()");
        }
        Validation.validateRootPathString(path);
        Path childPath = new Path(path);
        return new DatabaseReference(this.repo, childPath);
    }

    public DatabaseReference getReferenceFromUrl(String url) {
        ensureRepo();
        if (url == null) {
            throw new NullPointerException("Can't pass null for argument 'url' in FirebaseDatabase.getReferenceFromUrl()");
        }
        ParsedUrl parsedUrl = Utilities.parseUrl(url);
        parsedUrl.repoInfo.applyEmulatorSettings(this.emulatorSettings);
        if (!parsedUrl.repoInfo.host.equals(this.repo.getRepoInfo().host)) {
            throw new DatabaseException("Invalid URL (" + url + ") passed to getReference().  URL was expected to match configured Database URL: " + getReference().toString());
        }
        return new DatabaseReference(this.repo, parsedUrl.path);
    }

    public void purgeOutstandingWrites() {
        ensureRepo();
        this.repo.scheduleNow(new Runnable() { // from class: com.google.firebase.database.FirebaseDatabase.1
            @Override // java.lang.Runnable
            public void run() {
                FirebaseDatabase.this.repo.purgeOutstandingWrites();
            }
        });
    }

    public void goOnline() {
        ensureRepo();
        RepoManager.resume(this.repo);
    }

    public void goOffline() {
        ensureRepo();
        RepoManager.interrupt(this.repo);
    }

    public synchronized void setLogLevel(Logger.Level logLevel) {
        assertUnfrozen("setLogLevel");
        this.config.setLogLevel(logLevel);
    }

    public synchronized void setPersistenceEnabled(boolean isEnabled) {
        assertUnfrozen("setPersistenceEnabled");
        this.config.setPersistenceEnabled(isEnabled);
    }

    public synchronized void setPersistenceCacheSizeBytes(long cacheSizeInBytes) {
        assertUnfrozen("setPersistenceCacheSizeBytes");
        this.config.setPersistenceCacheSizeBytes(cacheSizeInBytes);
    }

    public void useEmulator(String host, int port) {
        if (this.repo != null) {
            throw new IllegalStateException("Cannot call useEmulator() after instance has already been initialized.");
        }
        this.emulatorSettings = new EmulatedServiceSettings(host, port);
    }

    public static String getSdkVersion() {
        return "20.0.3";
    }

    private void assertUnfrozen(String methodCalled) {
        if (this.repo != null) {
            throw new DatabaseException("Calls to " + methodCalled + "() must be made before any other usage of FirebaseDatabase instance.");
        }
    }

    private synchronized void ensureRepo() {
        if (this.repo == null) {
            this.repoInfo.applyEmulatorSettings(this.emulatorSettings);
            this.repo = RepoManager.createRepo(this.config, this.repoInfo, this);
        }
    }

    DatabaseConfig getConfig() {
        return this.config;
    }
}
