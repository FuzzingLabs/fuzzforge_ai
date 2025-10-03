package com.airbnb.lottie.animation;

import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.LocaleList;

/* loaded from: classes.dex */
public class LPaint extends Paint {
    public LPaint() {
    }

    public LPaint(int flags) {
        super(flags);
    }

    public LPaint(PorterDuff.Mode porterDuffMode) {
        setXfermode(new PorterDuffXfermode(porterDuffMode));
    }

    public LPaint(int flags, PorterDuff.Mode porterDuffMode) {
        super(flags);
        setXfermode(new PorterDuffXfermode(porterDuffMode));
    }

    @Override // android.graphics.Paint
    public void setTextLocales(LocaleList locales) {
    }
}
