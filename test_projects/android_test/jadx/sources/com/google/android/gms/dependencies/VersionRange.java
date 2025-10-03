package com.google.android.gms.dependencies;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

/* compiled from: Versions.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\b\u0086\b\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0006¢\u0006\u0002\u0010\bJ\t\u0010\u000f\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0010\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0011\u001a\u00020\u0006HÆ\u0003J\t\u0010\u0012\u001a\u00020\u0006HÆ\u0003J1\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u0006HÆ\u0001J\u0013\u0010\u0014\u001a\u00020\u00032\b\u0010\u0015\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0016\u001a\u00020\u0017HÖ\u0001J\t\u0010\u0018\u001a\u00020\u0019HÖ\u0001J\u0006\u0010\u001a\u001a\u00020\u0019J\u000e\u0010\u001b\u001a\u00020\u00032\u0006\u0010\u001c\u001a\u00020\u0006R\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\nR\u0011\u0010\u0007\u001a\u00020\u0006¢\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0005\u001a\u00020\u0006¢\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\r¨\u0006\u001e"}, m238d2 = {"Lcom/google/android/gms/dependencies/VersionRange;", "", "closedStart", "", "closedEnd", "rangeStart", "Lcom/google/android/gms/dependencies/Version;", "rangeEnd", "(ZZLcom/google/android/gms/dependencies/Version;Lcom/google/android/gms/dependencies/Version;)V", "getClosedEnd", "()Z", "getClosedStart", "getRangeEnd", "()Lcom/google/android/gms/dependencies/Version;", "getRangeStart", "component1", "component2", "component3", "component4", "copy", "equals", "other", "hashCode", "", "toString", "", "toVersionString", "versionInRange", "compareTo", "Companion", "strict-version-matcher-plugin"}, m239k = 1, m240mv = {1, 4, 0})
/* loaded from: classes.dex */
public final /* data */ class VersionRange {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private static final Pattern VERSION_RANGE_PATTERN = Pattern.compile("\\[(\\d+\\.)*(\\d+)+(-\\w)*\\]");
    private final boolean closedEnd;
    private final boolean closedStart;
    private final Version rangeEnd;
    private final Version rangeStart;

    public static /* synthetic */ VersionRange copy$default(VersionRange versionRange, boolean z, boolean z2, Version version, Version version2, int i, Object obj) {
        if ((i & 1) != 0) {
            z = versionRange.closedStart;
        }
        if ((i & 2) != 0) {
            z2 = versionRange.closedEnd;
        }
        if ((i & 4) != 0) {
            version = versionRange.rangeStart;
        }
        if ((i & 8) != 0) {
            version2 = versionRange.rangeEnd;
        }
        return versionRange.copy(z, z2, version, version2);
    }

    /* renamed from: component1, reason: from getter */
    public final boolean getClosedStart() {
        return this.closedStart;
    }

    /* renamed from: component2, reason: from getter */
    public final boolean getClosedEnd() {
        return this.closedEnd;
    }

    /* renamed from: component3, reason: from getter */
    public final Version getRangeStart() {
        return this.rangeStart;
    }

    /* renamed from: component4, reason: from getter */
    public final Version getRangeEnd() {
        return this.rangeEnd;
    }

    public final VersionRange copy(boolean closedStart, boolean closedEnd, Version rangeStart, Version rangeEnd) {
        Intrinsics.checkNotNullParameter(rangeStart, "rangeStart");
        Intrinsics.checkNotNullParameter(rangeEnd, "rangeEnd");
        return new VersionRange(closedStart, closedEnd, rangeStart, rangeEnd);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof VersionRange)) {
            return false;
        }
        VersionRange versionRange = (VersionRange) other;
        return this.closedStart == versionRange.closedStart && this.closedEnd == versionRange.closedEnd && Intrinsics.areEqual(this.rangeStart, versionRange.rangeStart) && Intrinsics.areEqual(this.rangeEnd, versionRange.rangeEnd);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [int] */
    /* JADX WARN: Type inference failed for: r0v8 */
    /* JADX WARN: Type inference failed for: r0v9 */
    public int hashCode() {
        boolean z = this.closedStart;
        ?? r0 = z;
        if (z) {
            r0 = 1;
        }
        int i = r0 * 31;
        boolean z2 = this.closedEnd;
        int i2 = (i + (z2 ? 1 : z2 ? 1 : 0)) * 31;
        Version version = this.rangeStart;
        int hashCode = (i2 + (version != null ? version.hashCode() : 0)) * 31;
        Version version2 = this.rangeEnd;
        return hashCode + (version2 != null ? version2.hashCode() : 0);
    }

    public String toString() {
        return "VersionRange(closedStart=" + this.closedStart + ", closedEnd=" + this.closedEnd + ", rangeStart=" + this.rangeStart + ", rangeEnd=" + this.rangeEnd + ")";
    }

    public VersionRange(boolean closedStart, boolean closedEnd, Version rangeStart, Version rangeEnd) {
        Intrinsics.checkNotNullParameter(rangeStart, "rangeStart");
        Intrinsics.checkNotNullParameter(rangeEnd, "rangeEnd");
        this.closedStart = closedStart;
        this.closedEnd = closedEnd;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    public final boolean getClosedEnd() {
        return this.closedEnd;
    }

    public final boolean getClosedStart() {
        return this.closedStart;
    }

    public final Version getRangeEnd() {
        return this.rangeEnd;
    }

    public final Version getRangeStart() {
        return this.rangeStart;
    }

    public final String toVersionString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.closedStart ? "[" : "(");
        sb.append(this.rangeStart.getTrimmedString());
        sb.append(",");
        sb.append(this.rangeEnd.getTrimmedString());
        sb.append(this.closedEnd ? "]" : ")");
        return sb.toString();
    }

    public final boolean versionInRange(Version compareTo) {
        Intrinsics.checkNotNullParameter(compareTo, "compareTo");
        if (this.closedStart) {
            if (INSTANCE.versionCompare(this.rangeStart.getTrimmedString(), compareTo.getTrimmedString()) > 0) {
                return false;
            }
        } else if (INSTANCE.versionCompare(this.rangeStart.getTrimmedString(), compareTo.getTrimmedString()) >= 0) {
            return false;
        }
        return this.closedEnd ? INSTANCE.versionCompare(this.rangeEnd.getTrimmedString(), compareTo.getTrimmedString()) >= 0 : INSTANCE.versionCompare(this.rangeEnd.getTrimmedString(), compareTo.getTrimmedString()) > 0;
    }

    /* compiled from: Versions.kt */
    @Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0010\u0010\b\u001a\u0004\u0018\u00010\t2\u0006\u0010\n\u001a\u00020\u000bJ\u0016\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u000f\u001a\u00020\u000bR\u0019\u0010\u0003\u001a\n \u0005*\u0004\u0018\u00010\u00040\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007¨\u0006\u0010"}, m238d2 = {"Lcom/google/android/gms/dependencies/VersionRange$Companion;", "", "()V", "VERSION_RANGE_PATTERN", "Ljava/util/regex/Pattern;", "kotlin.jvm.PlatformType", "getVERSION_RANGE_PATTERN", "()Ljava/util/regex/Pattern;", "fromString", "Lcom/google/android/gms/dependencies/VersionRange;", "versionRange", "", "versionCompare", "", "str1", "str2", "strict-version-matcher-plugin"}, m239k = 1, m240mv = {1, 4, 0})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }

        public final int versionCompare(String str1, String str2) {
            Intrinsics.checkNotNullParameter(str1, "str1");
            Intrinsics.checkNotNullParameter(str2, "str2");
            List vals1 = StringsKt.split$default((CharSequence) str1, new String[]{"\\."}, false, 0, 6, (Object) null);
            List vals2 = StringsKt.split$default((CharSequence) str2, new String[]{"\\."}, false, 0, 6, (Object) null);
            int i = 0;
            while (i < vals1.size() && i < vals2.size() && Intrinsics.areEqual((String) vals1.get(i), (String) vals2.get(i))) {
                i++;
            }
            if (i < vals1.size() && i < vals2.size()) {
                Integer valueOf = Integer.valueOf((String) vals1.get(i));
                Intrinsics.checkNotNullExpressionValue(valueOf, "Integer.valueOf(vals1[i])");
                int intValue = valueOf.intValue();
                Integer valueOf2 = Integer.valueOf((String) vals2.get(i));
                Intrinsics.checkNotNullExpressionValue(valueOf2, "Integer.valueOf(vals2[i])");
                int diff = Integer.compare(intValue, valueOf2.intValue());
                return Integer.signum(diff);
            }
            int diff2 = vals1.size();
            return Integer.signum(diff2 - vals2.size());
        }

        public final Pattern getVERSION_RANGE_PATTERN() {
            return VersionRange.VERSION_RANGE_PATTERN;
        }

        public final VersionRange fromString(String versionRange) {
            Version v;
            Intrinsics.checkNotNullParameter(versionRange, "versionRange");
            Matcher versionRangeMatcher = getVERSION_RANGE_PATTERN().matcher(versionRange);
            if (!versionRangeMatcher.matches() || (v = Version.INSTANCE.fromString(versionRangeMatcher.group(1))) == null) {
                return null;
            }
            return new VersionRange(true, true, v, v);
        }
    }
}
