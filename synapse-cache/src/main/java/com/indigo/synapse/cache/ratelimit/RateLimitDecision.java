package com.indigo.synapse.cache.ratelimit;

public record RateLimitDecision(boolean allowed, long limit, long remaining, long resetAtMillis) {

    public RateLimitDecision {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        if (remaining < 0) {
            throw new IllegalArgumentException("remaining must not be negative");
        }
        if (resetAtMillis < 0) {
            throw new IllegalArgumentException("resetAtMillis must not be negative");
        }
    }
}
