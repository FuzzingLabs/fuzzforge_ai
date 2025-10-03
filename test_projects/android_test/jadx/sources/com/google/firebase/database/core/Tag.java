package com.google.firebase.database.core;

/* loaded from: classes.dex */
public final class Tag {
    private final long tagNumber;

    public Tag(long tagNumber) {
        this.tagNumber = tagNumber;
    }

    public long getTagNumber() {
        return this.tagNumber;
    }

    public String toString() {
        return "Tag{tagNumber=" + this.tagNumber + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tag tag = (Tag) o;
        if (this.tagNumber == tag.tagNumber) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        long j = this.tagNumber;
        return (int) (j ^ (j >>> 32));
    }
}
