package app.beetlebug.utils;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

/* loaded from: classes7.dex */
public class CustomTypeFaceSpan extends TypefaceSpan {
    private final int mColor;
    private final Typeface newType;

    public CustomTypeFaceSpan(String family, Typeface type, int color) {
        super(family);
        this.newType = type;
        this.mColor = color;
    }

    @Override // android.text.style.TypefaceSpan, android.text.style.CharacterStyle
    public void updateDrawState(TextPaint ds) {
        ds.setColor(this.mColor);
        applyCustomTypeFace(ds, this.newType);
    }

    @Override // android.text.style.TypefaceSpan, android.text.style.MetricAffectingSpan
    public void updateMeasureState(TextPaint paint) {
        applyCustomTypeFace(paint, this.newType);
    }

    @Override // android.text.style.TypefaceSpan, android.text.ParcelableSpan
    public int getSpanTypeId() {
        return super.getSpanTypeId();
    }

    public int getForegroundColor() {
        return this.mColor;
    }

    private static void applyCustomTypeFace(Paint paint, Typeface tf) {
        int oldStyle;
        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }
        int fake = (~tf.getStyle()) & oldStyle;
        if ((fake & 1) != 0) {
            paint.setFakeBoldText(true);
        }
        if ((fake & 2) != 0) {
            paint.setTextSkewX(-0.25f);
        }
        paint.setTypeface(tf);
    }
}
