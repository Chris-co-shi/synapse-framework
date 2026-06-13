package com.indigo.synapse.message.core;

import java.time.Duration;
import java.time.Instant;

/**
 * 指数退避重试策略。
 */
public final class ExponentialBackoffRetryPolicy implements RetryPolicy {

    private final int maxAttempts;
    private final Duration initialInterval;
    private final double multiplier;
    private final Duration maxInterval;

    public ExponentialBackoffRetryPolicy(int maxAttempts, Duration initialInterval, double multiplier, Duration maxInterval) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        validateDuration(initialInterval, "initialInterval");
        if (multiplier < 1.0d) {
            throw new IllegalArgumentException("multiplier must be greater than or equal to 1");
        }
        validateDuration(maxInterval, "maxInterval");
        this.maxAttempts = maxAttempts;
        this.initialInterval = initialInterval;
        this.multiplier = multiplier;
        this.maxInterval = maxInterval;
    }

    @Override
    public RetryDecision decide(ReliableMessage message, Throwable failure, Instant now) {
        if (message == null || failure == null || now == null) {
            throw new IllegalArgumentException("retry context must not be null");
        }
        int nextAttempt = message.attempt() + 1;
        if (nextAttempt >= maxAttempts) {
            return RetryDecision.exhaustedDecision();
        }
        double factor = Math.pow(multiplier, Math.max(0, nextAttempt - 1));
        long delayMillis = Math.min(maxInterval.toMillis(), Math.round(initialInterval.toMillis() * factor));
        return RetryDecision.retryAt(now.plusMillis(delayMillis));
    }

    private static void validateDuration(Duration duration, String name) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
