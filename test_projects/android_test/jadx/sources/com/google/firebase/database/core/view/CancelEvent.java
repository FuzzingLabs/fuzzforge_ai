package com.google.firebase.database.core.view;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.core.EventRegistration;
import com.google.firebase.database.core.Path;

/* loaded from: classes.dex */
public class CancelEvent implements Event {
    private final DatabaseError error;
    private final EventRegistration eventRegistration;
    private final Path path;

    public CancelEvent(EventRegistration eventRegistration, DatabaseError error, Path path) {
        this.eventRegistration = eventRegistration;
        this.path = path;
        this.error = error;
    }

    @Override // com.google.firebase.database.core.view.Event
    public Path getPath() {
        return this.path;
    }

    @Override // com.google.firebase.database.core.view.Event
    public void fire() {
        this.eventRegistration.fireCancelEvent(this.error);
    }

    @Override // com.google.firebase.database.core.view.Event
    public String toString() {
        return getPath() + ":CANCEL";
    }
}
