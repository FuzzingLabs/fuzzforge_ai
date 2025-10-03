package com.google.firebase.database;

import com.google.firebase.database.snapshot.Node;

/* loaded from: classes.dex */
public class Transaction {

    /* loaded from: classes.dex */
    public interface Handler {
        Result doTransaction(MutableData mutableData);

        void onComplete(DatabaseError databaseError, boolean z, DataSnapshot dataSnapshot);
    }

    /* loaded from: classes.dex */
    public static class Result {
        private Node data;
        private boolean success;

        private Result(boolean success, Node data) {
            this.success = success;
            this.data = data;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public Node getNode() {
            return this.data;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static Result abort() {
        return new Result(false, null);
    }

    public static Result success(MutableData resultData) {
        return new Result(true, resultData.getNode());
    }
}
