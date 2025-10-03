package com.google.android.gms.common.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import com.google.android.gms.common.internal.Asserts;
import com.google.android.gms.internal.base.zaj;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
public abstract class zab {
    final zaa zamz;
    protected int zanb;
    private int zana = 0;
    private boolean zanc = false;
    private boolean zand = true;
    private boolean zane = false;
    private boolean zanf = true;

    public zab(Uri uri, int i) {
        this.zanb = 0;
        this.zamz = new zaa(uri);
        this.zanb = i;
    }

    protected abstract void zaa(Drawable drawable, boolean z, boolean z2, boolean z3);

    final void zaa(Context context, Bitmap bitmap, boolean z) {
        Asserts.checkNotNull(bitmap);
        zaa(new BitmapDrawable(context.getResources(), bitmap), z, false, true);
    }

    final void zaa(Context context, zaj zajVar) {
        if (this.zanf) {
            zaa(null, false, true, false);
        }
    }

    final void zaa(Context context, zaj zajVar, boolean z) {
        Drawable drawable;
        int i = this.zanb;
        if (i == 0) {
            drawable = null;
        } else {
            drawable = context.getResources().getDrawable(i);
        }
        zaa(drawable, z, false, false);
    }

    protected final boolean zaa(boolean z, boolean z2) {
        return (!this.zand || z2 || z) ? false : true;
    }
}
