package com.google.android.gms.common.api.internal;

import android.os.Bundle;

/* loaded from: classes.dex */
final class zzd implements Runnable {
    private final /* synthetic */ LifecycleCallback zzbi;
    private final /* synthetic */ String zzbj;
    private final /* synthetic */ zzc zzbl;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zzd(zzc zzcVar, LifecycleCallback lifecycleCallback, String str) {
        this.zzbl = zzcVar;
        this.zzbi = lifecycleCallback;
        this.zzbj = str;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        Bundle bundle;
        Bundle bundle2;
        Bundle bundle3;
        i = this.zzbl.zzbg;
        if (i > 0) {
            LifecycleCallback lifecycleCallback = this.zzbi;
            bundle = this.zzbl.zzbh;
            if (bundle != null) {
                bundle3 = this.zzbl.zzbh;
                bundle2 = bundle3.getBundle(this.zzbj);
            } else {
                bundle2 = null;
            }
            lifecycleCallback.onCreate(bundle2);
        }
        i2 = this.zzbl.zzbg;
        if (i2 >= 2) {
            this.zzbi.onStart();
        }
        i3 = this.zzbl.zzbg;
        if (i3 >= 3) {
            this.zzbi.onResume();
        }
        i4 = this.zzbl.zzbg;
        if (i4 >= 4) {
            this.zzbi.onStop();
        }
        i5 = this.zzbl.zzbg;
        if (i5 >= 5) {
            this.zzbi.onDestroy();
        }
    }
}
