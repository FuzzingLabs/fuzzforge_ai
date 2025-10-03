package com.google.firebase.database.core.view;

import com.google.firebase.database.core.Path;
import com.google.firebase.database.snapshot.Index;
import java.util.Map;

/* loaded from: classes.dex */
public final class QuerySpec {
    private final QueryParams params;
    private final Path path;

    public static QuerySpec defaultQueryAtPath(Path path) {
        return new QuerySpec(path, QueryParams.DEFAULT_PARAMS);
    }

    public QuerySpec(Path path, QueryParams params) {
        this.path = path;
        this.params = params;
    }

    public Path getPath() {
        return this.path;
    }

    public QueryParams getParams() {
        return this.params;
    }

    public static QuerySpec fromPathAndQueryObject(Path path, Map<String, Object> map) {
        QueryParams params = QueryParams.fromQueryObject(map);
        return new QuerySpec(path, params);
    }

    public Index getIndex() {
        return this.params.getIndex();
    }

    public boolean isDefault() {
        return this.params.isDefault();
    }

    public boolean loadsAllData() {
        return this.params.loadsAllData();
    }

    public String toString() {
        return this.path + ":" + this.params;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuerySpec that = (QuerySpec) o;
        if (this.path.equals(that.path) && this.params.equals(that.params)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = this.path.hashCode();
        return (result * 31) + this.params.hashCode();
    }
}
