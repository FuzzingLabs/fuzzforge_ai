package com.airbnb.lottie.model.content;

import com.airbnb.lottie.utils.GammaEvaluator;
import com.airbnb.lottie.utils.MiscUtils;

/* loaded from: classes.dex */
public class GradientColor {
    private final int[] colors;
    private final float[] positions;

    public GradientColor(float[] positions, int[] colors) {
        this.positions = positions;
        this.colors = colors;
    }

    public float[] getPositions() {
        return this.positions;
    }

    public int[] getColors() {
        return this.colors;
    }

    public int getSize() {
        return this.colors.length;
    }

    public void lerp(GradientColor gc1, GradientColor gc2, float progress) {
        if (gc1.colors.length != gc2.colors.length) {
            throw new IllegalArgumentException("Cannot interpolate between gradients. Lengths vary (" + gc1.colors.length + " vs " + gc2.colors.length + ")");
        }
        for (int i = 0; i < gc1.colors.length; i++) {
            this.positions[i] = MiscUtils.lerp(gc1.positions[i], gc2.positions[i], progress);
            this.colors[i] = GammaEvaluator.evaluate(progress, gc1.colors[i], gc2.colors[i]);
        }
    }
}
