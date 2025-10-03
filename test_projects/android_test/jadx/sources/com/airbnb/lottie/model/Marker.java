package com.airbnb.lottie.model;

/* loaded from: classes.dex */
public class Marker {
    private static String CARRIAGE_RETURN = "\r";
    public final float durationFrames;
    private final String name;
    public final float startFrame;

    public Marker(String name, float startFrame, float durationFrames) {
        this.name = name;
        this.durationFrames = durationFrames;
        this.startFrame = startFrame;
    }

    public boolean matchesName(String name) {
        if (this.name.equalsIgnoreCase(name)) {
            return true;
        }
        if (this.name.endsWith(CARRIAGE_RETURN)) {
            String str = this.name;
            if (str.substring(0, str.length() - 1).equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
