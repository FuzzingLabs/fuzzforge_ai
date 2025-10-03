package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.value.ScaleXY;
import java.util.List;

/* loaded from: classes.dex */
public class ScaleKeyframeAnimation extends KeyframeAnimation<ScaleXY> {
    private final ScaleXY scaleXY;

    @Override // com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation
    public /* bridge */ /* synthetic */ Object getValue(Keyframe keyframe, float f) {
        return getValue((Keyframe<ScaleXY>) keyframe, f);
    }

    public ScaleKeyframeAnimation(List<Keyframe<ScaleXY>> keyframes) {
        super(keyframes);
        this.scaleXY = new ScaleXY();
    }

    @Override // com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation
    public ScaleXY getValue(Keyframe<ScaleXY> keyframe, float keyframeProgress) {
        ScaleXY value;
        if (keyframe.startValue == null || keyframe.endValue == null) {
            throw new IllegalStateException("Missing values for keyframe.");
        }
        ScaleXY startTransform = keyframe.startValue;
        ScaleXY endTransform = keyframe.endValue;
        if (this.valueCallback != null && (value = (ScaleXY) this.valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame.floatValue(), startTransform, endTransform, keyframeProgress, getLinearCurrentKeyframeProgress(), getProgress())) != null) {
            return value;
        }
        this.scaleXY.set(MiscUtils.lerp(startTransform.getScaleX(), endTransform.getScaleX(), keyframeProgress), MiscUtils.lerp(startTransform.getScaleY(), endTransform.getScaleY(), keyframeProgress));
        return this.scaleXY;
    }
}
