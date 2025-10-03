package com.google.android.material.shape;

import android.graphics.RectF;
import java.util.Arrays;

/* loaded from: classes.dex */
public final class AdjustedCornerSize implements CornerSize {
    private final float adjustment;
    private final CornerSize other;

    public AdjustedCornerSize(float adjustment, CornerSize other) {
        while (other instanceof AdjustedCornerSize) {
            other = ((AdjustedCornerSize) other).other;
            adjustment += ((AdjustedCornerSize) other).adjustment;
        }
        this.other = other;
        this.adjustment = adjustment;
    }

    @Override // com.google.android.material.shape.CornerSize
    public float getCornerSize(RectF bounds) {
        return Math.max(0.0f, this.other.getCornerSize(bounds) + this.adjustment);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdjustedCornerSize)) {
            return false;
        }
        AdjustedCornerSize that = (AdjustedCornerSize) o;
        return this.other.equals(that.other) && this.adjustment == that.adjustment;
    }

    public int hashCode() {
        Object[] hashedFields = {this.other, Float.valueOf(this.adjustment)};
        return Arrays.hashCode(hashedFields);
    }
}
