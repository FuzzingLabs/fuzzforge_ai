package com.google.common.util.concurrent;

import com.google.common.base.Preconditions;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

/* loaded from: classes.dex */
public final class ExecutionSequencer {
    private final AtomicReference<ListenableFuture<Object>> ref = new AtomicReference<>(Futures.immediateFuture(null));

    enum RunningState {
        NOT_RUN,
        CANCELLED,
        STARTED
    }

    private ExecutionSequencer() {
    }

    public static ExecutionSequencer create() {
        return new ExecutionSequencer();
    }

    public <T> ListenableFuture<T> submit(final Callable<T> callable, Executor executor) {
        Preconditions.checkNotNull(callable);
        return submitAsync(new AsyncCallable<T>() { // from class: com.google.common.util.concurrent.ExecutionSequencer.1
            @Override // com.google.common.util.concurrent.AsyncCallable
            public ListenableFuture<T> call() throws Exception {
                return Futures.immediateFuture(callable.call());
            }

            public String toString() {
                return callable.toString();
            }
        }, executor);
    }

    public <T> ListenableFuture<T> submitAsync(final AsyncCallable<T> callable, final Executor executor) {
        Preconditions.checkNotNull(callable);
        final AtomicReference<RunningState> runningState = new AtomicReference<>(RunningState.NOT_RUN);
        AsyncCallable<T> task = new AsyncCallable<T>() { // from class: com.google.common.util.concurrent.ExecutionSequencer.2
            @Override // com.google.common.util.concurrent.AsyncCallable
            public ListenableFuture<T> call() throws Exception {
                if (!runningState.compareAndSet(RunningState.NOT_RUN, RunningState.STARTED)) {
                    return Futures.immediateCancelledFuture();
                }
                return callable.call();
            }

            public String toString() {
                return callable.toString();
            }
        };
        final SettableFuture<Object> newFuture = SettableFuture.create();
        final ListenableFuture<?> oldFuture = this.ref.getAndSet(newFuture);
        final ListenableFuture<T> taskFuture = Futures.submitAsync(task, new Executor() { // from class: com.google.common.util.concurrent.ExecutionSequencer.3
            @Override // java.util.concurrent.Executor
            public void execute(Runnable runnable) {
                oldFuture.addListener(runnable, executor);
            }
        });
        final ListenableFuture<T> outputFuture = Futures.nonCancellationPropagating(taskFuture);
        Runnable listener = new Runnable() { // from class: com.google.common.util.concurrent.ExecutionSequencer.4
            @Override // java.lang.Runnable
            public void run() {
                if (taskFuture.isDone() || (outputFuture.isCancelled() && runningState.compareAndSet(RunningState.NOT_RUN, RunningState.CANCELLED))) {
                    newFuture.setFuture(oldFuture);
                }
            }
        };
        outputFuture.addListener(listener, MoreExecutors.directExecutor());
        taskFuture.addListener(listener, MoreExecutors.directExecutor());
        return outputFuture;
    }
}
