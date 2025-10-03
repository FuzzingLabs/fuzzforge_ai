package com.google.firebase.database;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.core.CompoundWrite;
import com.google.firebase.database.core.DatabaseConfig;
import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.Repo;
import com.google.firebase.database.core.RepoManager;
import com.google.firebase.database.core.ValidationPath;
import com.google.firebase.database.core.utilities.Pair;
import com.google.firebase.database.core.utilities.ParsedUrl;
import com.google.firebase.database.core.utilities.PushIdGenerator;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.core.utilities.Validation;
import com.google.firebase.database.core.utilities.encoding.CustomClassMapper;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.Node;
import com.google.firebase.database.snapshot.NodeUtilities;
import com.google.firebase.database.snapshot.PriorityUtilities;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/* loaded from: classes.dex */
public class DatabaseReference extends Query {
    private static DatabaseConfig defaultConfig;

    public interface CompletionListener {
        void onComplete(DatabaseError databaseError, DatabaseReference databaseReference);
    }

    DatabaseReference(Repo repo, Path path) {
        super(repo, path);
    }

    DatabaseReference(String url, DatabaseConfig config) {
        this(Utilities.parseUrl(url), config);
    }

    private DatabaseReference(ParsedUrl parsedUrl, DatabaseConfig config) {
        this(RepoManager.getRepo(config, parsedUrl.repoInfo), parsedUrl.path);
    }

    public DatabaseReference child(String pathString) {
        if (pathString == null) {
            throw new NullPointerException("Can't pass null for argument 'pathString' in child()");
        }
        if (getPath().isEmpty()) {
            Validation.validateRootPathString(pathString);
        } else {
            Validation.validatePathString(pathString);
        }
        Path childPath = getPath().child(new Path(pathString));
        return new DatabaseReference(this.repo, childPath);
    }

    public DatabaseReference push() {
        String childNameStr = PushIdGenerator.generatePushChildName(this.repo.getServerTime());
        ChildKey childKey = ChildKey.fromString(childNameStr);
        return new DatabaseReference(this.repo, getPath().child(childKey));
    }

    public Task<Void> setValue(Object value) {
        return setValueInternal(value, PriorityUtilities.parsePriority(this.path, null), null);
    }

    public Task<Void> setValue(Object value, Object priority) {
        return setValueInternal(value, PriorityUtilities.parsePriority(this.path, priority), null);
    }

    public void setValue(Object value, CompletionListener listener) {
        setValueInternal(value, PriorityUtilities.parsePriority(this.path, null), listener);
    }

    public void setValue(Object value, Object priority, CompletionListener listener) {
        setValueInternal(value, PriorityUtilities.parsePriority(this.path, priority), listener);
    }

    private Task<Void> setValueInternal(Object value, Node priority, CompletionListener optListener) {
        Validation.validateWritablePath(getPath());
        ValidationPath.validateWithObject(getPath(), value);
        Object bouncedValue = CustomClassMapper.convertToPlainJavaTypes(value);
        Validation.validateWritableObject(bouncedValue);
        final Node node = NodeUtilities.NodeFromJSON(bouncedValue, priority);
        final Pair<Task<Void>, CompletionListener> wrapped = Utilities.wrapOnComplete(optListener);
        this.repo.scheduleNow(new Runnable() { // from class: com.google.firebase.database.DatabaseReference.1
            @Override // java.lang.Runnable
            public void run() {
                DatabaseReference.this.repo.setValue(DatabaseReference.this.getPath(), node, (CompletionListener) wrapped.getSecond());
            }
        });
        return wrapped.getFirst();
    }

    public Task<Void> setPriority(Object priority) {
        return setPriorityInternal(PriorityUtilities.parsePriority(this.path, priority), null);
    }

    public void setPriority(Object priority, CompletionListener listener) {
        setPriorityInternal(PriorityUtilities.parsePriority(this.path, priority), listener);
    }

    private Task<Void> setPriorityInternal(final Node priority, CompletionListener optListener) {
        Validation.validateWritablePath(getPath());
        final Pair<Task<Void>, CompletionListener> wrapped = Utilities.wrapOnComplete(optListener);
        this.repo.scheduleNow(new Runnable() { // from class: com.google.firebase.database.DatabaseReference.2
            @Override // java.lang.Runnable
            public void run() {
                DatabaseReference.this.repo.setValue(DatabaseReference.this.getPath().child(ChildKey.getPriorityKey()), priority, (CompletionListener) wrapped.getSecond());
            }
        });
        return wrapped.getFirst();
    }

    public Task<Void> updateChildren(Map<String, Object> update) {
        return updateChildrenInternal(update, null);
    }

    public void updateChildren(Map<String, Object> update, CompletionListener listener) {
        updateChildrenInternal(update, listener);
    }

    private Task<Void> updateChildrenInternal(Map<String, Object> update, CompletionListener optListener) {
        if (update == null) {
            throw new NullPointerException("Can't pass null for argument 'update' in updateChildren()");
        }
        final Map<String, Object> bouncedUpdate = CustomClassMapper.convertToPlainJavaTypes(update);
        Map<Path, Node> parsedUpdate = Validation.parseAndValidateUpdate(getPath(), bouncedUpdate);
        final CompoundWrite merge = CompoundWrite.fromPathMerge(parsedUpdate);
        final Pair<Task<Void>, CompletionListener> wrapped = Utilities.wrapOnComplete(optListener);
        this.repo.scheduleNow(new Runnable() { // from class: com.google.firebase.database.DatabaseReference.3
            @Override // java.lang.Runnable
            public void run() {
                DatabaseReference.this.repo.updateChildren(DatabaseReference.this.getPath(), merge, (CompletionListener) wrapped.getSecond(), bouncedUpdate);
            }
        });
        return wrapped.getFirst();
    }

    public Task<Void> removeValue() {
        return setValue(null);
    }

    public void removeValue(CompletionListener listener) {
        setValue((Object) null, listener);
    }

    public OnDisconnect onDisconnect() {
        Validation.validateWritablePath(getPath());
        return new OnDisconnect(this.repo, getPath());
    }

    public void runTransaction(Transaction.Handler handler) {
        runTransaction(handler, true);
    }

    public void runTransaction(final Transaction.Handler handler, final boolean fireLocalEvents) {
        if (handler == null) {
            throw new NullPointerException("Can't pass null for argument 'handler' in runTransaction()");
        }
        Validation.validateWritablePath(getPath());
        this.repo.scheduleNow(new Runnable() { // from class: com.google.firebase.database.DatabaseReference.4
            @Override // java.lang.Runnable
            public void run() {
                DatabaseReference.this.repo.startTransaction(DatabaseReference.this.getPath(), handler, fireLocalEvents);
            }
        });
    }

    public static void goOffline() {
        goOffline(getDefaultConfig());
    }

    static void goOffline(DatabaseConfig config) {
        RepoManager.interrupt(config);
    }

    public static void goOnline() {
        goOnline(getDefaultConfig());
    }

    static void goOnline(DatabaseConfig config) {
        RepoManager.resume(config);
    }

    public FirebaseDatabase getDatabase() {
        return this.repo.getDatabase();
    }

    public String toString() {
        DatabaseReference parent = getParent();
        if (parent == null) {
            return this.repo.toString();
        }
        try {
            return parent.toString() + "/" + URLEncoder.encode(getKey(), "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new DatabaseException("Failed to URLEncode key: " + getKey(), e);
        }
    }

    public DatabaseReference getParent() {
        Path parentPath = getPath().getParent();
        if (parentPath != null) {
            return new DatabaseReference(this.repo, parentPath);
        }
        return null;
    }

    public DatabaseReference getRoot() {
        return new DatabaseReference(this.repo, new Path(""));
    }

    public String getKey() {
        if (getPath().isEmpty()) {
            return null;
        }
        return getPath().getBack().asString();
    }

    public boolean equals(Object other) {
        return (other instanceof DatabaseReference) && toString().equals(other.toString());
    }

    public int hashCode() {
        return toString().hashCode();
    }

    void setHijackHash(final boolean hijackHash) {
        this.repo.scheduleNow(new Runnable() { // from class: com.google.firebase.database.DatabaseReference.5
            @Override // java.lang.Runnable
            public void run() {
                DatabaseReference.this.repo.setHijackHash(hijackHash);
            }
        });
    }

    private static synchronized DatabaseConfig getDefaultConfig() {
        DatabaseConfig databaseConfig;
        synchronized (DatabaseReference.class) {
            if (defaultConfig == null) {
                defaultConfig = new DatabaseConfig();
            }
            databaseConfig = defaultConfig;
        }
        return databaseConfig;
    }
}
