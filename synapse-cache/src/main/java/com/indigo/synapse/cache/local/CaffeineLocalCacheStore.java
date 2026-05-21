package com.indigo.synapse.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

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
