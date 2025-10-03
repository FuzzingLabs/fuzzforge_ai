package com.google.android.gms.common.api.internal;

import android.os.Looper;
import android.os.Message;
import com.google.android.gms.common.internal.Preconditions;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public final class ListenerHolder<L> {
    private final zaa zajm;
    private volatile L zajn;
    private final ListenerKey<L> zajo;

    /* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
    public interface Notifier<L> {
        void notifyListener(L l);

        void onNotifyListenerFailed();
    }

    ListenerHolder(Looper looper, L l, String str) {
        this.zajm = new zaa(looper);
        this.zajn = (L) Preconditions.checkNotNull(l, "Listener must not be null");
        this.zajo = new ListenerKey<>(l, Preconditions.checkNotEmpty(str));
    }

    /* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
    private final class zaa extends com.google.android.gms.internal.base.zar {
        public zaa(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public final void handleMessage(Message message) {
            Preconditions.checkArgument(message.what == 1);
            ListenerHolder.this.notifyListenerInternal((Notifier) message.obj);
        }
    }

    /* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
    public static final class ListenerKey<L> {
        private final L zajn;
        private final String zajp;

        ListenerKey(L l, String str) {
            this.zajn = l;
            this.zajp = str;
        }

        public final boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ListenerKey)) {
                return false;
            }
            ListenerKey listenerKey = (ListenerKey) obj;
            return this.zajn == listenerKey.zajn && this.zajp.equals(listenerKey.zajp);
        }

        public final int hashCode() {
            return (System.identityHashCode(this.zajn) * 31) + this.zajp.hashCode();
        }
    }

    public final void notifyListener(Notifier<? super L> notifier) {
        Preconditions.checkNotNull(notifier, "Notifier must not be null");
        this.zajm.sendMessage(this.zajm.obtainMessage(1, notifier));
    }

    public final boolean hasListener() {
        return this.zajn != null;
    }

    public final void clear() {
        this.zajn = null;
    }

    public final ListenerKey<L> getListenerKey() {
        return this.zajo;
    }

    final void notifyListenerInternal(Notifier<? super L> notifier) {
        L l = this.zajn;
        if (l == null) {
            notifier.onNotifyListenerFailed();
            return;
        }
        try {
            notifier.notifyListener(l);
        } catch (RuntimeException e) {
            notifier.onNotifyListenerFailed();
            throw e;
        }
    }
}
