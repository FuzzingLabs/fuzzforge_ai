package com.google.common.util.concurrent;

import java.util.concurrent.atomic.AtomicReference;

/* loaded from: classes.dex */
abstract class InterruptibleTask<T> extends AtomicReference<Runnable> implements Runnable {
    private static final Runnable DONE;
    private static final Runnable INTERRUPTING;

    abstract void afterRanInterruptibly(T t, Throwable th);

    abstract boolean isDone();

    abstract T runInterruptibly() throws Exception;

    abstract String toPendingString();

    /* loaded from: classes.dex */
    private static final class DoNothingRunnable implements Runnable {
        private DoNothingRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
        }
    }

    static {
        DONE = new DoNothingRunnable();
        INTERRUPTING = new DoNothingRunnable();
    }

    @Override // java.lang.Runnable
    public final void run() {
        Thread currentThread = Thread.currentThread();
        if (!compareAndSet(null, currentThread)) {
            return;
        }
        boolean run = !isDone();
        T result = null;
        Throwable error = null;
        if (run) {
            try {
                result = runInterruptibly();
            } catch (Throwable t) {
                error = t;
                if (!compareAndSet(currentThread, DONE)) {
                    while (get() == INTERRUPTING) {
                        Thread.yield();
                    }
                }
                if (!run) {
                    return;
                }
            }
        }
        if (!compareAndSet(currentThread, DONE)) {
            while (get() == INTERRUPTING) {
                Thread.yield();
            }
        }
        if (!run) {
            return;
        }
        afterRanInterruptibly(result, error);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void interruptTask() {
        Runnable currentRunner = get();
        if ((currentRunner instanceof Thread) && compareAndSet(currentRunner, INTERRUPTING)) {
            ((Thread) currentRunner).interrupt();
            set(DONE);
        }
    }

    @Override // java.util.concurrent.atomic.AtomicReference
    public final String toString() {
        String result;
        Runnable state = get();
        if (state == DONE) {
            result = "running=[DONE]";
        } else if (state == INTERRUPTING) {
            result = "running=[INTERRUPTED]";
        } else if (state instanceof Thread) {
            result = "running=[RUNNING ON " + ((Thread) state).getName() + "]";
        } else {
            result = "running=[NOT STARTED YET]";
        }
        return result + ", " + toPendingString();
    }
}
