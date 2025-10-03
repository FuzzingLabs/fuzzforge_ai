package com.airbnb.lottie.value;

/* loaded from: classes.dex */
public class LottieFrameInfo<T> {
    private float endFrame;
    private T endValue;
    private float interpolatedKeyframeProgress;
    private float linearKeyframeProgress;
    private float overallProgress;
    private float startFrame;
    private T startValue;

    public LottieFrameInfo<T> set(float startFrame, float endFrame, T startValue, T endValue, float linearKeyframeProgress, float interpolatedKeyframeProgress, float overallProgress) {
        this.startFrame = startFrame;
        this.endFrame = endFrame;
        this.startValue = startValue;
        this.endValue = endValue;
        this.linearKeyframeProgress = linearKeyframeProgress;
        this.interpolatedKeyframeProgress = interpolatedKeyframeProgress;
        this.overallProgress = overallProgress;
        return this;
    }

    public float getStartFrame() {
        return this.startFrame;
    }

    public float getEndFrame() {
        return this.endFrame;
    }

    public T getStartValue() {
        return this.startValue;
    }

    public T getEndValue() {
        return this.endValue;
    }

    public float getLinearKeyframeProgress() {
        return this.linearKeyframeProgress;
    }

    public float getInterpolatedKeyframeProgress() {
        return this.interpolatedKeyframeProgress;
    }

    public float getOverallProgress() {
        return this.overallProgress;
    }
}
