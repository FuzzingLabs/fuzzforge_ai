package com.airbnb.lottie.parser;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.Logger;
import java.io.IOException;

/* loaded from: classes.dex */
class ContentModelParser {
    private static JsonReader.Options NAMES = JsonReader.Options.m16of("ty", "d");

    private ContentModelParser() {
    }

    static ContentModel parse(JsonReader reader, LottieComposition composition) throws IOException {
        ContentModel model;
        String type = null;
        reader.beginObject();
        int d = 2;
        while (true) {
            if (reader.hasNext()) {
                switch (reader.selectName(NAMES)) {
                    case 0:
                        type = reader.nextString();
                        break;
                    case 1:
                        d = reader.nextInt();
                        continue;
                    default:
                        reader.skipName();
                        reader.skipValue();
                        continue;
                }
            }
        }
        if (type == null) {
            return null;
        }
        model = null;
        switch (type) {
            default:
                Logger.warning("Unknown shape type " + type);
                break;
            case "gr":
                model = ShapeGroupParser.parse(reader, composition);
                break;
            case "st":
                model = ShapeStrokeParser.parse(reader, composition);
                break;
            case "gs":
                model = GradientStrokeParser.parse(reader, composition);
                break;
            case "fl":
                model = ShapeFillParser.parse(reader, composition);
                break;
            case "gf":
                model = GradientFillParser.parse(reader, composition);
                break;
            case "tr":
                model = AnimatableTransformParser.parse(reader, composition);
                break;
            case "sh":
                model = ShapePathParser.parse(reader, composition);
                break;
            case "el":
                model = CircleShapeParser.parse(reader, composition, d);
                break;
            case "rc":
                model = RectangleShapeParser.parse(reader, composition);
                break;
            case "tm":
                model = ShapeTrimPathParser.parse(reader, composition);
                break;
            case "sr":
                model = PolystarShapeParser.parse(reader, composition);
                break;
            case "mm":
                model = MergePathsParser.parse(reader);
                composition.addWarning("Animation contains merge paths. Merge paths are only supported on KitKat+ and must be manually enabled by calling enableMergePathsForKitKatAndAbove().");
                break;
            case "rp":
                model = RepeaterParser.parse(reader, composition);
                break;
        }
        while (reader.hasNext()) {
            reader.skipValue();
        }
        reader.endObject();
        return model;
    }
}
