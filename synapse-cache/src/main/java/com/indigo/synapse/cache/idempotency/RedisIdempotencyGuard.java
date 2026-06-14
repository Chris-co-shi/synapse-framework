package com.indigo.synapse.cache.idempotency;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * 基于 Redis SET NX 的幂等 Guard。
 *
 * <p>该实现通过 setIfAbsent 写入短期标记，保证同一 scope + idempotencyKey 在 TTL 内只会首次成功。
 * 它不负责保存业务执行结果，也不处理失败补偿；消费方应根据业务语义决定失败后是否复用同一幂等 key。</p>
 */
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
