package com.airbnb.lottie.model;

/* loaded from: classes.dex */
public class Font {
    private final float ascent;
    private final String family;
    private final String name;
    private final String style;

    public Font(String family, String name, String style, float ascent) {
        this.family = family;
        this.name = name;
        this.style = style;
        this.ascent = ascent;
    }

    public String getFamily() {
        return this.family;
    }

    public String getName() {
        return this.name;
    }

    public String getStyle() {
        return this.style;
    }

    float getAscent() {
        return this.ascent;
    }
}
