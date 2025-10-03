package com.google.firebase.database;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.Repo;
import com.google.firebase.database.core.ValidationPath;
import com.google.firebase.database.core.utilities.Pair;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.core.utilities.Validation;
import com.google.firebase.database.core.utilities.encoding.CustomClassMapper;
import com.google.firebase.database.snapshot.Node;
import com.google.firebase.database.snapshot.NodeUtilities;
import com.google.firebase.database.snapshot.PriorityUtilities;
import java.util.Map;

/* loaded from: classes.dex */
public class OnDisconnect {
    private Path path;
    private Repo repo;

    /* JADX INFO: Access modifiers changed from: package-private */
    public OnDisconnect(Repo repo, Path path) {
        this.repo = repo;
        this.path = path;
    }

    public Task<Void> setValue(Object value) {
        return onDisconnectSetInternal(value, PriorityUtilities.NullPriority(), null);
    }

    public Task<Void> setValue(Object value, String priority) {
        return onDisconnectSetInternal(value, PriorityUtilities.parsePriority(this.path, priority), null);
    }

    public Task<Void> setValue(Object value, double priority) {
        return onDisconnectSetInternal(value, PriorityUtilities.parsePriority(this.path, Double.valueOf(priority)), null);
    }

    public void setValue(Object value, DatabaseReference.CompletionListener listener) {
        onDisconnectSetInternal(value, PriorityUtilities.NullPriority(), listener);
    }

    public void setValue(Object value, String priority, DatabaseReference.CompletionListener listener) {
        onDisconnectSetInternal(value, PriorityUtilities.parsePriority(this.path, priority), listener);
    }

    public void setValue(Object value, double priority, DatabaseReference.CompletionListener listener) {
        onDisconnectSetInternal(value, PriorityUtilities.parsePriority(this.path, Double.valueOf(priority)), listener);
    }

    public void setValue(Object value, Map priority, DatabaseReference.CompletionListener listener) {
        onDisconnectSetInternal(value, PriorityUtilities.parsePriority(this.path, priority), listener);
    }

    private Task<Void> onDisconnectSetInternal(Object value, Node priority, DatabaseReference.CompletionListener optListener) {
        Validation.validateWritablePath(this.path);
        ValidationPath.validateWithObject(this.path, value);
        Object bouncedValue = CustomClassMapper.convertToPlainJavaTypes(value);
        Validation.validateWritableObject(bouncedValue);
        final Node node = NodeUtilities.NodeFromJSON(bouncedValue, priority);
        final Pair<Task<Void>, DatabaseReference.CompletionListener> wrapped = Utilities.wrapOnComplete(optListener);
        this.repo.scheduleNow(new Runnable() { // from class: com.google.firebase.database.OnDisconnect.1
            @Override // java.lang.Runnable
            public void run() {
                OnDisconnect.this.repo.onDisconnectSetValue(OnDisconnect.this.path, node, (DatabaseReference.CompletionListener) wrapped.getSecond());
            }
        });
        return wrapped.getFirst();
    }

    public Task<Void> updateChildren(Map<String, Object> update) {
        return updateChildrenInternal(update, null);
    }

    public void updateChildren(Map<String, Object> update, DatabaseReference.CompletionListener listener) {
        updateChildrenInternal(update, listener);
    }

    private Task<Void> updateChildrenInternal(final Map<String, Object> update, DatabaseReference.CompletionListener optListener) {
        final Map<Path, Node> parsedUpdate = Validation.parseAndValidateUpdate(this.path, update);
        final Pair<Task<Void>, DatabaseReference.CompletionListener> wrapped = Utilities.wrapOnComplete(optListener);
        this.repo.scheduleNow(new Runnable() { // from class: com.google.firebase.database.OnDisconnect.2
            @Override // java.lang.Runnable
            public void run() {
                OnDisconnect.this.repo.onDisconnectUpdate(OnDisconnect.this.path, parsedUpdate, (DatabaseReference.CompletionListener) wrapped.getSecond(), update);
            }
        });
        return wrapped.getFirst();
    }

    public Task<Void> removeValue() {
        return setValue(null);
    }

    public void removeValue(DatabaseReference.CompletionListener listener) {
        setValue((Object) null, listener);
    }

    public Task<Void> cancel() {
        return cancelInternal(null);
    }

    public void cancel(DatabaseReference.CompletionListener listener) {
        cancelInternal(listener);
    }

    private Task<Void> cancelInternal(DatabaseReference.CompletionListener optListener) {
        final Pair<Task<Void>, DatabaseReference.CompletionListener> wrapped = Utilities.wrapOnComplete(optListener);
        this.repo.scheduleNow(new Runnable() { // from class: com.google.firebase.database.OnDisconnect.3
            @Override // java.lang.Runnable
            public void run() {
                OnDisconnect.this.repo.onDisconnectCancel(OnDisconnect.this.path, (DatabaseReference.CompletionListener) wrapped.getSecond());
            }
        });
        return wrapped.getFirst();
    }
}
