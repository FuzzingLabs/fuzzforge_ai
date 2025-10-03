package com.airbnb.lottie.model;

/* loaded from: classes.dex */
public class DocumentData {
    public final float baselineShift;
    public final int color;
    public final String fontName;
    public final Justification justification;
    public final float lineHeight;
    public final float size;
    public final int strokeColor;
    public final boolean strokeOverFill;
    public final float strokeWidth;
    public final String text;
    public final int tracking;

    public enum Justification {
        LEFT_ALIGN,
        RIGHT_ALIGN,
        CENTER
    }

    public DocumentData(String text, String fontName, float size, Justification justification, int tracking, float lineHeight, float baselineShift, int color, int strokeColor, float strokeWidth, boolean strokeOverFill) {
        this.text = text;
        this.fontName = fontName;
        this.size = size;
        this.justification = justification;
        this.tracking = tracking;
        this.lineHeight = lineHeight;
        this.baselineShift = baselineShift;
        this.color = color;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.strokeOverFill = strokeOverFill;
    }

    public int hashCode() {
        int result = this.text.hashCode();
        int result2 = (((((int) ((((result * 31) + this.fontName.hashCode()) * 31) + this.size)) * 31) + this.justification.ordinal()) * 31) + this.tracking;
        long temp = Float.floatToRawIntBits(this.lineHeight);
        return (((result2 * 31) + ((int) ((temp >>> 32) ^ temp))) * 31) + this.color;
    }
}
