package com.airbnb.lottie.parser;

import android.graphics.Color;
import android.graphics.PointF;
import com.airbnb.lottie.parser.moshi.JsonReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
class JsonUtils {
    private static final JsonReader.Options POINT_NAMES = JsonReader.Options.m16of("x", "y");

    private JsonUtils() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int jsonToColor(JsonReader reader) throws IOException {
        reader.beginArray();
        int r = (int) (reader.nextDouble() * 255.0d);
        int g = (int) (reader.nextDouble() * 255.0d);
        int b = (int) (reader.nextDouble() * 255.0d);
        while (reader.hasNext()) {
            reader.skipValue();
        }
        reader.endArray();
        return Color.argb(255, r, g, b);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static List<PointF> jsonToPoints(JsonReader reader, float scale) throws IOException {
        List<PointF> points = new ArrayList<>();
        reader.beginArray();
        while (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
            reader.beginArray();
            points.add(jsonToPoint(reader, scale));
            reader.endArray();
        }
        reader.endArray();
        return points;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.airbnb.lottie.parser.JsonUtils$1 */
    /* loaded from: classes.dex */
    public static /* synthetic */ class C06831 {
        static final /* synthetic */ int[] $SwitchMap$com$airbnb$lottie$parser$moshi$JsonReader$Token;

        static {
            int[] iArr = new int[JsonReader.Token.values().length];
            $SwitchMap$com$airbnb$lottie$parser$moshi$JsonReader$Token = iArr;
            try {
                iArr[JsonReader.Token.NUMBER.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$airbnb$lottie$parser$moshi$JsonReader$Token[JsonReader.Token.BEGIN_ARRAY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$airbnb$lottie$parser$moshi$JsonReader$Token[JsonReader.Token.BEGIN_OBJECT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static PointF jsonToPoint(JsonReader reader, float scale) throws IOException {
        switch (C06831.$SwitchMap$com$airbnb$lottie$parser$moshi$JsonReader$Token[reader.peek().ordinal()]) {
            case 1:
                return jsonNumbersToPoint(reader, scale);
            case 2:
                return jsonArrayToPoint(reader, scale);
            case 3:
                return jsonObjectToPoint(reader, scale);
            default:
                throw new IllegalArgumentException("Unknown point starts with " + reader.peek());
        }
    }

    private static PointF jsonNumbersToPoint(JsonReader reader, float scale) throws IOException {
        float x = (float) reader.nextDouble();
        float y = (float) reader.nextDouble();
        while (reader.hasNext()) {
            reader.skipValue();
        }
        return new PointF(x * scale, y * scale);
    }

    private static PointF jsonArrayToPoint(JsonReader reader, float scale) throws IOException {
        reader.beginArray();
        float x = (float) reader.nextDouble();
        float y = (float) reader.nextDouble();
        while (reader.peek() != JsonReader.Token.END_ARRAY) {
            reader.skipValue();
        }
        reader.endArray();
        return new PointF(x * scale, y * scale);
    }

    private static PointF jsonObjectToPoint(JsonReader reader, float scale) throws IOException {
        float x = 0.0f;
        float y = 0.0f;
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.selectName(POINT_NAMES)) {
                case 0:
                    x = valueFromObject(reader);
                    break;
                case 1:
                    y = valueFromObject(reader);
                    break;
                default:
                    reader.skipName();
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new PointF(x * scale, y * scale);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static float valueFromObject(JsonReader reader) throws IOException {
        JsonReader.Token token = reader.peek();
        switch (C06831.$SwitchMap$com$airbnb$lottie$parser$moshi$JsonReader$Token[token.ordinal()]) {
            case 1:
                return (float) reader.nextDouble();
            case 2:
                reader.beginArray();
                float val = (float) reader.nextDouble();
                while (reader.hasNext()) {
                    reader.skipValue();
                }
                reader.endArray();
                return val;
            default:
                throw new IllegalArgumentException("Unknown value for token of type " + token);
        }
    }
}
