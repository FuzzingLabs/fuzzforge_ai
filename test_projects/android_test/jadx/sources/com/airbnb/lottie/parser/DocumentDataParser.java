package com.airbnb.lottie.parser;

import com.airbnb.lottie.model.DocumentData;
import com.airbnb.lottie.parser.moshi.JsonReader;
import java.io.IOException;

/* loaded from: classes.dex */
public class DocumentDataParser implements ValueParser<DocumentData> {
    public static final DocumentDataParser INSTANCE = new DocumentDataParser();
    private static final JsonReader.Options NAMES = JsonReader.Options.m16of("t", "f", "s", "j", "tr", "lh", "ls", "fc", "sc", "sw", "of");

    private DocumentDataParser() {
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.airbnb.lottie.parser.ValueParser
    public DocumentData parse(JsonReader reader, float scale) throws IOException {
        String text = null;
        String fontName = null;
        float size = 0.0f;
        DocumentData.Justification justification = DocumentData.Justification.CENTER;
        int tracking = 0;
        float lineHeight = 0.0f;
        float baselineShift = 0.0f;
        int fillColor = 0;
        int strokeColor = 0;
        float strokeWidth = 0.0f;
        boolean strokeOverFill = true;
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.selectName(NAMES)) {
                case 0:
                    text = reader.nextString();
                    break;
                case 1:
                    fontName = reader.nextString();
                    break;
                case 2:
                    size = (float) reader.nextDouble();
                    break;
                case 3:
                    int justificationInt = reader.nextInt();
                    if (justificationInt > DocumentData.Justification.CENTER.ordinal() || justificationInt < 0) {
                        justification = DocumentData.Justification.CENTER;
                        break;
                    } else {
                        justification = DocumentData.Justification.values()[justificationInt];
                        break;
                    }
                    break;
                case 4:
                    tracking = reader.nextInt();
                    break;
                case 5:
                    lineHeight = (float) reader.nextDouble();
                    break;
                case 6:
                    baselineShift = (float) reader.nextDouble();
                    break;
                case 7:
                    fillColor = JsonUtils.jsonToColor(reader);
                    break;
                case 8:
                    strokeColor = JsonUtils.jsonToColor(reader);
                    break;
                case 9:
                    strokeWidth = (float) reader.nextDouble();
                    break;
                case 10:
                    strokeOverFill = reader.nextBoolean();
                    break;
                default:
                    reader.skipName();
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new DocumentData(text, fontName, size, justification, tracking, lineHeight, baselineShift, fillColor, strokeColor, strokeWidth, strokeOverFill);
    }
}
