package com.airbnb.lottie.parser;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableShapeValue;
import com.airbnb.lottie.model.content.ShapePath;
import com.airbnb.lottie.parser.moshi.JsonReader;
import java.io.IOException;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ShapePathParser {
    static JsonReader.Options NAMES = JsonReader.Options.m16of("nm", "ind", "ks", "hd");

    private ShapePathParser() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static ShapePath parse(JsonReader reader, LottieComposition composition) throws IOException {
        String name = null;
        int ind = 0;
        AnimatableShapeValue shape = null;
        boolean hidden = false;
        while (reader.hasNext()) {
            switch (reader.selectName(NAMES)) {
                case 0:
                    name = reader.nextString();
                    break;
                case 1:
                    ind = reader.nextInt();
                    break;
                case 2:
                    shape = AnimatableValueParser.parseShapeData(reader, composition);
                    break;
                case 3:
                    hidden = reader.nextBoolean();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        return new ShapePath(name, ind, shape, hidden);
    }
}
