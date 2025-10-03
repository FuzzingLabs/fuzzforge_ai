package com.airbnb.lottie.value;

import com.airbnb.lottie.utils.MiscUtils;

/* loaded from: classes.dex */
public class LottieRelativeFloatValueCallback extends LottieValueCallback<Float> {
    public LottieRelativeFloatValueCallback() {
    }

    public LottieRelativeFloatValueCallback(Float staticValue) {
        super(staticValue);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.airbnb.lottie.value.LottieValueCallback
    public Float getValue(LottieFrameInfo<Float> frameInfo) {
        float originalValue = MiscUtils.lerp(frameInfo.getStartValue().floatValue(), frameInfo.getEndValue().floatValue(), frameInfo.getInterpolatedKeyframeProgress());
        float offset = getOffset(frameInfo).floatValue();
        return Float.valueOf(originalValue + offset);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public Float getOffset(LottieFrameInfo<Float> frameInfo) {
        if (this.value == 0) {
            throw new IllegalArgumentException("You must provide a static value in the constructor , call setValue, or override getValue.");
        }
        return (Float) this.value;
    }
}
