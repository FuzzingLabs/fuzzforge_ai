package com.google.android.gms.internal.base;

import android.graphics.drawable.Drawable;

/* compiled from: com.google.android.gms:play-services-base@@17.1.0 */
/* loaded from: classes.dex */
final class zah extends Drawable.ConstantState {
    int mChangingConfigurations;
    int zaoa;

    zah(zah zahVar) {
        if (zahVar != null) {
            this.mChangingConfigurations = zahVar.mChangingConfigurations;
            this.zaoa = zahVar.zaoa;
        }
    }

    @Override // android.graphics.drawable.Drawable.ConstantState
    public final Drawable newDrawable() {
        return new zae(this);
    }

    @Override // android.graphics.drawable.Drawable.ConstantState
    public final int getChangingConfigurations() {
        return this.mChangingConfigurations;
    }
}
