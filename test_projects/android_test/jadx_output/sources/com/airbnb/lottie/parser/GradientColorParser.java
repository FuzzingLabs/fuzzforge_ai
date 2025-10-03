package com.airbnb.lottie.parser;

import android.graphics.Color;
import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.MiscUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class GradientColorParser implements ValueParser<GradientColor> {
    private int colorPoints;

    public GradientColorParser(int colorPoints) {
        this.colorPoints = colorPoints;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.airbnb.lottie.parser.ValueParser
    public GradientColor parse(JsonReader reader, float scale) throws IOException {
        List<Float> array = new ArrayList<>();
        boolean isArray = reader.peek() == JsonReader.Token.BEGIN_ARRAY;
        if (isArray) {
            reader.beginArray();
        }
        while (reader.hasNext()) {
            array.add(Float.valueOf((float) reader.nextDouble()));
        }
        if (isArray) {
            reader.endArray();
        }
        if (this.colorPoints == -1) {
            this.colorPoints = array.size() / 4;
        }
        int i = this.colorPoints;
        float[] positions = new float[i];
        int[] colors = new int[i];
        int r = 0;
        int g = 0;
        for (int i2 = 0; i2 < this.colorPoints * 4; i2++) {
            int colorIndex = i2 / 4;
            double value = array.get(i2).floatValue();
            switch (i2 % 4) {
                case 0:
                    positions[colorIndex] = (float) value;
                    break;
                case 1:
                    r = (int) (255.0d * value);
                    break;
                case 2:
                    g = (int) (255.0d * value);
                    break;
                case 3:
                    int b = (int) (255.0d * value);
                    colors[colorIndex] = Color.argb(255, r, g, b);
                    break;
            }
        }
        GradientColor gradientColor = new GradientColor(positions, colors);
        addOpacityStopsToGradientIfNeeded(gradientColor, array);
        return gradientColor;
    }

    private void addOpacityStopsToGradientIfNeeded(GradientColor gradientColor, List<Float> array) {
        int startIndex = this.colorPoints * 4;
        if (array.size() <= startIndex) {
            return;
        }
        int opacityStops = (array.size() - startIndex) / 2;
        double[] positions = new double[opacityStops];
        double[] opacities = new double[opacityStops];
        int j = 0;
        for (int i = startIndex; i < array.size(); i++) {
            if (i % 2 == 0) {
                positions[j] = array.get(i).floatValue();
            } else {
                opacities[j] = array.get(i).floatValue();
                j++;
            }
        }
        for (int i2 = 0; i2 < gradientColor.getSize(); i2++) {
            int color = gradientColor.getColors()[i2];
            gradientColor.getColors()[i2] = Color.argb(getOpacityAtPosition(gradientColor.getPositions()[i2], positions, opacities), Color.red(color), Color.green(color), Color.blue(color));
        }
    }

    private int getOpacityAtPosition(double position, double[] positions, double[] opacities) {
        for (int i = 1; i < positions.length; i++) {
            double lastPosition = positions[i - 1];
            double thisPosition = positions[i];
            if (positions[i] >= position) {
                double progress = (position - lastPosition) / (thisPosition - lastPosition);
                return (int) (MiscUtils.lerp(opacities[i - 1], opacities[i], progress) * 255.0d);
            }
        }
        int i2 = opacities.length;
        return (int) (opacities[i2 - 1] * 255.0d);
    }
}
