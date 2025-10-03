package com.airbnb.lottie.value;

import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;

/* loaded from: classes.dex */
public class LottieValueCallback<T> {
    private BaseKeyframeAnimation<?, ?> animation;
    private final LottieFrameInfo<T> frameInfo;
    protected T value;

    public LottieValueCallback() {
        this.frameInfo = new LottieFrameInfo<>();
        this.value = null;
    }

    public LottieValueCallback(T staticValue) {
        this.frameInfo = new LottieFrameInfo<>();
        this.value = null;
        this.value = staticValue;
    }

    public T getValue(LottieFrameInfo<T> frameInfo) {
        return this.value;
    }

    public final void setValue(T value) {
        this.value = value;
        BaseKeyframeAnimation<?, ?> baseKeyframeAnimation = this.animation;
        if (baseKeyframeAnimation != null) {
            baseKeyframeAnimation.notifyListeners();
        }
    }

    public final T getValueInternal(float startFrame, float endFrame, T startValue, T endValue, float linearKeyframeProgress, float interpolatedKeyframeProgress, float overallProgress) {
        return getValue(this.frameInfo.set(startFrame, endFrame, startValue, endValue, linearKeyframeProgress, interpolatedKeyframeProgress, overallProgress));
    }

    public final void setAnimation(BaseKeyframeAnimation<?, ?> animation) {
        this.animation = animation;
    }
}
