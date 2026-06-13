package com.indigo.synapse.cache.redis;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 字符串缓存存储端口。
 *
 * <p>该接口只表达框架缓存层需要的最小字符串读写能力，值序列化由上层
 * {@code CacheValueCodec} 负责。消费方自定义实现时应保持 key 校验和 TTL
 * 语义一致，避免本地缓存与远端缓存出现过期时间倒挂。</p>
 */
public interface RedisCacheStore {

    /**
     * 读取指定 key 的字符串值。
     *
     * @param key Redis key，不能为空白
     * @return key 不存在时返回空
     */
    Optional<String> get(String key);

    /**
     * 写入指定 key，并设置正数 TTL；值为 {@code null} 时等价于删除。
     *
     * @param key Redis key，不能为空白
     * @param value 字符串值，可为 {@code null}
     * @param ttl 远端缓存过期时间，必须为正数
     */
    void put(String key, String value, Duration ttl);

    /**
     * 删除指定 key。
     *
     * @param key Redis key，不能为空白
     */
    void evict(String key);

    /**
     * 查询指定 key 的剩余 TTL。
     *
     * <p>默认实现返回空，用于兼容消费方已有实现。返回空表示实现无法确认剩余
     * TTL、key 不存在或 key 未设置过期时间。</p>
     *
     * @param key Redis key，不能为空白
     * @return 可确认的正数剩余 TTL
     */
    default Optional<Duration> ttl(String key) {
        return Optional.empty();
    }
}
