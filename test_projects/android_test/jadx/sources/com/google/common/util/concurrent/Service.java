package com.google.common.util.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/* loaded from: classes.dex */
public interface Service {

    public enum State {
        NEW { // from class: com.google.common.util.concurrent.Service.State.1
            @Override // com.google.common.util.concurrent.Service.State
            boolean isTerminal() {
                return false;
            }
        },
        STARTING { // from class: com.google.common.util.concurrent.Service.State.2
            @Override // com.google.common.util.concurrent.Service.State
            boolean isTerminal() {
                return false;
            }
        },
        RUNNING { // from class: com.google.common.util.concurrent.Service.State.3
            @Override // com.google.common.util.concurrent.Service.State
            boolean isTerminal() {
                return false;
            }
        },
        STOPPING { // from class: com.google.common.util.concurrent.Service.State.4
            @Override // com.google.common.util.concurrent.Service.State
            boolean isTerminal() {
                return false;
            }
        },
        TERMINATED { // from class: com.google.common.util.concurrent.Service.State.5
            @Override // com.google.common.util.concurrent.Service.State
            boolean isTerminal() {
                return true;
            }
        },
        FAILED { // from class: com.google.common.util.concurrent.Service.State.6
            @Override // com.google.common.util.concurrent.Service.State
            boolean isTerminal() {
                return true;
            }
        };

        abstract boolean isTerminal();
    }

    void addListener(Listener listener, Executor executor);

    void awaitRunning();

    void awaitRunning(long j, TimeUnit timeUnit) throws TimeoutException;

    void awaitTerminated();

    void awaitTerminated(long j, TimeUnit timeUnit) throws TimeoutException;

    Throwable failureCause();

    boolean isRunning();

    Service startAsync();

    State state();

    Service stopAsync();

    public static abstract class Listener {
        public void starting() {
        }

        public void running() {
        }

        public void stopping(State from) {
        }

        public void terminated(State from) {
        }

        public void failed(State from, Throwable failure) {
        }
    }
}
