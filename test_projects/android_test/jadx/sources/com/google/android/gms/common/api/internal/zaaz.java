package com.google.android.gms.common.api.internal;

import android.os.Looper;
import android.os.Message;
import android.util.Log;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zaaz extends com.google.android.gms.internal.base.zar {
    private final /* synthetic */ zaaw zagv;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    zaaz(zaaw zaawVar, Looper looper) {
        super(looper);
        this.zagv = zaawVar;
    }

    @Override // android.os.Handler
    public final void handleMessage(Message message) {
        switch (message.what) {
            case 1:
                this.zagv.zaat();
                break;
            case 2:
                this.zagv.resume();
                break;
            default:
                int i = message.what;
                StringBuilder sb = new StringBuilder(31);
                sb.append("Unknown message id: ");
                sb.append(i);
                Log.w("GoogleApiClientImpl", sb.toString());
                break;
        }
    }
}
