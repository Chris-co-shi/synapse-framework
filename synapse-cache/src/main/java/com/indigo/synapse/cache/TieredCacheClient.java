package com.indigo.synapse.cache;

import com.indigo.synapse.cache.local.LocalCacheStore;
import com.indigo.synapse.cache.redis.RedisCacheStore;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public final class TieredCacheClient implements CacheClient {

    private static final ConcurrentMap<String, Object> LOCAL_LOCKS = new ConcurrentHashMap<>();

    private final LocalCacheStore localCacheStore;
    private final RedisCacheStore redisCacheStore;
    private final CacheValueCodec cacheValueCodec;
    private final CacheSpec defaultCacheSpec;

    public TieredCacheClient(
            LocalCacheStore localCacheStore,
            RedisCacheStore redisCacheStore,
            CacheValueCodec cacheValueCodec,
            CacheSpec defaultCacheSpec
    ) {
        if (redisCacheStore == null) {
            throw new IllegalArgumentException("redisCacheStore must not be null");
        }
        if (cacheValueCodec == null) {
            throw new IllegalArgumentException("cacheValueCodec must not be null");
        }
        if (defaultCacheSpec == null) {
            throw new IllegalArgumentException("defaultCacheSpec must not be null");
        }
        this.localCacheStore = localCacheStore;
        this.redisCacheStore = redisCacheStore;
        this.cacheValueCodec = cacheValueCodec;
        this.defaultCacheSpec = defaultCacheSpec;
    }

    @Override
    public <T> Optional<T> get(CacheKeyRef key, Class<T> valueType) {
        validateKeyAndType(key, valueType);
        String cacheKey = key.value();
        Optional<String> local = localCacheStore == null ? Optional.empty() : localCacheStore.get(cacheKey);
        if (local.isPresent()) {
            return Optional.of(cacheValueCodec.decode(local.get(), valueType));
        }
        Optional<String> remote = redisCacheStore.get(cacheKey);
        remote.ifPresent(value -> {
            if (localCacheStore != null) {
                localCacheStore.put(cacheKey, value, defaultCacheSpec.l1Ttl());
            }
        });
        return remote.map(value -> cacheValueCodec.decode(value, valueType));
    }

    @Override
    public <T> void put(CacheKeyRef key, T value, Duration ttl) {
        validateKey(key);
        validateTtl(ttl);
        String cacheKey = key.value();
        if (value == null) {
            evict(key);
            return;
        }
        String encoded = cacheValueCodec.encode(value);
        redisCacheStore.put(cacheKey, encoded, ttl);
        if (localCacheStore != null) {
            localCacheStore.put(cacheKey, encoded, defaultCacheSpec.l1Ttl());
        }
    }

    @Override
    public void evict(CacheKeyRef key) {
        validateKey(key);
        String cacheKey = key.value();
        redisCacheStore.evict(cacheKey);
        if (localCacheStore != null) {
            localCacheStore.evict(cacheKey);
        }
    }

    @Override
    public <T> T getOrLoad(CacheKeyRef key, Class<T> valueType, Supplier<T> loader, CacheSpec cacheSpec) {
        validateKeyAndType(key, valueType);
        if (loader == null) {
            throw new IllegalArgumentException("loader must not be null");
        }
        CacheSpec effectiveSpec = cacheSpec == null ? defaultCacheSpec : cacheSpec;
        Optional<T> cached = get(key, valueType);
        if (cached.isPresent()) {
            return cached.get();
        }
        Object lock = LOCAL_LOCKS.computeIfAbsent(key.value(), ignored -> new Object());
        synchronized (lock) {
            try {
                cached = get(key, valueType);
                if (cached.isPresent()) {
                    return cached.get();
                }
                T loaded = loader.get();
                if (loaded == null) {
                    evict(key);
                    return null;
                }
                String encoded = cacheValueCodec.encode(loaded);
                redisCacheStore.put(key.value(), encoded, effectiveSpec.l2Ttl());
                if (localCacheStore != null) {
                    localCacheStore.put(key.value(), encoded, effectiveSpec.l1Ttl());
                }
                return loaded;
            } finally {
                LOCAL_LOCKS.remove(key.value(), lock);
            }
        }
    }

    private static void validateKey(CacheKeyRef key) {
        if (key == null || key.value() == null || key.value().isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
    }

    private static <T> void validateKeyAndType(CacheKeyRef key, Class<T> valueType) {
        validateKey(key);
        if (valueType == null) {
            throw new IllegalArgumentException("valueType must not be null");
        }
    }

    private static void validateTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be positive");
        }
    }
}
