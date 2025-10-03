package com.airbnb.lottie.parser;

import android.graphics.PointF;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePathValue;
import com.airbnb.lottie.model.animatable.AnimatableScaleValue;
import com.airbnb.lottie.model.animatable.AnimatableSplitDimensionPathValue;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.value.ScaleXY;
import java.io.IOException;

/* loaded from: classes.dex */
public class AnimatableTransformParser {
    private static JsonReader.Options NAMES = JsonReader.Options.m16of("a", "p", "s", "rz", "r", "o", "so", "eo", "sk", "sa");
    private static JsonReader.Options ANIMATABLE_NAMES = JsonReader.Options.m16of("k");

    private AnimatableTransformParser() {
    }

    public static AnimatableTransform parse(JsonReader reader, LottieComposition composition) throws IOException {
        AnimatableFloatValue skew;
        AnimatableFloatValue skewAngle;
        boolean z;
        LottieComposition lottieComposition = composition;
        AnimatableFloatValue rotation = null;
        boolean z2 = false;
        boolean isObject = reader.peek() == JsonReader.Token.BEGIN_OBJECT;
        if (isObject) {
            reader.beginObject();
        }
        AnimatableScaleValue scale = null;
        AnimatableIntegerValue opacity = null;
        AnimatableFloatValue startOpacity = null;
        AnimatableFloatValue endOpacity = null;
        AnimatableFloatValue skew2 = null;
        AnimatableFloatValue skewAngle2 = null;
        AnimatablePathValue anchorPoint = null;
        AnimatableValue<PointF, PointF> position = null;
        while (reader.hasNext()) {
            switch (reader.selectName(NAMES)) {
                case 0:
                    boolean z3 = z2;
                    reader.beginObject();
                    while (reader.hasNext()) {
                        switch (reader.selectName(ANIMATABLE_NAMES)) {
                            case 0:
                                anchorPoint = AnimatablePathValueParser.parse(reader, composition);
                                break;
                            default:
                                reader.skipName();
                                reader.skipValue();
                                break;
                        }
                    }
                    reader.endObject();
                    lottieComposition = composition;
                    z2 = z3;
                    continue;
                case 1:
                    position = AnimatablePathValueParser.parseSplitPath(reader, composition);
                    lottieComposition = composition;
                    continue;
                case 2:
                    scale = AnimatableValueParser.parseScale(reader, composition);
                    lottieComposition = composition;
                    continue;
                case 3:
                    lottieComposition.addWarning("Lottie doesn't support 3D layers.");
                    break;
                case 4:
                    break;
                case 5:
                    opacity = AnimatableValueParser.parseInteger(reader, composition);
                    continue;
                case 6:
                    startOpacity = AnimatableValueParser.parseFloat(reader, lottieComposition, z2);
                    continue;
                case 7:
                    endOpacity = AnimatableValueParser.parseFloat(reader, lottieComposition, z2);
                    continue;
                case 8:
                    skew2 = AnimatableValueParser.parseFloat(reader, lottieComposition, z2);
                    continue;
                case 9:
                    skewAngle2 = AnimatableValueParser.parseFloat(reader, lottieComposition, z2);
                    continue;
                default:
                    reader.skipName();
                    reader.skipValue();
                    lottieComposition = composition;
                    continue;
            }
            AnimatableFloatValue rotation2 = AnimatableValueParser.parseFloat(reader, lottieComposition, z2);
            if (rotation2.getKeyframes().isEmpty()) {
                rotation2.getKeyframes().add(new Keyframe(composition, Float.valueOf(0.0f), Float.valueOf(0.0f), null, 0.0f, Float.valueOf(composition.getEndFrame())));
                z = false;
            } else if (((Keyframe) rotation2.getKeyframes().get(0)).startValue != 0) {
                z = false;
            } else {
                z = false;
                rotation2.getKeyframes().set(0, new Keyframe(composition, Float.valueOf(0.0f), Float.valueOf(0.0f), null, 0.0f, Float.valueOf(composition.getEndFrame())));
            }
            lottieComposition = composition;
            z2 = z;
            rotation = rotation2;
        }
        if (isObject) {
            reader.endObject();
        }
        if (isAnchorPointIdentity(anchorPoint)) {
            anchorPoint = null;
        }
        if (isPositionIdentity(position)) {
            position = null;
        }
        if (isRotationIdentity(rotation)) {
            rotation = null;
        }
        if (isScaleIdentity(scale)) {
            scale = null;
        }
        if (!isSkewIdentity(skew2)) {
            skew = skew2;
        } else {
            skew = null;
        }
        if (!isSkewAngleIdentity(skewAngle2)) {
            skewAngle = skewAngle2;
        } else {
            skewAngle = null;
        }
        return new AnimatableTransform(anchorPoint, position, scale, rotation, opacity, startOpacity, endOpacity, skew, skewAngle);
    }

    private static boolean isAnchorPointIdentity(AnimatablePathValue anchorPoint) {
        return anchorPoint == null || (anchorPoint.isStatic() && anchorPoint.getKeyframes().get(0).startValue.equals(0.0f, 0.0f));
    }

    private static boolean isPositionIdentity(AnimatableValue<PointF, PointF> position) {
        return position == null || (!(position instanceof AnimatableSplitDimensionPathValue) && position.isStatic() && position.getKeyframes().get(0).startValue.equals(0.0f, 0.0f));
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static boolean isRotationIdentity(AnimatableFloatValue rotation) {
        return rotation == null || (rotation.isStatic() && ((Float) ((Keyframe) rotation.getKeyframes().get(0)).startValue).floatValue() == 0.0f);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static boolean isScaleIdentity(AnimatableScaleValue scale) {
        return scale == null || (scale.isStatic() && ((ScaleXY) ((Keyframe) scale.getKeyframes().get(0)).startValue).equals(1.0f, 1.0f));
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static boolean isSkewIdentity(AnimatableFloatValue skew) {
        return skew == null || (skew.isStatic() && ((Float) ((Keyframe) skew.getKeyframes().get(0)).startValue).floatValue() == 0.0f);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static boolean isSkewAngleIdentity(AnimatableFloatValue skewAngle) {
        return skewAngle == null || (skewAngle.isStatic() && ((Float) ((Keyframe) skewAngle.getKeyframes().get(0)).startValue).floatValue() == 0.0f);
    }
}
