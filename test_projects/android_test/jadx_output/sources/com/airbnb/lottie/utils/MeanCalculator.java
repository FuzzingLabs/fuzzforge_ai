package com.airbnb.lottie.utils;

/* loaded from: classes.dex */
public class MeanCalculator {

    /* renamed from: n */
    private int f122n;
    private float sum;

    public void add(float number) {
        float f = this.sum + number;
        this.sum = f;
        int i = this.f122n + 1;
        this.f122n = i;
        if (i == Integer.MAX_VALUE) {
            this.sum = f / 2.0f;
            this.f122n = i / 2;
        }
    }

    public float getMean() {
        int i = this.f122n;
        if (i == 0) {
            return 0.0f;
        }
        return this.sum / i;
    }
}
