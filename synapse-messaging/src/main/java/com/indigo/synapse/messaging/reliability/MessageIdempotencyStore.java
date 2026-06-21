package com.indigo.synapse.messaging.reliability;

import java.time.Duration;

/**
 * 消费幂等状态端口；实现由应用按本地存储和事务边界提供。
 * claim 必须原子完成状态判断和处理权申请，complete/release 必须校验 claimId。
 */
public interface MessageIdempotencyStore {
    MessageIdempotencyClaim claim(MessageIdempotencyKey key, Duration lease);
    boolean complete(MessageIdempotencyKey key, String claimId);
    boolean release(MessageIdempotencyKey key, String claimId);
}
