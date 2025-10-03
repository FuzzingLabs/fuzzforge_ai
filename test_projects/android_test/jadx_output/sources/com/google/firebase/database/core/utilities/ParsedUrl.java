package com.google.firebase.database.core.utilities;

import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.RepoInfo;

/* loaded from: classes.dex */
public final class ParsedUrl {
    public Path path;
    public RepoInfo repoInfo;

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParsedUrl parsedUrl = (ParsedUrl) o;
        if (!this.repoInfo.equals(parsedUrl.repoInfo)) {
            return false;
        }
        return this.path.equals(parsedUrl.path);
    }

    public int hashCode() {
        int result = this.repoInfo.hashCode();
        return (result * 31) + this.path.hashCode();
    }
}
