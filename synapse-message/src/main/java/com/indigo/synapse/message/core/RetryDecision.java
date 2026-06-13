package com.indigo.synapse.message.core;

import java.time.Instant;

/**
 * 重试决策。
 */
public record RetryDecision(boolean retryable, boolean exhausted, Instant nextRetryAt) {

    public static RetryDecision retryAt(Instant nextRetryAt) {
        if (nextRetryAt == null) {
            throw new IllegalArgumentException("nextRetryAt must not be null");
        }
        return new RetryDecision(true, false, nextRetryAt);
    }

    public static RetryDecision exhaustedDecision() {
        return new RetryDecision(false, true, null);
    }

    public static RetryDecision notRetryable() {
        return new RetryDecision(false, false, null);
    }
}
