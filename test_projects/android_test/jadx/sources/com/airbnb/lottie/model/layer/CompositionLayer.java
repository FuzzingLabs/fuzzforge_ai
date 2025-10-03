package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.collection.LongSparseArray;
import com.airbnb.lottie.C0633L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class CompositionLayer extends BaseLayer {
    private Boolean hasMasks;
    private Boolean hasMatte;
    private Paint layerPaint;
    private final List<BaseLayer> layers;
    private final RectF newClipRect;
    private final RectF rect;
    private BaseKeyframeAnimation<Float, Float> timeRemapping;

    public CompositionLayer(LottieDrawable lottieDrawable, Layer layerModel, List<Layer> layerModels, LottieComposition composition) {
        super(lottieDrawable, layerModel);
        BaseLayer parentLayer;
        this.layers = new ArrayList();
        this.rect = new RectF();
        this.newClipRect = new RectF();
        this.layerPaint = new Paint();
        AnimatableFloatValue timeRemapping = layerModel.getTimeRemapping();
        if (timeRemapping != null) {
            BaseKeyframeAnimation<Float, Float> createAnimation = timeRemapping.createAnimation();
            this.timeRemapping = createAnimation;
            addAnimation(createAnimation);
            this.timeRemapping.addUpdateListener(this);
        } else {
            this.timeRemapping = null;
        }
        LongSparseArray<BaseLayer> layerMap = new LongSparseArray<>(composition.getLayers().size());
        BaseLayer mattedLayer = null;
        for (int i = layerModels.size() - 1; i >= 0; i--) {
            Layer lm = layerModels.get(i);
            BaseLayer layer = BaseLayer.forModel(lm, lottieDrawable, composition);
            if (layer != null) {
                layerMap.put(layer.getLayerModel().getId(), layer);
                if (mattedLayer != null) {
                    mattedLayer.setMatteLayer(layer);
                    mattedLayer = null;
                } else {
                    this.layers.add(0, layer);
                    switch (C06791.$SwitchMap$com$airbnb$lottie$model$layer$Layer$MatteType[lm.getMatteType().ordinal()]) {
                        case 1:
                        case 2:
                            mattedLayer = layer;
                            break;
                    }
                }
            }
        }
        for (int i2 = 0; i2 < layerMap.size(); i2++) {
            long key = layerMap.keyAt(i2);
            BaseLayer layerView = layerMap.get(key);
            if (layerView != null && (parentLayer = layerMap.get(layerView.getLayerModel().getParentId())) != null) {
                layerView.setParentLayer(parentLayer);
            }
        }
    }

    /* renamed from: com.airbnb.lottie.model.layer.CompositionLayer$1 */
    static /* synthetic */ class C06791 {
        static final /* synthetic */ int[] $SwitchMap$com$airbnb$lottie$model$layer$Layer$MatteType;

        static {
            int[] iArr = new int[Layer.MatteType.values().length];
            $SwitchMap$com$airbnb$lottie$model$layer$Layer$MatteType = iArr;
            try {
                iArr[Layer.MatteType.ADD.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$airbnb$lottie$model$layer$Layer$MatteType[Layer.MatteType.INVERT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    @Override // com.airbnb.lottie.model.layer.BaseLayer
    void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        C0633L.beginSection("CompositionLayer#draw");
        this.newClipRect.set(0.0f, 0.0f, this.layerModel.getPreCompWidth(), this.layerModel.getPreCompHeight());
        parentMatrix.mapRect(this.newClipRect);
        boolean isDrawingWithOffScreen = this.lottieDrawable.isApplyingOpacityToLayersEnabled() && this.layers.size() > 1 && parentAlpha != 255;
        if (isDrawingWithOffScreen) {
            this.layerPaint.setAlpha(parentAlpha);
            Utils.saveLayerCompat(canvas, this.newClipRect, this.layerPaint);
        } else {
            canvas.save();
        }
        int childAlpha = isDrawingWithOffScreen ? 255 : parentAlpha;
        for (int i = this.layers.size() - 1; i >= 0; i--) {
            boolean nonEmptyClip = true;
            if (!this.newClipRect.isEmpty()) {
                nonEmptyClip = canvas.clipRect(this.newClipRect);
            }
            if (nonEmptyClip) {
                BaseLayer layer = this.layers.get(i);
                layer.draw(canvas, parentMatrix, childAlpha);
            }
        }
        canvas.restore();
        C0633L.endSection("CompositionLayer#draw");
    }

    @Override // com.airbnb.lottie.model.layer.BaseLayer, com.airbnb.lottie.animation.content.DrawingContent
    public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
        super.getBounds(outBounds, parentMatrix, applyParents);
        for (int i = this.layers.size() - 1; i >= 0; i--) {
            this.rect.set(0.0f, 0.0f, 0.0f, 0.0f);
            this.layers.get(i).getBounds(this.rect, this.boundsMatrix, true);
            outBounds.union(this.rect);
        }
    }

    @Override // com.airbnb.lottie.model.layer.BaseLayer
    public void setProgress(float progress) {
        super.setProgress(progress);
        if (this.timeRemapping != null) {
            float durationFrames = this.lottieDrawable.getComposition().getDurationFrames() + 0.01f;
            float compositionDelayFrames = this.layerModel.getComposition().getStartFrame();
            float remappedFrames = (this.timeRemapping.getValue().floatValue() * this.layerModel.getComposition().getFrameRate()) - compositionDelayFrames;
            progress = remappedFrames / durationFrames;
        }
        if (this.timeRemapping == null) {
            progress -= this.layerModel.getStartProgress();
        }
        if (this.layerModel.getTimeStretch() != 0.0f) {
            progress /= this.layerModel.getTimeStretch();
        }
        for (int i = this.layers.size() - 1; i >= 0; i--) {
            this.layers.get(i).setProgress(progress);
        }
    }

    public boolean hasMasks() {
        if (this.hasMasks == null) {
            for (int i = this.layers.size() - 1; i >= 0; i--) {
                BaseLayer layer = this.layers.get(i);
                if (layer instanceof ShapeLayer) {
                    if (layer.hasMasksOnThisLayer()) {
                        this.hasMasks = true;
                        return true;
                    }
                } else if ((layer instanceof CompositionLayer) && ((CompositionLayer) layer).hasMasks()) {
                    this.hasMasks = true;
                    return true;
                }
            }
            this.hasMasks = false;
        }
        return this.hasMasks.booleanValue();
    }

    public boolean hasMatte() {
        if (this.hasMatte == null) {
            if (hasMatteOnThisLayer()) {
                this.hasMatte = true;
                return true;
            }
            for (int i = this.layers.size() - 1; i >= 0; i--) {
                if (this.layers.get(i).hasMatteOnThisLayer()) {
                    this.hasMatte = true;
                    return true;
                }
            }
            this.hasMatte = false;
        }
        return this.hasMatte.booleanValue();
    }

    @Override // com.airbnb.lottie.model.layer.BaseLayer
    protected void resolveChildKeyPath(KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath) {
        for (int i = 0; i < this.layers.size(); i++) {
            this.layers.get(i).resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath);
        }
    }

    @Override // com.airbnb.lottie.model.layer.BaseLayer, com.airbnb.lottie.model.KeyPathElement
    public <T> void addValueCallback(T property, LottieValueCallback<T> callback) {
        super.addValueCallback(property, callback);
        if (property == LottieProperty.TIME_REMAP) {
            if (callback == null) {
                BaseKeyframeAnimation<Float, Float> baseKeyframeAnimation = this.timeRemapping;
                if (baseKeyframeAnimation != null) {
                    baseKeyframeAnimation.setValueCallback(null);
                    return;
                }
                return;
            }
            ValueCallbackKeyframeAnimation valueCallbackKeyframeAnimation = new ValueCallbackKeyframeAnimation(callback);
            this.timeRemapping = valueCallbackKeyframeAnimation;
            valueCallbackKeyframeAnimation.addUpdateListener(this);
            addAnimation(this.timeRemapping);
        }
    }
}
