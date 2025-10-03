package com.airbnb.lottie.animation.content;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.LPaint;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.KeyPathElement;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.content.ShapeGroup;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class ContentGroup implements DrawingContent, PathContent, BaseKeyframeAnimation.AnimationListener, KeyPathElement {
    private final List<Content> contents;
    private final boolean hidden;
    private final LottieDrawable lottieDrawable;
    private final Matrix matrix;
    private final String name;
    private Paint offScreenPaint;
    private RectF offScreenRectF;
    private final Path path;
    private List<PathContent> pathContents;
    private final RectF rect;
    private TransformKeyframeAnimation transformAnimation;

    private static List<Content> contentsFromModels(LottieDrawable drawable, BaseLayer layer, List<ContentModel> contentModels) {
        List<Content> contents = new ArrayList<>(contentModels.size());
        for (int i = 0; i < contentModels.size(); i++) {
            Content content = contentModels.get(i).toContent(drawable, layer);
            if (content != null) {
                contents.add(content);
            }
        }
        return contents;
    }

    static AnimatableTransform findTransform(List<ContentModel> contentModels) {
        for (int i = 0; i < contentModels.size(); i++) {
            ContentModel contentModel = contentModels.get(i);
            if (contentModel instanceof AnimatableTransform) {
                return (AnimatableTransform) contentModel;
            }
        }
        return null;
    }

    public ContentGroup(LottieDrawable lottieDrawable, BaseLayer layer, ShapeGroup shapeGroup) {
        this(lottieDrawable, layer, shapeGroup.getName(), shapeGroup.isHidden(), contentsFromModels(lottieDrawable, layer, shapeGroup.getItems()), findTransform(shapeGroup.getItems()));
    }

    ContentGroup(LottieDrawable lottieDrawable, BaseLayer layer, String name, boolean hidden, List<Content> contents, AnimatableTransform transform) {
        this.offScreenPaint = new LPaint();
        this.offScreenRectF = new RectF();
        this.matrix = new Matrix();
        this.path = new Path();
        this.rect = new RectF();
        this.name = name;
        this.lottieDrawable = lottieDrawable;
        this.hidden = hidden;
        this.contents = contents;
        if (transform != null) {
            TransformKeyframeAnimation createAnimation = transform.createAnimation();
            this.transformAnimation = createAnimation;
            createAnimation.addAnimationsToLayer(layer);
            this.transformAnimation.addListener(this);
        }
        List<GreedyContent> greedyContents = new ArrayList<>();
        for (int i = contents.size() - 1; i >= 0; i--) {
            Content content = contents.get(i);
            if (content instanceof GreedyContent) {
                greedyContents.add((GreedyContent) content);
            }
        }
        int i2 = greedyContents.size();
        for (int i3 = i2 - 1; i3 >= 0; i3--) {
            greedyContents.get(i3).absorbContent(contents.listIterator(contents.size()));
        }
    }

    @Override // com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation.AnimationListener
    public void onValueChanged() {
        this.lottieDrawable.invalidateSelf();
    }

    @Override // com.airbnb.lottie.animation.content.Content
    public String getName() {
        return this.name;
    }

    @Override // com.airbnb.lottie.animation.content.Content
    public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
        List<Content> myContentsBefore = new ArrayList<>(contentsBefore.size() + this.contents.size());
        myContentsBefore.addAll(contentsBefore);
        for (int i = this.contents.size() - 1; i >= 0; i--) {
            Content content = this.contents.get(i);
            content.setContents(myContentsBefore, this.contents.subList(0, i));
            myContentsBefore.add(content);
        }
    }

    List<PathContent> getPathList() {
        if (this.pathContents == null) {
            this.pathContents = new ArrayList();
            for (int i = 0; i < this.contents.size(); i++) {
                Content content = this.contents.get(i);
                if (content instanceof PathContent) {
                    this.pathContents.add((PathContent) content);
                }
            }
        }
        return this.pathContents;
    }

    Matrix getTransformationMatrix() {
        TransformKeyframeAnimation transformKeyframeAnimation = this.transformAnimation;
        if (transformKeyframeAnimation != null) {
            return transformKeyframeAnimation.getMatrix();
        }
        this.matrix.reset();
        return this.matrix;
    }

    @Override // com.airbnb.lottie.animation.content.PathContent
    public Path getPath() {
        this.matrix.reset();
        TransformKeyframeAnimation transformKeyframeAnimation = this.transformAnimation;
        if (transformKeyframeAnimation != null) {
            this.matrix.set(transformKeyframeAnimation.getMatrix());
        }
        this.path.reset();
        if (this.hidden) {
            return this.path;
        }
        for (int i = this.contents.size() - 1; i >= 0; i--) {
            Content content = this.contents.get(i);
            if (content instanceof PathContent) {
                this.path.addPath(((PathContent) content).getPath(), this.matrix);
            }
        }
        return this.path;
    }

    @Override // com.airbnb.lottie.animation.content.DrawingContent
    public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        int opacity;
        if (this.hidden) {
            return;
        }
        this.matrix.set(parentMatrix);
        TransformKeyframeAnimation transformKeyframeAnimation = this.transformAnimation;
        if (transformKeyframeAnimation != null) {
            this.matrix.preConcat(transformKeyframeAnimation.getMatrix());
            int opacity2 = this.transformAnimation.getOpacity() == null ? 100 : this.transformAnimation.getOpacity().getValue().intValue();
            opacity = (int) ((((opacity2 / 100.0f) * parentAlpha) / 255.0f) * 255.0f);
        } else {
            opacity = parentAlpha;
        }
        boolean isRenderingWithOffScreen = this.lottieDrawable.isApplyingOpacityToLayersEnabled() && hasTwoOrMoreDrawableContent() && opacity != 255;
        if (isRenderingWithOffScreen) {
            this.offScreenRectF.set(0.0f, 0.0f, 0.0f, 0.0f);
            getBounds(this.offScreenRectF, this.matrix, true);
            this.offScreenPaint.setAlpha(opacity);
            Utils.saveLayerCompat(canvas, this.offScreenRectF, this.offScreenPaint);
        }
        int childAlpha = isRenderingWithOffScreen ? 255 : opacity;
        for (int i = this.contents.size() - 1; i >= 0; i--) {
            Object content = this.contents.get(i);
            if (content instanceof DrawingContent) {
                ((DrawingContent) content).draw(canvas, this.matrix, childAlpha);
            }
        }
        if (isRenderingWithOffScreen) {
            canvas.restore();
        }
    }

    private boolean hasTwoOrMoreDrawableContent() {
        int drawableContentCount = 0;
        for (int i = 0; i < this.contents.size(); i++) {
            if ((this.contents.get(i) instanceof DrawingContent) && (drawableContentCount = drawableContentCount + 1) >= 2) {
                return true;
            }
        }
        return false;
    }

    @Override // com.airbnb.lottie.animation.content.DrawingContent
    public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
        this.matrix.set(parentMatrix);
        TransformKeyframeAnimation transformKeyframeAnimation = this.transformAnimation;
        if (transformKeyframeAnimation != null) {
            this.matrix.preConcat(transformKeyframeAnimation.getMatrix());
        }
        this.rect.set(0.0f, 0.0f, 0.0f, 0.0f);
        for (int i = this.contents.size() - 1; i >= 0; i--) {
            Content content = this.contents.get(i);
            if (content instanceof DrawingContent) {
                ((DrawingContent) content).getBounds(this.rect, this.matrix, applyParents);
                outBounds.union(this.rect);
            }
        }
    }

    @Override // com.airbnb.lottie.model.KeyPathElement
    public void resolveKeyPath(KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath) {
        if (!keyPath.matches(getName(), depth)) {
            return;
        }
        if (!"__container".equals(getName())) {
            currentPartialKeyPath = currentPartialKeyPath.addKey(getName());
            if (keyPath.fullyResolvesTo(getName(), depth)) {
                accumulator.add(currentPartialKeyPath.resolve(this));
            }
        }
        if (keyPath.propagateToChildren(getName(), depth)) {
            int newDepth = keyPath.incrementDepthBy(getName(), depth) + depth;
            for (int i = 0; i < this.contents.size(); i++) {
                Content content = this.contents.get(i);
                if (content instanceof KeyPathElement) {
                    KeyPathElement element = (KeyPathElement) content;
                    element.resolveKeyPath(keyPath, newDepth, accumulator, currentPartialKeyPath);
                }
            }
        }
    }

    @Override // com.airbnb.lottie.model.KeyPathElement
    public <T> void addValueCallback(T property, LottieValueCallback<T> callback) {
        TransformKeyframeAnimation transformKeyframeAnimation = this.transformAnimation;
        if (transformKeyframeAnimation != null) {
            transformKeyframeAnimation.applyValueCallback(property, callback);
        }
    }
}
