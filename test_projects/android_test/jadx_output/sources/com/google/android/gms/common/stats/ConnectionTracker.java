package com.google.android.gms.common.stats;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import com.google.android.gms.common.util.ClientLibraryUtils;
import java.util.Collections;
import java.util.List;

/* loaded from: classes.dex */
public class ConnectionTracker {
    private static volatile ConnectionTracker zzfa;
    private final List<String> zzfc = Collections.EMPTY_LIST;
    private final List<String> zzfd = Collections.EMPTY_LIST;
    private final List<String> zzfe = Collections.EMPTY_LIST;
    private final List<String> zzff = Collections.EMPTY_LIST;
    private static final Object zzdp = new Object();
    private static boolean zzfb = false;

    public static ConnectionTracker getInstance() {
        if (zzfa == null) {
            synchronized (zzdp) {
                if (zzfa == null) {
                    zzfa = new ConnectionTracker();
                }
            }
        }
        return zzfa;
    }

    private ConnectionTracker() {
    }

    public final boolean zza(Context context, String str, Intent intent, ServiceConnection serviceConnection, int i) {
        boolean zzc;
        ComponentName component = intent.getComponent();
        if (component == null) {
            zzc = false;
        } else {
            zzc = ClientLibraryUtils.zzc(context, component.getPackageName());
        }
        if (zzc) {
            Log.w("ConnectionTracker", "Attempted to bind to a service in a STOPPED package.");
            return false;
        }
        return context.bindService(intent, serviceConnection, i);
    }

    public boolean bindService(Context context, Intent intent, ServiceConnection serviceConnection, int i) {
        return zza(context, context.getClass().getName(), intent, serviceConnection, i);
    }

    public void unbindService(Context context, ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }
}
