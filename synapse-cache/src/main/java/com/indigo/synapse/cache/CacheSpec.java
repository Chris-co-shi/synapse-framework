package com.indigo.synapse.cache;

import java.time.Duration;

public record CacheSpec(
        Duration l1Ttl,
        long l1MaximumSize,
        Duration l2Ttl
) {

    public static final Duration DEFAULT_L1_TTL = Duration.ofMinutes(5);
    public static final long DEFAULT_L1_MAXIMUM_SIZE = 1_000L;
    public static final Duration DEFAULT_L2_TTL = Duration.ofMinutes(30);

    public static CacheSpec defaults() {
        return new CacheSpec(DEFAULT_L1_TTL, DEFAULT_L1_MAXIMUM_SIZE, DEFAULT_L2_TTL);
    }

    public CacheSpec {
        if (l1Ttl == null || l1Ttl.isZero() || l1Ttl.isNegative()) {
            throw new IllegalArgumentException("l1Ttl must be positive");
        }
        if (l1MaximumSize <= 0) {
            throw new IllegalArgumentException("l1MaximumSize must be positive");
        }
        if (l2Ttl == null || l2Ttl.isZero() || l2Ttl.isNegative()) {
            throw new IllegalArgumentException("l2Ttl must be positive");
        }
    }
}
