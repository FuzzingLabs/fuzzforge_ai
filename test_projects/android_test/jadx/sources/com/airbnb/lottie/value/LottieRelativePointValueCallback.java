package com.airbnb.lottie.value;

import android.graphics.PointF;
import com.airbnb.lottie.utils.MiscUtils;

/* loaded from: classes.dex */
public class LottieRelativePointValueCallback extends LottieValueCallback<PointF> {
    private final PointF point;

    public LottieRelativePointValueCallback() {
        this.point = new PointF();
    }

    public LottieRelativePointValueCallback(PointF staticValue) {
        super(staticValue);
        this.point = new PointF();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.airbnb.lottie.value.LottieValueCallback
    public final PointF getValue(LottieFrameInfo<PointF> frameInfo) {
        this.point.set(MiscUtils.lerp(frameInfo.getStartValue().x, frameInfo.getEndValue().x, frameInfo.getInterpolatedKeyframeProgress()), MiscUtils.lerp(frameInfo.getStartValue().y, frameInfo.getEndValue().y, frameInfo.getInterpolatedKeyframeProgress()));
        PointF offset = getOffset(frameInfo);
        this.point.offset(offset.x, offset.y);
        return this.point;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public PointF getOffset(LottieFrameInfo<PointF> frameInfo) {
        if (this.value == 0) {
            throw new IllegalArgumentException("You must provide a static value in the constructor , call setValue, or override getValue.");
        }
        return (PointF) this.value;
    }
}
