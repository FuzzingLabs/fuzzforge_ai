package com.google.android.gms.dependencies;

import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* compiled from: DataObjects.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0005\b\u0086\b\u0018\u0000  2\u00020\u0001:\u0001 B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007¢\u0006\u0002\u0010\bJ\t\u0010\u0013\u001a\u00020\u0003HÆ\u0003J\t\u0010\u0014\u001a\u00020\u0005HÆ\u0003J\t\u0010\u0015\u001a\u00020\u0007HÆ\u0003J'\u0010\u0016\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007HÆ\u0001J\u0013\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\u0006\u0010\u001a\u001a\u00020\u0007J\t\u0010\u001b\u001a\u00020\u001cHÖ\u0001J\u000e\u0010\u001d\u001a\u00020\u00182\u0006\u0010\u001e\u001a\u00020\u0007J\t\u0010\u001f\u001a\u00020\u0007HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004¢\u0006\u0002\n\u0000R\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0006\u001a\u00020\u0007¢\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006!"}, m238d2 = {"Lcom/google/android/gms/dependencies/Dependency;", "", "fromArtifactVersion", "Lcom/google/android/gms/dependencies/ArtifactVersion;", "toArtifact", "Lcom/google/android/gms/dependencies/Artifact;", "toArtifactVersionString", "", "(Lcom/google/android/gms/dependencies/ArtifactVersion;Lcom/google/android/gms/dependencies/Artifact;Ljava/lang/String;)V", "getFromArtifactVersion", "()Lcom/google/android/gms/dependencies/ArtifactVersion;", "logger", "Lorg/slf4j/Logger;", "getToArtifact", "()Lcom/google/android/gms/dependencies/Artifact;", "getToArtifactVersionString", "()Ljava/lang/String;", "versionEvaluator", "Lcom/google/android/gms/dependencies/VersionEvaluator;", "component1", "component2", "component3", "copy", "equals", "", "other", "getDisplayString", "hashCode", "", "isVersionCompatible", "versionString", "toString", "Companion", "strict-version-matcher-plugin"}, m239k = 1, m240mv = {1, 4, 0})
/* loaded from: classes.dex */
public final /* data */ class Dependency {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private final ArtifactVersion fromArtifactVersion;
    private final Logger logger;
    private final Artifact toArtifact;
    private final String toArtifactVersionString;
    private final VersionEvaluator versionEvaluator;

    public static /* synthetic */ Dependency copy$default(Dependency dependency, ArtifactVersion artifactVersion, Artifact artifact, String str, int i, Object obj) {
        if ((i & 1) != 0) {
            artifactVersion = dependency.fromArtifactVersion;
        }
        if ((i & 2) != 0) {
            artifact = dependency.toArtifact;
        }
        if ((i & 4) != 0) {
            str = dependency.toArtifactVersionString;
        }
        return dependency.copy(artifactVersion, artifact, str);
    }

    /* renamed from: component1, reason: from getter */
    public final ArtifactVersion getFromArtifactVersion() {
        return this.fromArtifactVersion;
    }

    /* renamed from: component2, reason: from getter */
    public final Artifact getToArtifact() {
        return this.toArtifact;
    }

    /* renamed from: component3, reason: from getter */
    public final String getToArtifactVersionString() {
        return this.toArtifactVersionString;
    }

    public final Dependency copy(ArtifactVersion fromArtifactVersion, Artifact toArtifact, String toArtifactVersionString) {
        Intrinsics.checkNotNullParameter(fromArtifactVersion, "fromArtifactVersion");
        Intrinsics.checkNotNullParameter(toArtifact, "toArtifact");
        Intrinsics.checkNotNullParameter(toArtifactVersionString, "toArtifactVersionString");
        return new Dependency(fromArtifactVersion, toArtifact, toArtifactVersionString);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Dependency)) {
            return false;
        }
        Dependency dependency = (Dependency) other;
        return Intrinsics.areEqual(this.fromArtifactVersion, dependency.fromArtifactVersion) && Intrinsics.areEqual(this.toArtifact, dependency.toArtifact) && Intrinsics.areEqual(this.toArtifactVersionString, dependency.toArtifactVersionString);
    }

    public int hashCode() {
        ArtifactVersion artifactVersion = this.fromArtifactVersion;
        int hashCode = (artifactVersion != null ? artifactVersion.hashCode() : 0) * 31;
        Artifact artifact = this.toArtifact;
        int hashCode2 = (hashCode + (artifact != null ? artifact.hashCode() : 0)) * 31;
        String str = this.toArtifactVersionString;
        return hashCode2 + (str != null ? str.hashCode() : 0);
    }

    public String toString() {
        return "Dependency(fromArtifactVersion=" + this.fromArtifactVersion + ", toArtifact=" + this.toArtifact + ", toArtifactVersionString=" + this.toArtifactVersionString + ")";
    }

    public Dependency(ArtifactVersion fromArtifactVersion, Artifact toArtifact, String toArtifactVersionString) {
        Intrinsics.checkNotNullParameter(fromArtifactVersion, "fromArtifactVersion");
        Intrinsics.checkNotNullParameter(toArtifact, "toArtifact");
        Intrinsics.checkNotNullParameter(toArtifactVersionString, "toArtifactVersionString");
        this.fromArtifactVersion = fromArtifactVersion;
        this.toArtifact = toArtifact;
        this.toArtifactVersionString = toArtifactVersionString;
        Logger logger = LoggerFactory.getLogger(Dependency.class);
        Intrinsics.checkNotNullExpressionValue(logger, "LoggerFactory.getLogger(Dependency::class.java)");
        this.logger = logger;
        boolean enableStrictMatching = toArtifact.getGroupId().equals("com.google.android.gms") || toArtifact.getGroupId().equals("com.google.firebase");
        this.versionEvaluator = VersionEvaluators.INSTANCE.getEvaluator(toArtifactVersionString, enableStrictMatching);
    }

    public final ArtifactVersion getFromArtifactVersion() {
        return this.fromArtifactVersion;
    }

    public final Artifact getToArtifact() {
        return this.toArtifact;
    }

    public final String getToArtifactVersionString() {
        return this.toArtifactVersionString;
    }

    public final boolean isVersionCompatible(String versionString) {
        Intrinsics.checkNotNullParameter(versionString, "versionString");
        if (this.versionEvaluator.isCompatible(versionString)) {
            return true;
        }
        this.logger.debug("Failed comparing " + this.toArtifactVersionString + " with " + versionString + " using " + this.versionEvaluator.getClass());
        return false;
    }

    public final String getDisplayString() {
        return this.fromArtifactVersion.getGradleRef() + " -> " + this.toArtifact.getGradleRef() + "@" + this.toArtifactVersionString;
    }

    /* compiled from: DataObjects.kt */
    @Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0016\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0006¨\u0006\b"}, m238d2 = {"Lcom/google/android/gms/dependencies/Dependency$Companion;", "", "()V", "fromArtifactVersions", "Lcom/google/android/gms/dependencies/Dependency;", "fromArtifactVersion", "Lcom/google/android/gms/dependencies/ArtifactVersion;", "toArtifactVersion", "strict-version-matcher-plugin"}, m239k = 1, m240mv = {1, 4, 0})
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }

        public final Dependency fromArtifactVersions(ArtifactVersion fromArtifactVersion, ArtifactVersion toArtifactVersion) {
            Intrinsics.checkNotNullParameter(fromArtifactVersion, "fromArtifactVersion");
            Intrinsics.checkNotNullParameter(toArtifactVersion, "toArtifactVersion");
            return new Dependency(fromArtifactVersion, toArtifactVersion.getArtifact(), toArtifactVersion.getVersion());
        }
    }
}
