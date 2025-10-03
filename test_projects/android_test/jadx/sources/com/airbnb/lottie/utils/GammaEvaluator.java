package com.airbnb.lottie.utils;

/* loaded from: classes.dex */
public class GammaEvaluator {
    private static float OECF_sRGB(float linear) {
        return linear <= 0.0031308f ? 12.92f * linear : (float) ((Math.pow(linear, 0.4166666567325592d) * 1.0549999475479126d) - 0.054999999701976776d);
    }

    private static float EOCF_sRGB(float srgb) {
        return srgb <= 0.04045f ? srgb / 12.92f : (float) Math.pow((0.055f + srgb) / 1.055f, 2.4000000953674316d);
    }

    public static int evaluate(float fraction, int startInt, int endInt) {
        if (startInt == endInt) {
            return startInt;
        }
        float startA = ((startInt >> 24) & 255) / 255.0f;
        float endA = ((endInt >> 24) & 255) / 255.0f;
        float endR = ((endInt >> 16) & 255) / 255.0f;
        float endG = ((endInt >> 8) & 255) / 255.0f;
        float endB = (endInt & 255) / 255.0f;
        float startR = EOCF_sRGB(((startInt >> 16) & 255) / 255.0f);
        float startG = EOCF_sRGB(((startInt >> 8) & 255) / 255.0f);
        float startB = EOCF_sRGB((startInt & 255) / 255.0f);
        float a = ((endA - startA) * fraction) + startA;
        float r = ((EOCF_sRGB(endR) - startR) * fraction) + startR;
        float g = ((EOCF_sRGB(endG) - startG) * fraction) + startG;
        float b = ((EOCF_sRGB(endB) - startB) * fraction) + startB;
        float r2 = OECF_sRGB(r) * 255.0f;
        float r3 = OECF_sRGB(g);
        float g2 = r3 * 255.0f;
        float g3 = OECF_sRGB(b);
        float b2 = g3 * 255.0f;
        return (Math.round(a * 255.0f) << 24) | (Math.round(r2) << 16) | (Math.round(g2) << 8) | Math.round(b2);
    }
}
