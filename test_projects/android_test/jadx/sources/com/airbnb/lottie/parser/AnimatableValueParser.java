package com.airbnb.lottie.parser;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.animatable.AnimatableScaleValue;
import com.airbnb.lottie.model.animatable.AnimatableShapeValue;
import com.airbnb.lottie.model.animatable.AnimatableTextFrame;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.value.ScaleXY;
import java.io.IOException;
import java.util.List;

/* loaded from: classes.dex */
public class AnimatableValueParser {
    private AnimatableValueParser() {
    }

    public static AnimatableFloatValue parseFloat(JsonReader reader, LottieComposition composition) throws IOException {
        return parseFloat(reader, composition, true);
    }

    public static AnimatableFloatValue parseFloat(JsonReader reader, LottieComposition composition, boolean isDp) throws IOException {
        return new AnimatableFloatValue(parse(reader, isDp ? Utils.dpScale() : 1.0f, composition, FloatParser.INSTANCE));
    }

    static AnimatableIntegerValue parseInteger(JsonReader reader, LottieComposition composition) throws IOException {
        return new AnimatableIntegerValue(parse(reader, composition, IntegerParser.INSTANCE));
    }

    static AnimatablePointValue parsePoint(JsonReader reader, LottieComposition composition) throws IOException {
        return new AnimatablePointValue(parse(reader, Utils.dpScale(), composition, PointFParser.INSTANCE));
    }

    static AnimatableScaleValue parseScale(JsonReader reader, LottieComposition composition) throws IOException {
        return new AnimatableScaleValue((List<Keyframe<ScaleXY>>) parse(reader, composition, ScaleXYParser.INSTANCE));
    }

    static AnimatableShapeValue parseShapeData(JsonReader reader, LottieComposition composition) throws IOException {
        return new AnimatableShapeValue(parse(reader, Utils.dpScale(), composition, ShapeDataParser.INSTANCE));
    }

    static AnimatableTextFrame parseDocumentData(JsonReader reader, LottieComposition composition) throws IOException {
        return new AnimatableTextFrame(parse(reader, composition, DocumentDataParser.INSTANCE));
    }

    static AnimatableColorValue parseColor(JsonReader reader, LottieComposition composition) throws IOException {
        return new AnimatableColorValue(parse(reader, composition, ColorParser.INSTANCE));
    }

    static AnimatableGradientColorValue parseGradientColor(JsonReader reader, LottieComposition composition, int points) throws IOException {
        return new AnimatableGradientColorValue(parse(reader, composition, new GradientColorParser(points)));
    }

    private static <T> List<Keyframe<T>> parse(JsonReader reader, LottieComposition composition, ValueParser<T> valueParser) throws IOException {
        return KeyframesParser.parse(reader, composition, 1.0f, valueParser);
    }

    private static <T> List<Keyframe<T>> parse(JsonReader reader, float scale, LottieComposition composition, ValueParser<T> valueParser) throws IOException {
        return KeyframesParser.parse(reader, composition, scale, valueParser);
    }
}
