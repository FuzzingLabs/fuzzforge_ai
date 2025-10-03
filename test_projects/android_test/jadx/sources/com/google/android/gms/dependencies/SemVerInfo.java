package com.google.android.gms.dependencies;

import java.util.List;
import kotlin.Metadata;
import kotlin.TypeCastException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

/* compiled from: DataObjects.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\b\u0018\u0000 \u00152\u00020\u0001:\u0001\u0015B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003¢\u0006\u0002\u0010\u0006J\t\u0010\u000b\u001a\u00020\u0003HÆ\u0003J\t\u0010\f\u001a\u00020\u0003HÆ\u0003J\t\u0010\r\u001a\u00020\u0003HÆ\u0003J'\u0010\u000e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0012\u001a\u00020\u0003HÖ\u0001J\t\u0010\u0013\u001a\u00020\u0014HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\bR\u0011\u0010\u0005\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\b¨\u0006\u0016"}, m238d2 = {"Lcom/google/android/gms/dependencies/SemVerInfo;", "", "major", "", "minor", "patch", "(III)V", "getMajor", "()I", "getMinor", "getPatch", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "toString", "", "Companion", "strict-version-matcher-plugin"}, m239k = 1, m240mv = {1, 4, 0})
/* loaded from: classes.dex */
public final /* data */ class SemVerInfo {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private final int major;
    private final int minor;
    private final int patch;

    public static /* synthetic */ SemVerInfo copy$default(SemVerInfo semVerInfo, int i, int i2, int i3, int i4, Object obj) {
        if ((i4 & 1) != 0) {
            i = semVerInfo.major;
        }
        if ((i4 & 2) != 0) {
            i2 = semVerInfo.minor;
        }
        if ((i4 & 4) != 0) {
            i3 = semVerInfo.patch;
        }
        return semVerInfo.copy(i, i2, i3);
    }

    /* renamed from: component1, reason: from getter */
    public final int getMajor() {
        return this.major;
    }

    /* renamed from: component2, reason: from getter */
    public final int getMinor() {
        return this.minor;
    }

    /* renamed from: component3, reason: from getter */
    public final int getPatch() {
        return this.patch;
    }

    public final SemVerInfo copy(int major, int minor, int patch) {
        return new SemVerInfo(major, minor, patch);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SemVerInfo)) {
            return false;
        }
        SemVerInfo semVerInfo = (SemVerInfo) other;
        return this.major == semVerInfo.major && this.minor == semVerInfo.minor && this.patch == semVerInfo.patch;
    }

    public int hashCode() {
        return (((this.major * 31) + this.minor) * 31) + this.patch;
    }

    public String toString() {
        return "SemVerInfo(major=" + this.major + ", minor=" + this.minor + ", patch=" + this.patch + ")";
    }

    /* compiled from: DataObjects.kt */
    @Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006¨\u0006\u0007"}, m238d2 = {"Lcom/google/android/gms/dependencies/SemVerInfo$Companion;", "", "()V", "parseString", "Lcom/google/android/gms/dependencies/SemVerInfo;", "versionString", "", "strict-version-matcher-plugin"}, m239k = 1, m240mv = {1, 4, 0})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }

        public final SemVerInfo parseString(String versionString) {
            Intrinsics.checkNotNullParameter(versionString, "versionString");
            String version = StringsKt.trim((CharSequence) versionString).toString();
            List parts = StringsKt.split$default((CharSequence) version, new String[]{"."}, false, 0, 6, (Object) null);
            if (parts.size() != 3) {
                throw new IllegalArgumentException("Version string didn't have 3 parts divided by periods: " + versionString);
            }
            Integer major = Integer.valueOf((String) parts.get(0));
            Integer minor = Integer.valueOf((String) parts.get(1));
            String patchString = (String) parts.get(2);
            int dashIndex = StringsKt.indexOf$default((CharSequence) patchString, "-", 0, false, 6, (Object) null);
            if (dashIndex != -1) {
                if (patchString == null) {
                    throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
                }
                String substring = patchString.substring(0, dashIndex);
                Intrinsics.checkExpressionValueIsNotNull(substring, "(this as java.lang.Strin…ing(startIndex, endIndex)");
                patchString = substring;
            }
            Integer patch = Integer.valueOf(patchString);
            Intrinsics.checkNotNullExpressionValue(major, "major");
            int intValue = major.intValue();
            Intrinsics.checkNotNullExpressionValue(minor, "minor");
            int intValue2 = minor.intValue();
            Intrinsics.checkNotNullExpressionValue(patch, "patch");
            return new SemVerInfo(intValue, intValue2, patch.intValue());
        }
    }

    public SemVerInfo(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public final int getMajor() {
        return this.major;
    }

    public final int getMinor() {
        return this.minor;
    }

    public final int getPatch() {
        return this.patch;
    }
}
