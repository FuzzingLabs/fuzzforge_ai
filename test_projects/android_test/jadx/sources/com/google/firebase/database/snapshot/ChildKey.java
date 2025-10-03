package com.google.firebase.database.snapshot;

import com.google.firebase.database.core.utilities.Utilities;

/* loaded from: classes.dex */
public class ChildKey implements Comparable<ChildKey> {
    private final String key;
    public static final String MIN_KEY_NAME = "[MIN_NAME]";
    private static final ChildKey MIN_KEY = new ChildKey(MIN_KEY_NAME);
    public static final String MAX_KEY_NAME = "[MAX_KEY]";
    private static final ChildKey MAX_KEY = new ChildKey(MAX_KEY_NAME);
    private static final ChildKey PRIORITY_CHILD_KEY = new ChildKey(".priority");
    private static final ChildKey INFO_CHILD_KEY = new ChildKey(".info");

    public static ChildKey getMinName() {
        return MIN_KEY;
    }

    public static ChildKey getMaxName() {
        return MAX_KEY;
    }

    public static ChildKey getPriorityKey() {
        return PRIORITY_CHILD_KEY;
    }

    public static ChildKey getInfoKey() {
        return INFO_CHILD_KEY;
    }

    private ChildKey(String key) {
        this.key = key;
    }

    public String asString() {
        return this.key;
    }

    public boolean isPriorityChildName() {
        return equals(PRIORITY_CHILD_KEY);
    }

    protected boolean isInt() {
        return false;
    }

    protected int intValue() {
        return 0;
    }

    @Override // java.lang.Comparable
    public int compareTo(ChildKey other) {
        if (this == other) {
            return 0;
        }
        if (this.key.equals(MIN_KEY_NAME) || other.key.equals(MAX_KEY_NAME)) {
            return -1;
        }
        if (other.key.equals(MIN_KEY_NAME) || this.key.equals(MAX_KEY_NAME)) {
            return 1;
        }
        if (isInt()) {
            if (!other.isInt()) {
                return -1;
            }
            int cmp = Utilities.compareInts(intValue(), other.intValue());
            return cmp == 0 ? Utilities.compareInts(this.key.length(), other.key.length()) : cmp;
        }
        if (other.isInt()) {
            return 1;
        }
        return this.key.compareTo(other.key);
    }

    public String toString() {
        return "ChildKey(\"" + this.key + "\")";
    }

    public int hashCode() {
        return this.key.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ChildKey)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        ChildKey other = (ChildKey) obj;
        return this.key.equals(other.key);
    }

    public static ChildKey fromString(String key) {
        Integer intValue = Utilities.tryParseInt(key);
        if (intValue != null) {
            return new IntegerChildKey(key, intValue.intValue());
        }
        if (key.equals(".priority")) {
            return PRIORITY_CHILD_KEY;
        }
        Utilities.hardAssert(!key.contains("/"));
        return new ChildKey(key);
    }

    private static class IntegerChildKey extends ChildKey {
        private final int intValue;

        @Override // com.google.firebase.database.snapshot.ChildKey, java.lang.Comparable
        public /* bridge */ /* synthetic */ int compareTo(ChildKey childKey) {
            return super.compareTo(childKey);
        }

        IntegerChildKey(String name, int intValue) {
            super(name);
            this.intValue = intValue;
        }

        @Override // com.google.firebase.database.snapshot.ChildKey
        protected boolean isInt() {
            return true;
        }

        @Override // com.google.firebase.database.snapshot.ChildKey
        protected int intValue() {
            return this.intValue;
        }

        @Override // com.google.firebase.database.snapshot.ChildKey
        public String toString() {
            return "IntegerChildName(\"" + ((ChildKey) this).key + "\")";
        }
    }
}
