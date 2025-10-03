package com.google.firebase.database.core.operation;

import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.operation.Operation;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.snapshot.ChildKey;

/* loaded from: classes.dex */
public class ListenComplete extends Operation {
    public ListenComplete(OperationSource source, Path path) {
        super(Operation.OperationType.ListenComplete, source, path);
        Utilities.hardAssert(!source.isFromUser(), "Can't have a listen complete from a user source");
    }

    @Override // com.google.firebase.database.core.operation.Operation
    public Operation operationForChild(ChildKey childKey) {
        if (this.path.isEmpty()) {
            return new ListenComplete(this.source, Path.getEmptyPath());
        }
        return new ListenComplete(this.source, this.path.popFront());
    }

    public String toString() {
        return String.format("ListenComplete { path=%s, source=%s }", getPath(), getSource());
    }
}
