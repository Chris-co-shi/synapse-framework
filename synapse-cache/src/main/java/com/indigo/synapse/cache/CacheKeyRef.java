package com.indigo.synapse.cache;

/**
 * 缓存 key 引用。
 *
 * <p>框架只要求 key 能稳定转换为字符串，不理解 key 中的业务语义。推荐消费方
 * 使用统一命名空间和分段规则，避免跨模块 key 冲突。</p>
 */
public interface CacheKeyRef {

    /**
     * 返回最终写入 Redis 或本地缓存的 key。
     */
    String value();
}
