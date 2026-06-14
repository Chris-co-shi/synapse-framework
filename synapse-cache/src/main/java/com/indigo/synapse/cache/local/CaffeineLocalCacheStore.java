package com.indigo.synapse.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 Caffeine 的 L1 本地缓存实现。
 *
 * <p>该实现只作为 Redis L2 的近端加速层，保存已经编码后的字符串值。每条记录使用写入时传入的 TTL，
 * 因此可以保证 L1 过期时间不超过调用方计算后的本地 TTL。</p>
 *
 * <p>本地缓存只在当前 JVM 内生效，不提供跨实例一致性通知。强一致数据不应仅依赖 L1。</p>
 */
public final class CaffeineLocalCacheStore implements LocalCacheStore {

    private final Cache<String, LocalCacheValue> cache;

    public CaffeineLocalCacheStore(long maximumSize) {
        if (maximumSize <= 0) {
            throw new IllegalArgumentException("maximumSize must be positive");
        }
        this.cache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfter(new LocalExpiry())
                .build();
    }

    @Override
    public Optional<String> get(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        LocalCacheValue value = cache.getIfPresent(key);
        return value == null ? Optional.empty() : Optional.of(value.value());
    }

    @Override
    public void put(String key, String value, Duration ttl) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        if (value == null) {
            evict(key);
            return;
        }
        cache.put(key, new LocalCacheValue(value, ttl));
    }

    @Override
    public void evict(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        cache.invalidate(key);
    }

    private record LocalCacheValue(String value, Duration ttl, long writeTimeNanos) {

        private LocalCacheValue(String value, Duration ttl) {
            this(value, requireTtl(ttl), System.nanoTime());
        }

        private static Duration requireTtl(Duration ttl) {
            if (ttl == null || ttl.isZero() || ttl.isNegative()) {
                throw new IllegalArgumentException("ttl must be positive");
            }
            return ttl;
        }
    }

    private static final class LocalExpiry implements com.github.benmanes.caffeine.cache.Expiry<String, LocalCacheValue> {

        @Override
        public long expireAfterCreate(String key, LocalCacheValue value, long currentTime) {
            return value.ttl().toNanos();
        }

        @Override
        public long expireAfterUpdate(String key, LocalCacheValue value, long currentTime, long currentDuration) {
            return value.ttl().toNanos();
        }

        @Override
        public long expireAfterRead(String key, LocalCacheValue value, long currentTime, long currentDuration) {
            long elapsed = currentTime - value.writeTimeNanos();
            long remaining = value.ttl().toNanos() - elapsed;
            return Math.max(1L, remaining);
        }
    }
}
