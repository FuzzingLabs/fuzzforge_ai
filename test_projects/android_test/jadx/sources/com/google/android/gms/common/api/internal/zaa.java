package com.google.android.gms.common.api.internal;

import android.app.Activity;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public final class zaa extends ActivityLifecycleObserver {
    private final WeakReference<C1710zaa> zaco;

    public zaa(Activity activity) {
        this(C1710zaa.zaa(activity));
    }

    private zaa(C1710zaa c1710zaa) {
        this.zaco = new WeakReference<>(c1710zaa);
    }

    @Override // com.google.android.gms.common.api.internal.ActivityLifecycleObserver
    public final ActivityLifecycleObserver onStopCallOnce(Runnable runnable) {
        C1710zaa c1710zaa = this.zaco.get();
        if (c1710zaa == null) {
            throw new IllegalStateException("The target activity has already been GC'd");
        }
        c1710zaa.zaa(runnable);
        return this;
    }

    /* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
    /* renamed from: com.google.android.gms.common.api.internal.zaa$zaa, reason: collision with other inner class name */
    static class C1710zaa extends LifecycleCallback {
        private List<Runnable> zacn;

        /* JADX INFO: Access modifiers changed from: private */
        public static C1710zaa zaa(Activity activity) {
            C1710zaa c1710zaa;
            synchronized (activity) {
                LifecycleFragment fragment = getFragment(activity);
                c1710zaa = (C1710zaa) fragment.getCallbackOrNull("LifecycleObserverOnStop", C1710zaa.class);
                if (c1710zaa == null) {
                    c1710zaa = new C1710zaa(fragment);
                }
            }
            return c1710zaa;
        }

        private C1710zaa(LifecycleFragment lifecycleFragment) {
            super(lifecycleFragment);
            this.zacn = new ArrayList();
            this.mLifecycleFragment.addCallback("LifecycleObserverOnStop", this);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public final synchronized void zaa(Runnable runnable) {
            this.zacn.add(runnable);
        }

        @Override // com.google.android.gms.common.api.internal.LifecycleCallback
        public void onStop() {
            List<Runnable> list;
            synchronized (this) {
                list = this.zacn;
                this.zacn = new ArrayList();
            }
            Iterator<Runnable> it = list.iterator();
            while (it.hasNext()) {
                it.next().run();
            }
        }
    }
}
