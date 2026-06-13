package com.indigo.synapse.cache.idempotency;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryIdempotencyGuard implements IdempotencyGuard {

    private final ConcurrentMap<String, Instant> acquiredKeys = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryIdempotencyGuard() {
        this(Clock.systemUTC());
    }

    public InMemoryIdempotencyGuard(Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        this.clock = clock;
    }

    @Override
    public boolean tryAcquire(String scope, String idempotencyKey, Duration ttl) {
        String key = IdempotencyKeyBuilder.build(scope, idempotencyKey);
        validateTtl(ttl);
        cleanupExpired();
        Instant expiresAt = clock.instant().plus(ttl);
        return acquiredKeys.putIfAbsent(key, expiresAt) == null;
    }

    private void cleanupExpired() {
        Instant now = clock.instant();
        Iterator<Map.Entry<String, Instant>> iterator = acquiredKeys.entrySet().iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().getValue().isAfter(now)) {
                iterator.remove();
            }
        }
    }

    private static void validateTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be positive");
        }
    }
}
