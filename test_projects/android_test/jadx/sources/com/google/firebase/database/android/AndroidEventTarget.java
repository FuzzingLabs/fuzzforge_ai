package com.google.firebase.database.android;

import android.os.Handler;
import android.os.Looper;
import com.google.firebase.database.core.EventTarget;

/* loaded from: classes.dex */
public class AndroidEventTarget implements EventTarget {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override // com.google.firebase.database.core.EventTarget
    public void postEvent(Runnable r) {
        this.handler.post(r);
    }

    @Override // com.google.firebase.database.core.EventTarget
    public void shutdown() {
    }

    @Override // com.google.firebase.database.core.EventTarget
    public void restart() {
    }
}
