package com.google.firebase.database.connection.util;

import com.google.firebase.database.logging.LogWrapper;
import com.google.firebase.database.logging.Logger;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public class RetryHelper {
    private long currentRetryDelay;
    private final ScheduledExecutorService executorService;
    private final double jitterFactor;
    private boolean lastWasSuccess;
    private final LogWrapper logger;
    private final long maxRetryDelay;
    private final long minRetryDelayAfterFailure;
    private final Random random;
    private final double retryExponent;
    private ScheduledFuture<?> scheduledRetry;

    private RetryHelper(ScheduledExecutorService executorService, LogWrapper logger, long minRetryDelayAfterFailure, long maxRetryDelay, double retryExponent, double jitterFactor) {
        this.random = new Random();
        this.lastWasSuccess = true;
        this.executorService = executorService;
        this.logger = logger;
        this.minRetryDelayAfterFailure = minRetryDelayAfterFailure;
        this.maxRetryDelay = maxRetryDelay;
        this.retryExponent = retryExponent;
        this.jitterFactor = jitterFactor;
    }

    public void retry(final Runnable runnable) {
        long delay;
        Runnable wrapped = new Runnable() { // from class: com.google.firebase.database.connection.util.RetryHelper.1
            @Override // java.lang.Runnable
            public void run() {
                RetryHelper.this.scheduledRetry = null;
                runnable.run();
            }
        };
        if (this.scheduledRetry != null) {
            this.logger.debug("Cancelling previous scheduled retry", new Object[0]);
            this.scheduledRetry.cancel(false);
            this.scheduledRetry = null;
        }
        if (this.lastWasSuccess) {
            delay = 0;
        } else {
            long delay2 = this.currentRetryDelay;
            if (delay2 == 0) {
                this.currentRetryDelay = this.minRetryDelayAfterFailure;
            } else {
                long newDelay = (long) (delay2 * this.retryExponent);
                this.currentRetryDelay = Math.min(newDelay, this.maxRetryDelay);
            }
            double d = this.jitterFactor;
            long j = this.currentRetryDelay;
            delay = (long) (((1.0d - d) * j) + (d * j * this.random.nextDouble()));
        }
        this.lastWasSuccess = false;
        this.logger.debug("Scheduling retry in %dms", Long.valueOf(delay));
        this.scheduledRetry = this.executorService.schedule(wrapped, delay, TimeUnit.MILLISECONDS);
    }

    public void signalSuccess() {
        this.lastWasSuccess = true;
        this.currentRetryDelay = 0L;
    }

    public void setMaxDelay() {
        this.currentRetryDelay = this.maxRetryDelay;
    }

    public void cancel() {
        if (this.scheduledRetry != null) {
            this.logger.debug("Cancelling existing retry attempt", new Object[0]);
            this.scheduledRetry.cancel(false);
            this.scheduledRetry = null;
        } else {
            this.logger.debug("No existing retry attempt to cancel", new Object[0]);
        }
        this.currentRetryDelay = 0L;
    }

    public static class Builder {
        private final LogWrapper logger;
        private final ScheduledExecutorService service;
        private long minRetryDelayAfterFailure = 1000;
        private double jitterFactor = 0.5d;
        private long retryMaxDelay = 30000;
        private double retryExponent = 1.3d;

        public Builder(ScheduledExecutorService service, Logger logger, String tag) {
            this.service = service;
            this.logger = new LogWrapper(logger, tag);
        }

        public Builder withMinDelayAfterFailure(long delay) {
            this.minRetryDelayAfterFailure = delay;
            return this;
        }

        public Builder withMaxDelay(long delay) {
            this.retryMaxDelay = delay;
            return this;
        }

        public Builder withRetryExponent(double exponent) {
            this.retryExponent = exponent;
            return this;
        }

        public Builder withJitterFactor(double random) {
            if (random < 0.0d || random > 1.0d) {
                throw new IllegalArgumentException("Argument out of range: " + random);
            }
            this.jitterFactor = random;
            return this;
        }

        public RetryHelper build() {
            return new RetryHelper(this.service, this.logger, this.minRetryDelayAfterFailure, this.retryMaxDelay, this.retryExponent, this.jitterFactor);
        }
    }
}
