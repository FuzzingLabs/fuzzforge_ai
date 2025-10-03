package com.airbnb.lottie.animation.keyframe;

import android.graphics.PointF;
import com.airbnb.lottie.value.Keyframe;
import java.util.List;

/* loaded from: classes.dex */
public class PointKeyframeAnimation extends KeyframeAnimation<PointF> {
    private final PointF point;

    @Override // com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation
    public /* bridge */ /* synthetic */ Object getValue(Keyframe keyframe, float f) {
        return getValue((Keyframe<PointF>) keyframe, f);
    }

    public PointKeyframeAnimation(List<Keyframe<PointF>> keyframes) {
        super(keyframes);
        this.point = new PointF();
    }

    @Override // com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation
    public PointF getValue(Keyframe<PointF> keyframe, float keyframeProgress) {
        PointF value;
        if (keyframe.startValue == null || keyframe.endValue == null) {
            throw new IllegalStateException("Missing values for keyframe.");
        }
        PointF startPoint = keyframe.startValue;
        PointF endPoint = keyframe.endValue;
        if (this.valueCallback != null && (value = (PointF) this.valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame.floatValue(), startPoint, endPoint, keyframeProgress, getLinearCurrentKeyframeProgress(), getProgress())) != null) {
            return value;
        }
        this.point.set(startPoint.x + ((endPoint.x - startPoint.x) * keyframeProgress), startPoint.y + ((endPoint.y - startPoint.y) * keyframeProgress));
        return this.point;
    }
}
