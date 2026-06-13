package com.indigo.synapse.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 通用缓存客户端端口。
 *
 * <p>该端口提供两级缓存的统一访问入口，不绑定业务 key 语义。消费方应使用
 * {@link CacheKeyRef} 或统一 key 构造器生成稳定 key，并根据数据一致性要求选择合适 TTL。</p>
 */
public interface CacheClient {

    /**
     * 读取缓存值。
     *
     * @param key 缓存 key 引用，不能为空
     * @param valueType 反序列化目标类型，不能为空
     * @return key 不存在时返回空
     */
    <T> Optional<T> get(CacheKeyRef key, Class<T> valueType);

    /**
     * 写入缓存值。
     *
     * <p>值为 {@code null} 时等价于删除。实现需要保证 L1 本地缓存 TTL 不长于
     * L2 远端缓存 TTL，避免返回远端已过期的旧值。</p>
     */
    <T> void put(CacheKeyRef key, T value, Duration ttl);

    /**
     * 删除缓存值。
     */
    void evict(CacheKeyRef key);

    /**
     * 读取缓存；未命中时通过 loader 加载并写入缓存。
     *
     * <p>该方法只承诺实现自身定义的加载互斥范围。当前默认实现只做单 JVM
     * single-flight，不替代分布式锁。</p>
     */
    <T> T getOrLoad(CacheKeyRef key, Class<T> valueType, Supplier<T> loader, CacheSpec cacheSpec);
}
