package com.google.android.gms.dependencies;

import java.util.List;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

/* compiled from: DataObjects.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\b\u0018\u0000 \u00182\u00020\u0001:\u0001\u0018B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003¢\u0006\u0002\u0010\u0006J\t\u0010\u000b\u001a\u00020\u0003HÆ\u0003J\t\u0010\f\u001a\u00020\u0003HÆ\u0003J\t\u0010\r\u001a\u00020\u0003HÆ\u0003J'\u0010\u000e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\u0006\u0010\u0012\u001a\u00020\u0013J\u0006\u0010\u0014\u001a\u00020\u0003J\t\u0010\u0015\u001a\u00020\u0016HÖ\u0001J\t\u0010\u0017\u001a\u00020\u0003HÖ\u0001R\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\bR\u0011\u0010\u0005\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\b¨\u0006\u0019"}, m238d2 = {"Lcom/google/android/gms/dependencies/ArtifactVersion;", "", "groupId", "", "artifactId", "version", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getArtifactId", "()Ljava/lang/String;", "getGroupId", "getVersion", "component1", "component2", "component3", "copy", "equals", "", "other", "getArtifact", "Lcom/google/android/gms/dependencies/Artifact;", "getGradleRef", "hashCode", "", "toString", "Companion", "strict-version-matcher-plugin"}, m239k = 1, m240mv = {1, 4, 0})
/* loaded from: classes.dex */
public final /* data */ class ArtifactVersion {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private final String artifactId;
    private final String groupId;
    private final String version;

    public static /* synthetic */ ArtifactVersion copy$default(ArtifactVersion artifactVersion, String str, String str2, String str3, int i, Object obj) {
        if ((i & 1) != 0) {
            str = artifactVersion.groupId;
        }
        if ((i & 2) != 0) {
            str2 = artifactVersion.artifactId;
        }
        if ((i & 4) != 0) {
            str3 = artifactVersion.version;
        }
        return artifactVersion.copy(str, str2, str3);
    }

    /* renamed from: component1, reason: from getter */
    public final String getGroupId() {
        return this.groupId;
    }

    /* renamed from: component2, reason: from getter */
    public final String getArtifactId() {
        return this.artifactId;
    }

    /* renamed from: component3, reason: from getter */
    public final String getVersion() {
        return this.version;
    }

    public final ArtifactVersion copy(String groupId, String artifactId, String version) {
        Intrinsics.checkNotNullParameter(groupId, "groupId");
        Intrinsics.checkNotNullParameter(artifactId, "artifactId");
        Intrinsics.checkNotNullParameter(version, "version");
        return new ArtifactVersion(groupId, artifactId, version);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ArtifactVersion)) {
            return false;
        }
        ArtifactVersion artifactVersion = (ArtifactVersion) other;
        return Intrinsics.areEqual(this.groupId, artifactVersion.groupId) && Intrinsics.areEqual(this.artifactId, artifactVersion.artifactId) && Intrinsics.areEqual(this.version, artifactVersion.version);
    }

    public int hashCode() {
        String str = this.groupId;
        int hashCode = (str != null ? str.hashCode() : 0) * 31;
        String str2 = this.artifactId;
        int hashCode2 = (hashCode + (str2 != null ? str2.hashCode() : 0)) * 31;
        String str3 = this.version;
        return hashCode2 + (str3 != null ? str3.hashCode() : 0);
    }

    public String toString() {
        return "ArtifactVersion(groupId=" + this.groupId + ", artifactId=" + this.artifactId + ", version=" + this.version + ")";
    }

    public ArtifactVersion(String groupId, String artifactId, String version) {
        Intrinsics.checkNotNullParameter(groupId, "groupId");
        Intrinsics.checkNotNullParameter(artifactId, "artifactId");
        Intrinsics.checkNotNullParameter(version, "version");
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public final String getArtifactId() {
        return this.artifactId;
    }

    public final String getGroupId() {
        return this.groupId;
    }

    public final String getVersion() {
        return this.version;
    }

    public final Artifact getArtifact() {
        return new Artifact(this.groupId, this.artifactId);
    }

    public final String getGradleRef() {
        return this.groupId + ':' + this.artifactId + ':' + this.version;
    }

    /* compiled from: DataObjects.kt */
    @Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0010\u0010\u0007\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0005\u001a\u00020\u0006¨\u0006\b"}, m238d2 = {"Lcom/google/android/gms/dependencies/ArtifactVersion$Companion;", "", "()V", "fromGradleRef", "Lcom/google/android/gms/dependencies/ArtifactVersion;", "referenceString", "", "fromGradleRefOrNull", "strict-version-matcher-plugin"}, m239k = 1, m240mv = {1, 4, 0})
    /* loaded from: classes.dex */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }

        public final ArtifactVersion fromGradleRef(String referenceString) {
            Intrinsics.checkNotNullParameter(referenceString, "referenceString");
            List stringSplit = StringsKt.split$default((CharSequence) referenceString, new String[]{":"}, false, 0, 6, (Object) null);
            if (stringSplit.size() < 3) {
                throw new IllegalArgumentException("Invalid Gradle reference string: " + referenceString);
            }
            return new ArtifactVersion((String) stringSplit.get(0), (String) stringSplit.get(1), (String) stringSplit.get(2));
        }

        public final ArtifactVersion fromGradleRefOrNull(String referenceString) {
            Intrinsics.checkNotNullParameter(referenceString, "referenceString");
            List stringSplit = StringsKt.split$default((CharSequence) referenceString, new String[]{":"}, false, 0, 6, (Object) null);
            if (stringSplit.size() < 3) {
                return null;
            }
            return new ArtifactVersion((String) stringSplit.get(0), (String) stringSplit.get(1), (String) stringSplit.get(2));
        }
    }
}
