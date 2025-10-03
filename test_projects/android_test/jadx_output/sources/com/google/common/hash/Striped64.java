package com.google.common.hash;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Random;
import sun.misc.Unsafe;

/* loaded from: classes.dex */
abstract class Striped64 extends Number {
    private static final Unsafe UNSAFE;
    private static final long baseOffset;
    private static final long busyOffset;
    volatile transient long base;
    volatile transient int busy;
    volatile transient Cell[] cells;
    static final ThreadLocal<int[]> threadHashCode = new ThreadLocal<>();
    static final Random rng = new Random();
    static final int NCPU = Runtime.getRuntime().availableProcessors();

    /* renamed from: fn */
    abstract long mo185fn(long j, long j2);

    static /* synthetic */ Unsafe access$000() {
        return getUnsafe();
    }

    /* loaded from: classes.dex */
    static final class Cell {
        private static final Unsafe UNSAFE;
        private static final long valueOffset;

        /* renamed from: p0 */
        volatile long f223p0;

        /* renamed from: p1 */
        volatile long f224p1;

        /* renamed from: p2 */
        volatile long f225p2;

        /* renamed from: p3 */
        volatile long f226p3;

        /* renamed from: p4 */
        volatile long f227p4;

        /* renamed from: p5 */
        volatile long f228p5;

        /* renamed from: p6 */
        volatile long f229p6;

        /* renamed from: q0 */
        volatile long f230q0;

        /* renamed from: q1 */
        volatile long f231q1;

        /* renamed from: q2 */
        volatile long f232q2;

        /* renamed from: q3 */
        volatile long f233q3;

        /* renamed from: q4 */
        volatile long f234q4;

        /* renamed from: q5 */
        volatile long f235q5;

        /* renamed from: q6 */
        volatile long f236q6;
        volatile long value;

        Cell(long x) {
            this.value = x;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public final boolean cas(long cmp, long val) {
            return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
        }

        static {
            try {
                Unsafe access$000 = Striped64.access$000();
                UNSAFE = access$000;
                valueOffset = access$000.objectFieldOffset(Cell.class.getDeclaredField("value"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    static {
        try {
            Unsafe unsafe = getUnsafe();
            UNSAFE = unsafe;
            baseOffset = unsafe.objectFieldOffset(Striped64.class.getDeclaredField("base"));
            busyOffset = unsafe.objectFieldOffset(Striped64.class.getDeclaredField("busy"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final boolean casBase(long cmp, long val) {
        return UNSAFE.compareAndSwapLong(this, baseOffset, cmp, val);
    }

    final boolean casBusy() {
        return UNSAFE.compareAndSwapInt(this, busyOffset, 0, 1);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Finally extract failed */
    public final void retryUpdate(long x, int[] hc, boolean wasUncontended) {
        int r;
        int[] hc2;
        boolean wasUncontended2;
        int i;
        boolean wasUncontended3;
        int m;
        int i2 = 0;
        if (hc == null) {
            int[] iArr = new int[1];
            hc2 = iArr;
            threadHashCode.set(iArr);
            int r2 = rng.nextInt();
            int i3 = r2 != 0 ? r2 : 1;
            hc2[0] = i3;
            r = i3;
        } else {
            r = hc[0];
            hc2 = hc;
        }
        int h = r;
        boolean collide = false;
        boolean wasUncontended4 = wasUncontended;
        while (true) {
            Cell[] as = this.cells;
            if (as != null) {
                int n = as.length;
                if (n <= 0) {
                    wasUncontended2 = wasUncontended4;
                } else {
                    Cell a = as[(n - 1) & h];
                    if (a == null) {
                        if (this.busy == 0) {
                            Cell r3 = new Cell(x);
                            if (this.busy == 0 && casBusy()) {
                                boolean created = false;
                                try {
                                    Cell[] rs = this.cells;
                                    if (rs != null && (m = rs.length) > 0) {
                                        int j = (m - 1) & h;
                                        if (rs[j] == null) {
                                            rs[j] = r3;
                                            created = true;
                                        }
                                    }
                                    this.busy = i2;
                                    if (created) {
                                        return;
                                    }
                                } catch (Throwable th) {
                                    this.busy = i2;
                                    throw th;
                                }
                            }
                        }
                        collide = false;
                        int h2 = (h << 13) ^ h;
                        int h3 = h2 ^ (h2 >>> 17);
                        int h4 = h3 ^ (h3 << 5);
                        hc2[0] = h4;
                        h = h4;
                        wasUncontended3 = wasUncontended4;
                        i = 0;
                    } else {
                        if (!wasUncontended4) {
                            wasUncontended4 = true;
                        } else {
                            long v = a.value;
                            boolean wasUncontended5 = wasUncontended4;
                            if (!a.cas(v, mo185fn(v, x))) {
                                if (n >= NCPU || this.cells != as) {
                                    collide = false;
                                    wasUncontended4 = wasUncontended5;
                                } else if (!collide) {
                                    collide = true;
                                    wasUncontended4 = wasUncontended5;
                                } else if (this.busy == 0 && casBusy()) {
                                    try {
                                        if (this.cells == as) {
                                            Cell[] rs2 = new Cell[n << 1];
                                            for (int i4 = 0; i4 < n; i4++) {
                                                rs2[i4] = as[i4];
                                            }
                                            this.cells = rs2;
                                        }
                                        i2 = 0;
                                        this.busy = 0;
                                        collide = false;
                                        wasUncontended4 = wasUncontended5;
                                    } finally {
                                    }
                                } else {
                                    wasUncontended4 = wasUncontended5;
                                }
                            } else {
                                return;
                            }
                        }
                        int h22 = (h << 13) ^ h;
                        int h32 = h22 ^ (h22 >>> 17);
                        int h42 = h32 ^ (h32 << 5);
                        hc2[0] = h42;
                        h = h42;
                        wasUncontended3 = wasUncontended4;
                        i = 0;
                    }
                    i2 = i;
                    wasUncontended4 = wasUncontended3;
                }
            } else {
                wasUncontended2 = wasUncontended4;
            }
            if (this.busy != 0 || this.cells != as || !casBusy()) {
                i = 0;
                long v2 = this.base;
                if (casBase(v2, mo185fn(v2, x))) {
                    return;
                }
            } else {
                boolean init = false;
                try {
                    if (this.cells == as) {
                        Cell[] rs3 = new Cell[2];
                        rs3[h & 1] = new Cell(x);
                        this.cells = rs3;
                        init = true;
                    }
                    if (!init) {
                        i = 0;
                    } else {
                        return;
                    }
                } finally {
                }
            }
            wasUncontended3 = wasUncontended2;
            i2 = i;
            wasUncontended4 = wasUncontended3;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void internalReset(long initialValue) {
        Cell[] as = this.cells;
        this.base = initialValue;
        if (as != null) {
            for (Cell a : as) {
                if (a != null) {
                    a.value = initialValue;
                }
            }
        }
    }

    private static Unsafe getUnsafe() {
        try {
            return Unsafe.getUnsafe();
        } catch (SecurityException e) {
            try {
                return (Unsafe) AccessController.doPrivileged(new PrivilegedExceptionAction<Unsafe>() { // from class: com.google.common.hash.Striped64.1
                    @Override // java.security.PrivilegedExceptionAction
                    public Unsafe run() throws Exception {
                        for (Field f : Unsafe.class.getDeclaredFields()) {
                            f.setAccessible(true);
                            Object x = f.get(null);
                            if (Unsafe.class.isInstance(x)) {
                                return (Unsafe) Unsafe.class.cast(x);
                            }
                        }
                        throw new NoSuchFieldError("the Unsafe");
                    }
                });
            } catch (PrivilegedActionException e2) {
                throw new RuntimeException("Could not initialize intrinsics", e2.getCause());
            }
        }
    }
}
