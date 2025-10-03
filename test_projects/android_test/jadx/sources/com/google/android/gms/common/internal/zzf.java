package com.google.android.gms.common.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.google.android.gms.common.internal.GmsClientSupervisor;
import com.google.android.gms.common.stats.ConnectionTracker;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/* loaded from: classes.dex */
final class zzf implements ServiceConnection {
    private ComponentName mComponentName;
    private IBinder zzcz;
    private boolean zzea;
    private final GmsClientSupervisor.zza zzeb;
    private final /* synthetic */ zze zzec;
    private final Set<ServiceConnection> zzdz = new HashSet();
    private int mState = 2;

    public zzf(zze zzeVar, GmsClientSupervisor.zza zzaVar) {
        this.zzec = zzeVar;
        this.zzeb = zzaVar;
    }

    @Override // android.content.ServiceConnection
    public final void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        HashMap hashMap;
        Handler handler;
        hashMap = this.zzec.zzdu;
        synchronized (hashMap) {
            handler = this.zzec.mHandler;
            handler.removeMessages(1, this.zzeb);
            this.zzcz = iBinder;
            this.mComponentName = componentName;
            Iterator<ServiceConnection> it = this.zzdz.iterator();
            while (it.hasNext()) {
                it.next().onServiceConnected(componentName, iBinder);
            }
            this.mState = 1;
        }
    }

    @Override // android.content.ServiceConnection
    public final void onServiceDisconnected(ComponentName componentName) {
        HashMap hashMap;
        Handler handler;
        hashMap = this.zzec.zzdu;
        synchronized (hashMap) {
            handler = this.zzec.mHandler;
            handler.removeMessages(1, this.zzeb);
            this.zzcz = null;
            this.mComponentName = componentName;
            Iterator<ServiceConnection> it = this.zzdz.iterator();
            while (it.hasNext()) {
                it.next().onServiceDisconnected(componentName);
            }
            this.mState = 2;
        }
    }

    public final void zze(String str) {
        ConnectionTracker connectionTracker;
        Context context;
        Context context2;
        ConnectionTracker connectionTracker2;
        Context context3;
        Handler handler;
        Handler handler2;
        long j;
        this.mState = 3;
        connectionTracker = this.zzec.zzdw;
        context = this.zzec.zzdv;
        GmsClientSupervisor.zza zzaVar = this.zzeb;
        context2 = this.zzec.zzdv;
        boolean zza = connectionTracker.zza(context, str, zzaVar.zzb(context2), this, this.zzeb.zzq());
        this.zzea = zza;
        if (!zza) {
            this.mState = 2;
            try {
                connectionTracker2 = this.zzec.zzdw;
                context3 = this.zzec.zzdv;
                connectionTracker2.unbindService(context3, this);
                return;
            } catch (IllegalArgumentException e) {
                return;
            }
        }
        handler = this.zzec.mHandler;
        Message obtainMessage = handler.obtainMessage(1, this.zzeb);
        handler2 = this.zzec.mHandler;
        j = this.zzec.zzdy;
        handler2.sendMessageDelayed(obtainMessage, j);
    }

    public final void zzf(String str) {
        Handler handler;
        ConnectionTracker connectionTracker;
        Context context;
        handler = this.zzec.mHandler;
        handler.removeMessages(1, this.zzeb);
        connectionTracker = this.zzec.zzdw;
        context = this.zzec.zzdv;
        connectionTracker.unbindService(context, this);
        this.zzea = false;
        this.mState = 2;
    }

    public final void zza(ServiceConnection serviceConnection, String str) {
        Context context;
        ConnectionTracker unused;
        Context unused2;
        unused = this.zzec.zzdw;
        unused2 = this.zzec.zzdv;
        GmsClientSupervisor.zza zzaVar = this.zzeb;
        context = this.zzec.zzdv;
        zzaVar.zzb(context);
        this.zzdz.add(serviceConnection);
    }

    public final void zzb(ServiceConnection serviceConnection, String str) {
        ConnectionTracker unused;
        Context unused2;
        unused = this.zzec.zzdw;
        unused2 = this.zzec.zzdv;
        this.zzdz.remove(serviceConnection);
    }

    public final boolean isBound() {
        return this.zzea;
    }

    public final int getState() {
        return this.mState;
    }

    public final boolean zza(ServiceConnection serviceConnection) {
        return this.zzdz.contains(serviceConnection);
    }

    public final boolean zzr() {
        return this.zzdz.isEmpty();
    }

    public final IBinder getBinder() {
        return this.zzcz;
    }

    public final ComponentName getComponentName() {
        return this.mComponentName;
    }
}
