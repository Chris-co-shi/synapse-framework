package com.indigo.synapse.cache.idempotency;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

public final class RedisIdempotencyGuard implements IdempotencyGuard {

    private static final String MARKER = "1";

    private final StringRedisTemplate redisTemplate;

    public RedisIdempotencyGuard(StringRedisTemplate redisTemplate) {
        if (redisTemplate == null) {
            throw new IllegalArgumentException("redisTemplate must not be null");
        }
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryAcquire(String scope, String idempotencyKey, Duration ttl) {
        validateTtl(ttl);
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(IdempotencyKeyBuilder.build(scope, idempotencyKey), MARKER, ttl);
        return Boolean.TRUE.equals(acquired);
    }

    private static void validateTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be positive");
        }
    }
}
