package com.google.common.collect;

import com.android.tools.r8.annotations.SynthesizedClass;
import java.lang.Comparable;
import java.util.Iterator;
import java.util.Set;

/* loaded from: classes.dex */
public interface RangeSet<C extends Comparable> {
    void add(Range<C> range);

    void addAll(RangeSet<C> rangeSet);

    void addAll(Iterable<Range<C>> iterable);

    Set<Range<C>> asDescendingSetOfRanges();

    Set<Range<C>> asRanges();

    void clear();

    RangeSet<C> complement();

    boolean contains(C c);

    boolean encloses(Range<C> range);

    boolean enclosesAll(RangeSet<C> rangeSet);

    boolean enclosesAll(Iterable<Range<C>> iterable);

    boolean equals(Object obj);

    int hashCode();

    boolean intersects(Range<C> range);

    boolean isEmpty();

    Range<C> rangeContaining(C c);

    void remove(Range<C> range);

    void removeAll(RangeSet<C> rangeSet);

    void removeAll(Iterable<Range<C>> iterable);

    Range<C> span();

    RangeSet<C> subRangeSet(Range<C> range);

    String toString();

    @SynthesizedClass(kind = "$-CC")
    /* renamed from: com.google.common.collect.RangeSet$-CC, reason: invalid class name */
    public final /* synthetic */ class CC<C extends Comparable> {
        public static boolean $default$enclosesAll(RangeSet _this, Iterable iterable) {
            Iterator it = iterable.iterator();
            while (it.hasNext()) {
                Range<C> range = (Range) it.next();
                if (!_this.encloses(range)) {
                    return false;
                }
            }
            return true;
        }

        public static void $default$addAll(RangeSet _this, Iterable iterable) {
            Iterator it = iterable.iterator();
            while (it.hasNext()) {
                Range<C> range = (Range) it.next();
                _this.add(range);
            }
        }

        public static void $default$removeAll(RangeSet _this, Iterable iterable) {
            Iterator it = iterable.iterator();
            while (it.hasNext()) {
                Range<C> range = (Range) it.next();
                _this.remove(range);
            }
        }
    }
}
