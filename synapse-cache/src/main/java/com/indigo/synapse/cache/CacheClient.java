package com.indigo.synapse.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

public interface CacheClient {

    <T> Optional<T> get(CacheKeyRef key, Class<T> valueType);

    <T> void put(CacheKeyRef key, T value, Duration ttl);

    void evict(CacheKeyRef key);

    <T> T getOrLoad(CacheKeyRef key, Class<T> valueType, Supplier<T> loader, CacheSpec cacheSpec);
}
