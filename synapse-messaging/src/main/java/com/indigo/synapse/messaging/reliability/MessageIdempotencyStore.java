package com.indigo.synapse.messaging.reliability;

/** 消费幂等状态端口；实现由应用按其本地存储和事务边界提供。 */
public interface MessageIdempotencyStore {
    boolean isProcessed(String idempotencyKey);
    void markProcessed(String idempotencyKey);
}
