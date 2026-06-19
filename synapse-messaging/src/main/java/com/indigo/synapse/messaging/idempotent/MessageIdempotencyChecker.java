package com.indigo.synapse.messaging.idempotent;

/**
 * MQ 消费幂等检查契约。
 *
 * <p>{@code synapse-messaging} 只定义幂等检查边界，不负责存储。真实幂等实现应由消费方或后续适配模块提供，
 * 例如使用 Redis、数据库或业务侧去重表。</p>
 */
public interface MessageIdempotencyChecker {

    /**
     * 判断幂等键是否已处理。
     *
     * @param idempotentKey 消费幂等键
     * @return true 表示已处理
     */
    boolean isProcessed(String idempotentKey);

    /**
     * 标记幂等键已处理。
     *
     * @param idempotentKey 消费幂等键
     */
    void markProcessed(String idempotentKey);
}
