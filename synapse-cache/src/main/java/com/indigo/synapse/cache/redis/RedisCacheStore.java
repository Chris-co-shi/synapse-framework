package com.indigo.synapse.cache.redis;

import java.time.Duration;
import java.util.Optional;

public interface RedisCacheStore {

    Optional<String> get(String key);

    void put(String key, String value, Duration ttl);

    void evict(String key);
}
