package com.indigo.synapse.messaging.reliability;

import java.time.Duration;

/** 消费幂等状态端口；实现由应用按其本地存储和事务边界提供。 */
public interface MessageIdempotencyStore {
    MessageIdempotencyClaim claim(MessageIdempotencyKey key, Duration lease);
    boolean complete(MessageIdempotencyKey key, String claimId);
    boolean release(MessageIdempotencyKey key, String claimId);
}
