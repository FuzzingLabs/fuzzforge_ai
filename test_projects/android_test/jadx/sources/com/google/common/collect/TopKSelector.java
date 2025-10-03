package com.google.common.collect;

import com.google.common.base.Preconditions;
import com.google.common.math.IntMath;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
final class TopKSelector<T> {
    private final T[] buffer;
    private int bufferSize;
    private final Comparator<? super T> comparator;

    /* renamed from: k */
    private final int f201k;
    private T threshold;

    public static <T extends Comparable<? super T>> TopKSelector<T> least(int k) {
        return least(k, Ordering.natural());
    }

    public static <T> TopKSelector<T> least(int k, Comparator<? super T> comparator) {
        return new TopKSelector<>(comparator, k);
    }

    public static <T extends Comparable<? super T>> TopKSelector<T> greatest(int k) {
        return greatest(k, Ordering.natural());
    }

    public static <T> TopKSelector<T> greatest(int k, Comparator<? super T> comparator) {
        return new TopKSelector<>(Ordering.from(comparator).reverse(), k);
    }

    private TopKSelector(Comparator<? super T> comparator, int i) {
        this.comparator = (Comparator) Preconditions.checkNotNull(comparator, "comparator");
        this.f201k = i;
        Preconditions.checkArgument(i >= 0, "k must be nonnegative, was %s", i);
        this.buffer = (T[]) new Object[i * 2];
        this.bufferSize = 0;
        this.threshold = null;
    }

    public void offer(T t) {
        int i = this.f201k;
        if (i == 0) {
            return;
        }
        int i2 = this.bufferSize;
        if (i2 == 0) {
            this.buffer[0] = t;
            this.threshold = t;
            this.bufferSize = 1;
            return;
        }
        if (i2 < i) {
            T[] tArr = this.buffer;
            this.bufferSize = i2 + 1;
            tArr[i2] = t;
            if (this.comparator.compare(t, this.threshold) > 0) {
                this.threshold = t;
                return;
            }
            return;
        }
        if (this.comparator.compare(t, this.threshold) < 0) {
            T[] tArr2 = this.buffer;
            int i3 = this.bufferSize;
            int i4 = i3 + 1;
            this.bufferSize = i4;
            tArr2[i3] = t;
            if (i4 == this.f201k * 2) {
                trim();
            }
        }
    }

    private void trim() {
        int i = 0;
        int i2 = (this.f201k * 2) - 1;
        int i3 = 0;
        int i4 = 0;
        int log2 = IntMath.log2(i2 - 0, RoundingMode.CEILING) * 3;
        while (true) {
            if (i >= i2) {
                break;
            }
            int partition = partition(i, i2, ((i + i2) + 1) >>> 1);
            int i5 = this.f201k;
            if (partition > i5) {
                i2 = partition - 1;
            } else {
                if (partition >= i5) {
                    break;
                }
                i = Math.max(partition, i + 1);
                i3 = partition;
            }
            i4++;
            if (i4 >= log2) {
                Arrays.sort(this.buffer, i, i2, this.comparator);
                break;
            }
        }
        this.bufferSize = this.f201k;
        this.threshold = this.buffer[i3];
        for (int i6 = i3 + 1; i6 < this.f201k; i6++) {
            if (this.comparator.compare(this.buffer[i6], this.threshold) > 0) {
                this.threshold = this.buffer[i6];
            }
        }
    }

    private int partition(int i, int i2, int i3) {
        T[] tArr = this.buffer;
        T t = tArr[i3];
        tArr[i3] = tArr[i2];
        int i4 = i;
        for (int i5 = i; i5 < i2; i5++) {
            if (this.comparator.compare(this.buffer[i5], t) < 0) {
                swap(i4, i5);
                i4++;
            }
        }
        T[] tArr2 = this.buffer;
        tArr2[i2] = tArr2[i4];
        tArr2[i4] = t;
        return i4;
    }

    private void swap(int i, int j) {
        T[] tArr = this.buffer;
        T tmp = tArr[i];
        tArr[i] = tArr[j];
        tArr[j] = tmp;
    }

    TopKSelector<T> combine(TopKSelector<T> other) {
        for (int i = 0; i < other.bufferSize; i++) {
            offer(other.buffer[i]);
        }
        return this;
    }

    public void offerAll(Iterable<? extends T> elements) {
        offerAll(elements.iterator());
    }

    public void offerAll(Iterator<? extends T> elements) {
        while (elements.hasNext()) {
            offer(elements.next());
        }
    }

    public List<T> topK() {
        Arrays.sort(this.buffer, 0, this.bufferSize, this.comparator);
        int i = this.bufferSize;
        int i2 = this.f201k;
        if (i > i2) {
            T[] tArr = this.buffer;
            Arrays.fill(tArr, i2, tArr.length, (Object) null);
            int i3 = this.f201k;
            this.bufferSize = i3;
            this.threshold = this.buffer[i3 - 1];
        }
        return Collections.unmodifiableList(Arrays.asList(Arrays.copyOf(this.buffer, this.bufferSize)));
    }
}
