package com.airbnb.lottie.parser;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.content.GradientStroke;
import com.airbnb.lottie.model.content.GradientType;
import com.airbnb.lottie.model.content.ShapeStroke;
import com.airbnb.lottie.parser.moshi.JsonReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class GradientStrokeParser {
    private static JsonReader.Options NAMES = JsonReader.Options.m16of("nm", "g", "o", "t", "s", "e", "w", "lc", "lj", "ml", "hd", "d");
    private static final JsonReader.Options GRADIENT_NAMES = JsonReader.Options.m16of("p", "k");
    private static final JsonReader.Options DASH_PATTERN_NAMES = JsonReader.Options.m16of("n", "v");

    private GradientStrokeParser() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static GradientStroke parse(JsonReader reader, LottieComposition composition) throws IOException {
        String name = null;
        AnimatableGradientColorValue color = null;
        AnimatableIntegerValue opacity = null;
        GradientType gradientType = null;
        AnimatablePointValue startPoint = null;
        AnimatablePointValue endPoint = null;
        AnimatableFloatValue width = null;
        ShapeStroke.LineCapType capType = null;
        ShapeStroke.LineJoinType joinType = null;
        AnimatableFloatValue offset = null;
        float miterLimit = 0.0f;
        boolean hidden = false;
        List<AnimatableFloatValue> lineDashPattern = new ArrayList<>();
        while (reader.hasNext()) {
            boolean hidden2 = hidden;
            switch (reader.selectName(NAMES)) {
                case 0:
                    name = reader.nextString();
                    hidden = hidden2;
                    break;
                case 1:
                    List<AnimatableFloatValue> lineDashPattern2 = lineDashPattern;
                    int points = -1;
                    reader.beginObject();
                    while (reader.hasNext()) {
                        switch (reader.selectName(GRADIENT_NAMES)) {
                            case 0:
                                points = reader.nextInt();
                                break;
                            case 1:
                                color = AnimatableValueParser.parseGradientColor(reader, composition, points);
                                break;
                            default:
                                reader.skipName();
                                reader.skipValue();
                                break;
                        }
                    }
                    reader.endObject();
                    hidden = hidden2;
                    lineDashPattern = lineDashPattern2;
                    break;
                case 2:
                    opacity = AnimatableValueParser.parseInteger(reader, composition);
                    hidden = hidden2;
                    break;
                case 3:
                    List<AnimatableFloatValue> lineDashPattern3 = lineDashPattern;
                    gradientType = reader.nextInt() == 1 ? GradientType.LINEAR : GradientType.RADIAL;
                    hidden = hidden2;
                    lineDashPattern = lineDashPattern3;
                    break;
                case 4:
                    startPoint = AnimatableValueParser.parsePoint(reader, composition);
                    hidden = hidden2;
                    break;
                case 5:
                    endPoint = AnimatableValueParser.parsePoint(reader, composition);
                    hidden = hidden2;
                    break;
                case 6:
                    width = AnimatableValueParser.parseFloat(reader, composition);
                    hidden = hidden2;
                    break;
                case 7:
                    capType = ShapeStroke.LineCapType.values()[reader.nextInt() - 1];
                    hidden = hidden2;
                    lineDashPattern = lineDashPattern;
                    break;
                case 8:
                    joinType = ShapeStroke.LineJoinType.values()[reader.nextInt() - 1];
                    hidden = hidden2;
                    lineDashPattern = lineDashPattern;
                    break;
                case 9:
                    miterLimit = (float) reader.nextDouble();
                    hidden = hidden2;
                    lineDashPattern = lineDashPattern;
                    break;
                case 10:
                    hidden = reader.nextBoolean();
                    break;
                case 11:
                    reader.beginArray();
                    while (reader.hasNext()) {
                        String n = null;
                        AnimatableFloatValue val = null;
                        reader.beginObject();
                        while (reader.hasNext()) {
                            switch (reader.selectName(DASH_PATTERN_NAMES)) {
                                case 0:
                                    n = reader.nextString();
                                    break;
                                case 1:
                                    val = AnimatableValueParser.parseFloat(reader, composition);
                                    break;
                                default:
                                    reader.skipName();
                                    reader.skipValue();
                                    break;
                            }
                        }
                        reader.endObject();
                        if (n.equals("o")) {
                            offset = val;
                        } else if (n.equals("d") || n.equals("g")) {
                            composition.setHasDashPattern(true);
                            lineDashPattern.add(val);
                        }
                    }
                    reader.endArray();
                    if (lineDashPattern.size() == 1) {
                        lineDashPattern.add(lineDashPattern.get(0));
                    }
                    hidden = hidden2;
                    break;
                default:
                    reader.skipName();
                    reader.skipValue();
                    hidden = hidden2;
                    break;
            }
        }
        return new GradientStroke(name, gradientType, color, opacity, startPoint, endPoint, width, capType, joinType, miterLimit, lineDashPattern, offset, hidden);
    }
}
