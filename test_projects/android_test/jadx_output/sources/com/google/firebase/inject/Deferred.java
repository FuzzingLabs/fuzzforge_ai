package com.google.firebase.inject;

/* loaded from: classes.dex */
public interface Deferred<T> {

    /* loaded from: classes.dex */
    public interface DeferredHandler<T> {
        void handle(Provider<T> provider);
    }

    void whenAvailable(DeferredHandler<T> deferredHandler);
}
