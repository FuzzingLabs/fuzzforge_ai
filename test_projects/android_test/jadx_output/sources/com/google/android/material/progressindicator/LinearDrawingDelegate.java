package com.google.android.material.progressindicator;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.google.android.material.color.MaterialColors;

/* loaded from: classes.dex */
final class LinearDrawingDelegate extends DrawingDelegate<LinearProgressIndicatorSpec> {
    private float displayedCornerRadius;
    private float displayedTrackThickness;
    private float trackLength;

    public LinearDrawingDelegate(LinearProgressIndicatorSpec spec) {
        super(spec);
        this.trackLength = 300.0f;
    }

    @Override // com.google.android.material.progressindicator.DrawingDelegate
    public int getPreferredWidth() {
        return -1;
    }

    @Override // com.google.android.material.progressindicator.DrawingDelegate
    public int getPreferredHeight() {
        return ((LinearProgressIndicatorSpec) this.spec).trackThickness;
    }

    @Override // com.google.android.material.progressindicator.DrawingDelegate
    public void adjustCanvas(Canvas canvas, float trackThicknessFraction) {
        Rect clipBounds = canvas.getClipBounds();
        this.trackLength = clipBounds.width();
        float trackSize = ((LinearProgressIndicatorSpec) this.spec).trackThickness;
        canvas.translate(clipBounds.left + (clipBounds.width() / 2.0f), clipBounds.top + (clipBounds.height() / 2.0f) + Math.max(0.0f, (clipBounds.height() - ((LinearProgressIndicatorSpec) this.spec).trackThickness) / 2.0f));
        if (((LinearProgressIndicatorSpec) this.spec).drawHorizontallyInverse) {
            canvas.scale(-1.0f, 1.0f);
        }
        if ((this.drawable.isShowing() && ((LinearProgressIndicatorSpec) this.spec).showAnimationBehavior == 1) || (this.drawable.isHiding() && ((LinearProgressIndicatorSpec) this.spec).hideAnimationBehavior == 2)) {
            canvas.scale(1.0f, -1.0f);
        }
        if (this.drawable.isShowing() || this.drawable.isHiding()) {
            canvas.translate(0.0f, (((LinearProgressIndicatorSpec) this.spec).trackThickness * (trackThicknessFraction - 1.0f)) / 2.0f);
        }
        float f = this.trackLength;
        canvas.clipRect((-f) / 2.0f, (-trackSize) / 2.0f, f / 2.0f, trackSize / 2.0f);
        this.displayedTrackThickness = ((LinearProgressIndicatorSpec) this.spec).trackThickness * trackThicknessFraction;
        this.displayedCornerRadius = ((LinearProgressIndicatorSpec) this.spec).trackCornerRadius * trackThicknessFraction;
    }

    @Override // com.google.android.material.progressindicator.DrawingDelegate
    public void fillIndicator(Canvas canvas, Paint paint, float startFraction, float endFraction, int color) {
        if (startFraction == endFraction) {
            return;
        }
        float f = this.trackLength;
        float f2 = this.displayedCornerRadius;
        float adjustedStartX = ((-f) / 2.0f) + f2 + ((f - (f2 * 2.0f)) * startFraction);
        float adjustedEndX = ((-f) / 2.0f) + f2 + ((f - (f2 * 2.0f)) * endFraction);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(color);
        float f3 = this.displayedTrackThickness;
        canvas.drawRect(adjustedStartX, (-f3) / 2.0f, adjustedEndX, f3 / 2.0f, paint);
        float f4 = this.displayedCornerRadius;
        RectF cornerPatternRectBound = new RectF(-f4, -f4, f4, f4);
        drawRoundedEnd(canvas, paint, this.displayedTrackThickness, this.displayedCornerRadius, adjustedStartX, true, cornerPatternRectBound);
        drawRoundedEnd(canvas, paint, this.displayedTrackThickness, this.displayedCornerRadius, adjustedEndX, false, cornerPatternRectBound);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // com.google.android.material.progressindicator.DrawingDelegate
    public void fillTrack(Canvas canvas, Paint paint) {
        int trackColor = MaterialColors.compositeARGBWithAlpha(((LinearProgressIndicatorSpec) this.spec).trackColor, this.drawable.getAlpha());
        float adjustedStartX = ((-this.trackLength) / 2.0f) + this.displayedCornerRadius;
        float adjustedEndX = -adjustedStartX;
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(trackColor);
        float f = this.displayedTrackThickness;
        canvas.drawRect(adjustedStartX, (-f) / 2.0f, adjustedEndX, f / 2.0f, paint);
        float f2 = this.displayedCornerRadius;
        RectF cornerPatternRectBound = new RectF(-f2, -f2, f2, f2);
        drawRoundedEnd(canvas, paint, this.displayedTrackThickness, this.displayedCornerRadius, adjustedStartX, true, cornerPatternRectBound);
        drawRoundedEnd(canvas, paint, this.displayedTrackThickness, this.displayedCornerRadius, adjustedEndX, false, cornerPatternRectBound);
    }

    private static void drawRoundedEnd(Canvas canvas, Paint paint, float trackSize, float cornerRadius, float x, boolean isStartPosition, RectF cornerPatternRectBound) {
        canvas.save();
        canvas.translate(x, 0.0f);
        if (!isStartPosition) {
            canvas.rotate(180.0f);
        }
        canvas.drawRect(-cornerRadius, ((-trackSize) / 2.0f) + cornerRadius, 0.0f, (trackSize / 2.0f) - cornerRadius, paint);
        canvas.save();
        canvas.translate(0.0f, ((-trackSize) / 2.0f) + cornerRadius);
        canvas.drawArc(cornerPatternRectBound, 180.0f, 90.0f, true, paint);
        canvas.restore();
        canvas.translate(0.0f, (trackSize / 2.0f) - cornerRadius);
        canvas.drawArc(cornerPatternRectBound, 180.0f, -90.0f, true, paint);
        canvas.restore();
    }
}
