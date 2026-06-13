package com.indigo.synapse.cache.local;

import java.time.Duration;
import java.util.Optional;

/**
 * L1 本地缓存存储端口。
 *
 * <p>本地缓存只作为 Redis L2 的近端加速层。实现必须遵守传入 TTL，不应让本地
 * 数据比远端数据保留更久。</p>
 */
public interface LocalCacheStore {

    /**
     * 读取本地缓存字符串值。
     */
    Optional<String> get(String key);

    /**
     * 写入本地缓存字符串值；值为 {@code null} 时等价于删除。
     */
    void put(String key, String value, Duration ttl);

    /**
     * 删除本地缓存。
     */
    void evict(String key);
}
