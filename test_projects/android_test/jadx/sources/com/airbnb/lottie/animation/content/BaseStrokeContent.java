package com.airbnb.lottie.animation.content;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import com.airbnb.lottie.C0633L;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.LPaint;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.FloatKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.IntegerKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.content.ShapeTrimPath;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public abstract class BaseStrokeContent implements BaseKeyframeAnimation.AnimationListener, KeyPathElementContent, DrawingContent {
    private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;
    private final List<BaseKeyframeAnimation<?, Float>> dashPatternAnimations;
    private final BaseKeyframeAnimation<?, Float> dashPatternOffsetAnimation;
    private final float[] dashPatternValues;
    protected final BaseLayer layer;
    private final LottieDrawable lottieDrawable;
    private final BaseKeyframeAnimation<?, Integer> opacityAnimation;
    final Paint paint;
    private final BaseKeyframeAnimation<?, Float> widthAnimation;

    /* renamed from: pm */
    private final PathMeasure f118pm = new PathMeasure();
    private final Path path = new Path();
    private final Path trimPathPath = new Path();
    private final RectF rect = new RectF();
    private final List<PathGroup> pathGroups = new ArrayList();

    BaseStrokeContent(LottieDrawable lottieDrawable, BaseLayer layer, Paint.Cap cap, Paint.Join join, float miterLimit, AnimatableIntegerValue opacity, AnimatableFloatValue width, List<AnimatableFloatValue> dashPattern, AnimatableFloatValue offset) {
        LPaint lPaint = new LPaint(1);
        this.paint = lPaint;
        this.lottieDrawable = lottieDrawable;
        this.layer = layer;
        lPaint.setStyle(Paint.Style.STROKE);
        lPaint.setStrokeCap(cap);
        lPaint.setStrokeJoin(join);
        lPaint.setStrokeMiter(miterLimit);
        this.opacityAnimation = opacity.createAnimation();
        this.widthAnimation = width.createAnimation();
        if (offset == null) {
            this.dashPatternOffsetAnimation = null;
        } else {
            this.dashPatternOffsetAnimation = offset.createAnimation();
        }
        this.dashPatternAnimations = new ArrayList(dashPattern.size());
        this.dashPatternValues = new float[dashPattern.size()];
        for (int i = 0; i < dashPattern.size(); i++) {
            this.dashPatternAnimations.add(dashPattern.get(i).createAnimation());
        }
        layer.addAnimation(this.opacityAnimation);
        layer.addAnimation(this.widthAnimation);
        for (int i2 = 0; i2 < this.dashPatternAnimations.size(); i2++) {
            layer.addAnimation(this.dashPatternAnimations.get(i2));
        }
        BaseKeyframeAnimation<?, Float> baseKeyframeAnimation = this.dashPatternOffsetAnimation;
        if (baseKeyframeAnimation != null) {
            layer.addAnimation(baseKeyframeAnimation);
        }
        this.opacityAnimation.addUpdateListener(this);
        this.widthAnimation.addUpdateListener(this);
        for (int i3 = 0; i3 < dashPattern.size(); i3++) {
            this.dashPatternAnimations.get(i3).addUpdateListener(this);
        }
        BaseKeyframeAnimation<?, Float> baseKeyframeAnimation2 = this.dashPatternOffsetAnimation;
        if (baseKeyframeAnimation2 != null) {
            baseKeyframeAnimation2.addUpdateListener(this);
        }
    }

    @Override // com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation.AnimationListener
    public void onValueChanged() {
        this.lottieDrawable.invalidateSelf();
    }

    @Override // com.airbnb.lottie.animation.content.Content
    public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
        TrimPathContent trimPathContentBefore = null;
        for (int i = contentsBefore.size() - 1; i >= 0; i--) {
            Content content = contentsBefore.get(i);
            if ((content instanceof TrimPathContent) && ((TrimPathContent) content).getType() == ShapeTrimPath.Type.INDIVIDUALLY) {
                trimPathContentBefore = (TrimPathContent) content;
            }
        }
        if (trimPathContentBefore != null) {
            trimPathContentBefore.addListener(this);
        }
        PathGroup currentPathGroup = null;
        for (int i2 = contentsAfter.size() - 1; i2 >= 0; i2--) {
            Content content2 = contentsAfter.get(i2);
            if ((content2 instanceof TrimPathContent) && ((TrimPathContent) content2).getType() == ShapeTrimPath.Type.INDIVIDUALLY) {
                if (currentPathGroup != null) {
                    this.pathGroups.add(currentPathGroup);
                }
                currentPathGroup = new PathGroup((TrimPathContent) content2);
                ((TrimPathContent) content2).addListener(this);
            } else if (content2 instanceof PathContent) {
                if (currentPathGroup == null) {
                    currentPathGroup = new PathGroup(trimPathContentBefore);
                }
                currentPathGroup.paths.add((PathContent) content2);
            }
        }
        if (currentPathGroup != null) {
            this.pathGroups.add(currentPathGroup);
        }
    }

    @Override // com.airbnb.lottie.animation.content.DrawingContent
    public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        C0633L.beginSection("StrokeContent#draw");
        if (Utils.hasZeroScaleAxis(parentMatrix)) {
            C0633L.endSection("StrokeContent#draw");
            return;
        }
        int alpha = (int) ((((parentAlpha / 255.0f) * ((IntegerKeyframeAnimation) this.opacityAnimation).getIntValue()) / 100.0f) * 255.0f);
        this.paint.setAlpha(MiscUtils.clamp(alpha, 0, 255));
        this.paint.setStrokeWidth(((FloatKeyframeAnimation) this.widthAnimation).getFloatValue() * Utils.getScale(parentMatrix));
        if (this.paint.getStrokeWidth() <= 0.0f) {
            C0633L.endSection("StrokeContent#draw");
            return;
        }
        applyDashPatternIfNeeded(parentMatrix);
        BaseKeyframeAnimation<ColorFilter, ColorFilter> baseKeyframeAnimation = this.colorFilterAnimation;
        if (baseKeyframeAnimation != null) {
            this.paint.setColorFilter(baseKeyframeAnimation.getValue());
        }
        for (int i = 0; i < this.pathGroups.size(); i++) {
            PathGroup pathGroup = this.pathGroups.get(i);
            if (pathGroup.trimPath != null) {
                applyTrimPath(canvas, pathGroup, parentMatrix);
            } else {
                C0633L.beginSection("StrokeContent#buildPath");
                this.path.reset();
                for (int j = pathGroup.paths.size() - 1; j >= 0; j--) {
                    this.path.addPath(((PathContent) pathGroup.paths.get(j)).getPath(), parentMatrix);
                }
                C0633L.endSection("StrokeContent#buildPath");
                C0633L.beginSection("StrokeContent#drawPath");
                canvas.drawPath(this.path, this.paint);
                C0633L.endSection("StrokeContent#drawPath");
            }
        }
        C0633L.endSection("StrokeContent#draw");
    }

    private void applyTrimPath(Canvas canvas, PathGroup pathGroup, Matrix parentMatrix) {
        float startValue;
        float endValue;
        float startValue2;
        C0633L.beginSection("StrokeContent#applyTrimPath");
        if (pathGroup.trimPath == null) {
            C0633L.endSection("StrokeContent#applyTrimPath");
            return;
        }
        this.path.reset();
        for (int j = pathGroup.paths.size() - 1; j >= 0; j--) {
            this.path.addPath(((PathContent) pathGroup.paths.get(j)).getPath(), parentMatrix);
        }
        this.f118pm.setPath(this.path, false);
        float totalLength = this.f118pm.getLength();
        while (this.f118pm.nextContour()) {
            totalLength += this.f118pm.getLength();
        }
        float offsetLength = (pathGroup.trimPath.getOffset().getValue().floatValue() * totalLength) / 360.0f;
        float startLength = ((pathGroup.trimPath.getStart().getValue().floatValue() * totalLength) / 100.0f) + offsetLength;
        float endLength = ((pathGroup.trimPath.getEnd().getValue().floatValue() * totalLength) / 100.0f) + offsetLength;
        float currentLength = 0.0f;
        for (int j2 = pathGroup.paths.size() - 1; j2 >= 0; j2--) {
            this.trimPathPath.set(((PathContent) pathGroup.paths.get(j2)).getPath());
            this.trimPathPath.transform(parentMatrix);
            this.f118pm.setPath(this.trimPathPath, false);
            float length = this.f118pm.getLength();
            if (endLength > totalLength && endLength - totalLength < currentLength + length && currentLength < endLength - totalLength) {
                if (startLength > totalLength) {
                    startValue2 = (startLength - totalLength) / length;
                } else {
                    startValue2 = 0.0f;
                }
                float endValue2 = Math.min((endLength - totalLength) / length, 1.0f);
                Utils.applyTrimPathIfNeeded(this.trimPathPath, startValue2, endValue2, 0.0f);
                canvas.drawPath(this.trimPathPath, this.paint);
            } else if (currentLength + length >= startLength && currentLength <= endLength) {
                if (currentLength + length <= endLength && startLength < currentLength) {
                    canvas.drawPath(this.trimPathPath, this.paint);
                } else {
                    if (startLength < currentLength) {
                        startValue = 0.0f;
                    } else {
                        float startValue3 = startLength - currentLength;
                        startValue = startValue3 / length;
                    }
                    if (endLength > currentLength + length) {
                        endValue = 1.0f;
                    } else {
                        float endValue3 = endLength - currentLength;
                        endValue = endValue3 / length;
                    }
                    Utils.applyTrimPathIfNeeded(this.trimPathPath, startValue, endValue, 0.0f);
                    canvas.drawPath(this.trimPathPath, this.paint);
                }
            }
            currentLength += length;
        }
        C0633L.endSection("StrokeContent#applyTrimPath");
    }

    @Override // com.airbnb.lottie.animation.content.DrawingContent
    public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
        C0633L.beginSection("StrokeContent#getBounds");
        this.path.reset();
        for (int i = 0; i < this.pathGroups.size(); i++) {
            PathGroup pathGroup = this.pathGroups.get(i);
            for (int j = 0; j < pathGroup.paths.size(); j++) {
                this.path.addPath(((PathContent) pathGroup.paths.get(j)).getPath(), parentMatrix);
            }
        }
        this.path.computeBounds(this.rect, false);
        float width = ((FloatKeyframeAnimation) this.widthAnimation).getFloatValue();
        RectF rectF = this.rect;
        rectF.set(rectF.left - (width / 2.0f), this.rect.top - (width / 2.0f), this.rect.right + (width / 2.0f), this.rect.bottom + (width / 2.0f));
        outBounds.set(this.rect);
        outBounds.set(outBounds.left - 1.0f, outBounds.top - 1.0f, outBounds.right + 1.0f, outBounds.bottom + 1.0f);
        C0633L.endSection("StrokeContent#getBounds");
    }

    private void applyDashPatternIfNeeded(Matrix parentMatrix) {
        C0633L.beginSection("StrokeContent#applyDashPattern");
        if (this.dashPatternAnimations.isEmpty()) {
            C0633L.endSection("StrokeContent#applyDashPattern");
            return;
        }
        float scale = Utils.getScale(parentMatrix);
        for (int i = 0; i < this.dashPatternAnimations.size(); i++) {
            this.dashPatternValues[i] = this.dashPatternAnimations.get(i).getValue().floatValue();
            if (i % 2 == 0) {
                float[] fArr = this.dashPatternValues;
                if (fArr[i] < 1.0f) {
                    fArr[i] = 1.0f;
                }
            } else {
                float[] fArr2 = this.dashPatternValues;
                if (fArr2[i] < 0.1f) {
                    fArr2[i] = 0.1f;
                }
            }
            float[] fArr3 = this.dashPatternValues;
            fArr3[i] = fArr3[i] * scale;
        }
        BaseKeyframeAnimation<?, Float> baseKeyframeAnimation = this.dashPatternOffsetAnimation;
        float offset = baseKeyframeAnimation == null ? 0.0f : baseKeyframeAnimation.getValue().floatValue() * scale;
        this.paint.setPathEffect(new DashPathEffect(this.dashPatternValues, offset));
        C0633L.endSection("StrokeContent#applyDashPattern");
    }

    @Override // com.airbnb.lottie.model.KeyPathElement
    public void resolveKeyPath(KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath) {
        MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
    }

    @Override // com.airbnb.lottie.model.KeyPathElement
    public <T> void addValueCallback(T property, LottieValueCallback<T> callback) {
        if (property == LottieProperty.OPACITY) {
            this.opacityAnimation.setValueCallback(callback);
            return;
        }
        if (property == LottieProperty.STROKE_WIDTH) {
            this.widthAnimation.setValueCallback(callback);
            return;
        }
        if (property == LottieProperty.COLOR_FILTER) {
            BaseKeyframeAnimation<ColorFilter, ColorFilter> baseKeyframeAnimation = this.colorFilterAnimation;
            if (baseKeyframeAnimation != null) {
                this.layer.removeAnimation(baseKeyframeAnimation);
            }
            if (callback == null) {
                this.colorFilterAnimation = null;
                return;
            }
            ValueCallbackKeyframeAnimation valueCallbackKeyframeAnimation = new ValueCallbackKeyframeAnimation(callback);
            this.colorFilterAnimation = valueCallbackKeyframeAnimation;
            valueCallbackKeyframeAnimation.addUpdateListener(this);
            this.layer.addAnimation(this.colorFilterAnimation);
        }
    }

    private static final class PathGroup {
        private final List<PathContent> paths;
        private final TrimPathContent trimPath;

        private PathGroup(TrimPathContent trimPath) {
            this.paths = new ArrayList();
            this.trimPath = trimPath;
        }
    }
}
