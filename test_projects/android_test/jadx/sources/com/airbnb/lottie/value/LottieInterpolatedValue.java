package com.airbnb.lottie.value;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/* loaded from: classes.dex */
abstract class LottieInterpolatedValue<T> extends LottieValueCallback<T> {
    private final T endValue;
    private final Interpolator interpolator;
    private final T startValue;

    abstract T interpolateValue(T t, T t2, float f);

    LottieInterpolatedValue(T startValue, T endValue) {
        this(startValue, endValue, new LinearInterpolator());
    }

    LottieInterpolatedValue(T startValue, T endValue, Interpolator interpolator) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.interpolator = interpolator;
    }

    @Override // com.airbnb.lottie.value.LottieValueCallback
    public T getValue(LottieFrameInfo<T> frameInfo) {
        float progress = this.interpolator.getInterpolation(frameInfo.getOverallProgress());
        return interpolateValue(this.startValue, this.endValue, progress);
    }
}
