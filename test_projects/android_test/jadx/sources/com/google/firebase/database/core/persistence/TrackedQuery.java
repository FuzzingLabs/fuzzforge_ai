package com.google.firebase.database.core.persistence;

import com.google.firebase.database.core.view.QuerySpec;

/* loaded from: classes.dex */
public final class TrackedQuery {
    public final boolean active;
    public final boolean complete;

    /* renamed from: id */
    public final long f247id;
    public final long lastUse;
    public final QuerySpec querySpec;

    public TrackedQuery(long id, QuerySpec querySpec, long lastUse, boolean complete, boolean active) {
        this.f247id = id;
        if (querySpec.loadsAllData() && !querySpec.isDefault()) {
            throw new IllegalArgumentException("Can't create TrackedQuery for a non-default query that loads all data");
        }
        this.querySpec = querySpec;
        this.lastUse = lastUse;
        this.complete = complete;
        this.active = active;
    }

    public TrackedQuery updateLastUse(long lastUse) {
        return new TrackedQuery(this.f247id, this.querySpec, lastUse, this.complete, this.active);
    }

    public TrackedQuery setComplete() {
        return new TrackedQuery(this.f247id, this.querySpec, this.lastUse, true, this.active);
    }

    public TrackedQuery setActiveState(boolean isActive) {
        return new TrackedQuery(this.f247id, this.querySpec, this.lastUse, this.complete, isActive);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        TrackedQuery query = (TrackedQuery) o;
        if (this.f247id == query.f247id && this.querySpec.equals(query.querySpec) && this.lastUse == query.lastUse && this.complete == query.complete && this.active == query.active) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = Long.valueOf(this.f247id).hashCode();
        return (((((((result * 31) + this.querySpec.hashCode()) * 31) + Long.valueOf(this.lastUse).hashCode()) * 31) + Boolean.valueOf(this.complete).hashCode()) * 31) + Boolean.valueOf(this.active).hashCode();
    }

    public String toString() {
        return "TrackedQuery{id=" + this.f247id + ", querySpec=" + this.querySpec + ", lastUse=" + this.lastUse + ", complete=" + this.complete + ", active=" + this.active + "}";
    }
}
