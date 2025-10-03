package com.google.android.gms.common.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

/* loaded from: classes.dex */
public abstract class GmsClientSupervisor {
    private static final Object zzdp = new Object();
    private static GmsClientSupervisor zzdq;

    /* JADX INFO: Access modifiers changed from: protected */
    public abstract boolean zza(zza zzaVar, ServiceConnection serviceConnection, String str);

    protected abstract void zzb(zza zzaVar, ServiceConnection serviceConnection, String str);

    public static GmsClientSupervisor getInstance(Context context) {
        synchronized (zzdp) {
            if (zzdq == null) {
                zzdq = new zze(context.getApplicationContext());
            }
        }
        return zzdq;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* loaded from: classes.dex */
    public static final class zza {
        private final ComponentName mComponentName;
        private final String zzdr;
        private final String zzds;
        private final int zzdt;

        public zza(String str, int i) {
            this.zzdr = Preconditions.checkNotEmpty(str);
            this.zzds = "com.google.android.gms";
            this.mComponentName = null;
            this.zzdt = 129;
        }

        public zza(String str, String str2, int i) {
            this.zzdr = Preconditions.checkNotEmpty(str);
            this.zzds = Preconditions.checkNotEmpty(str2);
            this.mComponentName = null;
            this.zzdt = i;
        }

        public zza(ComponentName componentName, int i) {
            this.zzdr = null;
            this.zzds = null;
            this.mComponentName = (ComponentName) Preconditions.checkNotNull(componentName);
            this.zzdt = 129;
        }

        public final String toString() {
            String str = this.zzdr;
            return str == null ? this.mComponentName.flattenToString() : str;
        }

        public final String getPackage() {
            return this.zzds;
        }

        public final ComponentName getComponentName() {
            return this.mComponentName;
        }

        public final int zzq() {
            return this.zzdt;
        }

        public final Intent zzb(Context context) {
            if (this.zzdr != null) {
                return new Intent(this.zzdr).setPackage(this.zzds);
            }
            return new Intent().setComponent(this.mComponentName);
        }

        public final int hashCode() {
            return Objects.hashCode(this.zzdr, this.zzds, this.mComponentName, Integer.valueOf(this.zzdt));
        }

        public final boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof zza)) {
                return false;
            }
            zza zzaVar = (zza) obj;
            return Objects.equal(this.zzdr, zzaVar.zzdr) && Objects.equal(this.zzds, zzaVar.zzds) && Objects.equal(this.mComponentName, zzaVar.mComponentName) && this.zzdt == zzaVar.zzdt;
        }
    }

    public boolean bindService(String str, ServiceConnection serviceConnection, String str2) {
        return zza(new zza(str, 129), serviceConnection, str2);
    }

    public boolean bindService(ComponentName componentName, ServiceConnection serviceConnection, String str) {
        return zza(new zza(componentName, 129), serviceConnection, str);
    }

    public void unbindService(String str, ServiceConnection serviceConnection, String str2) {
        zzb(new zza(str, 129), serviceConnection, str2);
    }

    public final void zza(String str, String str2, int i, ServiceConnection serviceConnection, String str3) {
        zzb(new zza(str, str2, i), serviceConnection, str3);
    }

    public void unbindService(ComponentName componentName, ServiceConnection serviceConnection, String str) {
        zzb(new zza(componentName, 129), serviceConnection, str);
    }
}
