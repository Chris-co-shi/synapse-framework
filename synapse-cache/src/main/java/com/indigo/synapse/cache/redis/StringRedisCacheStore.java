package com.indigo.synapse.cache.redis;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;

public final class StringRedisCacheStore implements RedisCacheStore {

    private final StringRedisTemplate redisTemplate;

    public StringRedisCacheStore(StringRedisTemplate redisTemplate) {
        if (redisTemplate == null) {
            throw new IllegalArgumentException("redisTemplate must not be null");
        }
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<String> get(String key) {
        validateKey(key);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public void put(String key, String value, Duration ttl) {
        validateKey(key);
        validateTtl(ttl);
        if (value == null) {
            evict(key);
            return;
        }
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public void evict(String key) {
        validateKey(key);
        redisTemplate.delete(key);
    }

    private static void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
    }

    private static void validateTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be positive");
        }
    }
}
