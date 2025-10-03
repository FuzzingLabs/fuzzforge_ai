package com.airbnb.lottie.value;

import com.airbnb.lottie.utils.MiscUtils;

/* loaded from: classes.dex */
public class LottieRelativeIntegerValueCallback extends LottieValueCallback<Integer> {
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.airbnb.lottie.value.LottieValueCallback
    public Integer getValue(LottieFrameInfo<Integer> frameInfo) {
        int originalValue = MiscUtils.lerp(frameInfo.getStartValue().intValue(), frameInfo.getEndValue().intValue(), frameInfo.getInterpolatedKeyframeProgress());
        int newValue = getOffset(frameInfo).intValue();
        return Integer.valueOf(originalValue + newValue);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public Integer getOffset(LottieFrameInfo<Integer> frameInfo) {
        if (this.value == 0) {
            throw new IllegalArgumentException("You must provide a static value in the constructor , call setValue, or override getValue.");
        }
        return (Integer) this.value;
    }
}
