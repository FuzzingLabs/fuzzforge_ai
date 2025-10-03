package com.airbnb.lottie;

import android.os.Handler;
import android.os.Looper;
import com.airbnb.lottie.utils.Logger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/* loaded from: classes.dex */
public class LottieTask<T> {
    public static Executor EXECUTOR = Executors.newCachedThreadPool();
    private final Set<LottieListener<Throwable>> failureListeners;
    private final Handler handler;
    private volatile LottieResult<T> result;
    private final Set<LottieListener<T>> successListeners;

    public LottieTask(Callable<LottieResult<T>> runnable) {
        this(runnable, false);
    }

    LottieTask(Callable<LottieResult<T>> runnable, boolean runNow) {
        this.successListeners = new LinkedHashSet(1);
        this.failureListeners = new LinkedHashSet(1);
        this.handler = new Handler(Looper.getMainLooper());
        this.result = null;
        if (runNow) {
            try {
                setResult(runnable.call());
                return;
            } catch (Throwable e) {
                setResult(new LottieResult<>(e));
                return;
            }
        }
        EXECUTOR.execute(new LottieFutureTask(runnable));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setResult(LottieResult<T> result) {
        if (this.result != null) {
            throw new IllegalStateException("A task may only be set once.");
        }
        this.result = result;
        notifyListeners();
    }

    public synchronized LottieTask<T> addListener(LottieListener<T> listener) {
        if (this.result != null && this.result.getValue() != null) {
            listener.onResult(this.result.getValue());
        }
        this.successListeners.add(listener);
        return this;
    }

    public synchronized LottieTask<T> removeListener(LottieListener<T> listener) {
        this.successListeners.remove(listener);
        return this;
    }

    public synchronized LottieTask<T> addFailureListener(LottieListener<Throwable> listener) {
        if (this.result != null && this.result.getException() != null) {
            listener.onResult(this.result.getException());
        }
        this.failureListeners.add(listener);
        return this;
    }

    public synchronized LottieTask<T> removeFailureListener(LottieListener<Throwable> listener) {
        this.failureListeners.remove(listener);
        return this;
    }

    private void notifyListeners() {
        this.handler.post(new Runnable() { // from class: com.airbnb.lottie.LottieTask.1
            @Override // java.lang.Runnable
            public void run() {
                if (LottieTask.this.result != null) {
                    LottieResult<T> result = LottieTask.this.result;
                    if (result.getValue() != null) {
                        LottieTask.this.notifySuccessListeners(result.getValue());
                    } else {
                        LottieTask.this.notifyFailureListeners(result.getException());
                    }
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void notifySuccessListeners(T value) {
        List<LottieListener<T>> listenersCopy = new ArrayList<>(this.successListeners);
        for (LottieListener<T> l : listenersCopy) {
            l.onResult(value);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void notifyFailureListeners(Throwable e) {
        List<LottieListener<Throwable>> listenersCopy = new ArrayList<>(this.failureListeners);
        if (listenersCopy.isEmpty()) {
            Logger.warning("Lottie encountered an error but no failure listener was added:", e);
            return;
        }
        for (LottieListener<Throwable> l : listenersCopy) {
            l.onResult(e);
        }
    }

    /* loaded from: classes.dex */
    private class LottieFutureTask extends FutureTask<LottieResult<T>> {
        LottieFutureTask(Callable<LottieResult<T>> callable) {
            super(callable);
        }

        @Override // java.util.concurrent.FutureTask
        protected void done() {
            if (!isCancelled()) {
                try {
                    LottieTask.this.setResult(get());
                } catch (InterruptedException | ExecutionException e) {
                    LottieTask.this.setResult(new LottieResult((Throwable) e));
                }
            }
        }
    }
}
