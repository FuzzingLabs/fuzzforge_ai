package com.google.android.gms.dependencies;

import java.util.List;
import kotlin.Metadata;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

/* compiled from: DataObjects.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\b\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003¢\u0006\u0002\u0010\u0005J\t\u0010\t\u001a\u00020\u0003HÆ\u0003J\t\u0010\n\u001a\u00020\u0003HÆ\u0003J\u001d\u0010\u000b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\u0006\u0010\u000f\u001a\u00020\u0003J\t\u0010\u0010\u001a\u00020\u0011HÖ\u0001J\t\u0010\u0012\u001a\u00020\u0003HÖ\u0001R\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0007¨\u0006\u0014"}, m238d2 = {"Lcom/google/android/gms/dependencies/Artifact;", "", "groupId", "", "artifactId", "(Ljava/lang/String;Ljava/lang/String;)V", "getArtifactId", "()Ljava/lang/String;", "getGroupId", "component1", "component2", "copy", "equals", "", "other", "getGradleRef", "hashCode", "", "toString", "Companion", "strict-version-matcher-plugin"}, m239k = 1, m240mv = {1, 4, 0})
/* loaded from: classes.dex */
public final /* data */ class Artifact {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private final String artifactId;
    private final String groupId;

    public static /* synthetic */ Artifact copy$default(Artifact artifact, String str, String str2, int i, Object obj) {
        if ((i & 1) != 0) {
            str = artifact.groupId;
        }
        if ((i & 2) != 0) {
            str2 = artifact.artifactId;
        }
        return artifact.copy(str, str2);
    }

    /* renamed from: component1, reason: from getter */
    public final String getGroupId() {
        return this.groupId;
    }

    /* renamed from: component2, reason: from getter */
    public final String getArtifactId() {
        return this.artifactId;
    }

    public final Artifact copy(String groupId, String artifactId) {
        Intrinsics.checkNotNullParameter(groupId, "groupId");
        Intrinsics.checkNotNullParameter(artifactId, "artifactId");
        return new Artifact(groupId, artifactId);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Artifact)) {
            return false;
        }
        Artifact artifact = (Artifact) other;
        return Intrinsics.areEqual(this.groupId, artifact.groupId) && Intrinsics.areEqual(this.artifactId, artifact.artifactId);
    }

    public int hashCode() {
        String str = this.groupId;
        int hashCode = (str != null ? str.hashCode() : 0) * 31;
        String str2 = this.artifactId;
        return hashCode + (str2 != null ? str2.hashCode() : 0);
    }

    public String toString() {
        return "Artifact(groupId=" + this.groupId + ", artifactId=" + this.artifactId + ")";
    }

    public Artifact(String groupId, String artifactId) {
        Intrinsics.checkNotNullParameter(groupId, "groupId");
        Intrinsics.checkNotNullParameter(artifactId, "artifactId");
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    public final String getArtifactId() {
        return this.artifactId;
    }

    public final String getGroupId() {
        return this.groupId;
    }

    public final String getGradleRef() {
        return this.groupId + ':' + this.artifactId;
    }

    /* compiled from: DataObjects.kt */
    @Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006¨\u0006\u0007"}, m238d2 = {"Lcom/google/android/gms/dependencies/Artifact$Companion;", "", "()V", "fromGradleRef", "Lcom/google/android/gms/dependencies/Artifact;", "referenceString", "", "strict-version-matcher-plugin"}, m239k = 1, m240mv = {1, 4, 0})
    /* loaded from: classes.dex */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }

        public final Artifact fromGradleRef(String referenceString) {
            Intrinsics.checkNotNullParameter(referenceString, "referenceString");
            List stringSplit = StringsKt.split$default((CharSequence) referenceString, new String[]{":"}, false, 0, 6, (Object) null);
            if (stringSplit.size() < 2) {
                throw new IllegalArgumentException("Invalid Gradle reference string: " + referenceString);
            }
            return new Artifact((String) stringSplit.get(0), (String) stringSplit.get(1));
        }
    }
}
