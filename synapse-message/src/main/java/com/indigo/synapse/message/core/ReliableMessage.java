package com.indigo.synapse.message.core;

import java.time.Instant;

/**
 * 可靠消息领域模型。
 *
 * <p>该模型不依赖 MyBatis-Plus 注解，持久化字段由 adapter 转换。</p>
 */
public record ReliableMessage(
        String messageId,
        MessageEnvelope envelope,
        ReliableMessageStatus status,
        int attempt,
        Instant nextRetryAt,
        String lockedBy,
        Instant lockedUntil,
        String lastError,
        String idempotencyKey,
        Instant createdAt,
        Instant updatedAt,
        int version
) {

    public ReliableMessage {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId must not be blank");
        }
        if (envelope == null) {
            throw new IllegalArgumentException("envelope must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (attempt < 0) {
            throw new IllegalArgumentException("attempt must not be negative");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt must not be null");
        }
    }

    public static ReliableMessage pending(MessageEnvelope envelope, String idempotencyKey, Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        return new ReliableMessage(
                envelope.messageId(),
                envelope,
                ReliableMessageStatus.PENDING,
                0,
                now,
                null,
                null,
                null,
                idempotencyKey,
                now,
                now,
                0
        );
    }
}
