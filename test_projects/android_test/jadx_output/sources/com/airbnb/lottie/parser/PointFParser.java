package com.airbnb.lottie.parser;

import android.graphics.PointF;
import com.airbnb.lottie.parser.moshi.JsonReader;
import java.io.IOException;

/* loaded from: classes.dex */
public class PointFParser implements ValueParser<PointF> {
    public static final PointFParser INSTANCE = new PointFParser();

    private PointFParser() {
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.airbnb.lottie.parser.ValueParser
    public PointF parse(JsonReader reader, float scale) throws IOException {
        JsonReader.Token token = reader.peek();
        if (token == JsonReader.Token.BEGIN_ARRAY) {
            return JsonUtils.jsonToPoint(reader, scale);
        }
        if (token == JsonReader.Token.BEGIN_OBJECT) {
            return JsonUtils.jsonToPoint(reader, scale);
        }
        if (token == JsonReader.Token.NUMBER) {
            PointF point = new PointF(((float) reader.nextDouble()) * scale, ((float) reader.nextDouble()) * scale);
            while (reader.hasNext()) {
                reader.skipValue();
            }
            return point;
        }
        throw new IllegalArgumentException("Cannot convert json to point. Next token is " + token);
    }
}
