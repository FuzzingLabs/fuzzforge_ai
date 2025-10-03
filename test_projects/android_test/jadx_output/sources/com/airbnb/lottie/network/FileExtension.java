package com.airbnb.lottie.network;

import com.airbnb.lottie.utils.Logger;

/* loaded from: classes.dex */
public enum FileExtension {
    JSON(".json"),
    ZIP(".zip");

    public final String extension;

    FileExtension(String extension) {
        this.extension = extension;
    }

    public String tempExtension() {
        return ".temp" + this.extension;
    }

    @Override // java.lang.Enum
    public String toString() {
        return this.extension;
    }

    public static FileExtension forFile(String filename) {
        for (FileExtension e : values()) {
            if (filename.endsWith(e.extension)) {
                return e;
            }
        }
        Logger.warning("Unable to find correct extension for " + filename);
        return JSON;
    }
}
