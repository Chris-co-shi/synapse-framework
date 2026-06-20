package com.indigo.synapse.messaging.reliability;

import java.time.Instant;

/** 不含异常堆栈和敏感负载副本的消费失败描述。 */
public record MessageFailure(String messageId, String eventId, String messageType, int attempt,
                             String failureType, String reason, Instant failedAt) {
    public MessageFailure {
        if (messageId == null || messageId.isBlank()) throw new IllegalArgumentException("messageId must not be blank");
        if (messageType == null || messageType.isBlank()) throw new IllegalArgumentException("messageType must not be blank");
        if (attempt < 1) throw new IllegalArgumentException("attempt must be positive");
        failureType = failureType == null ? "unknown" : failureType;
        reason = reason == null ? "" : reason;
        failedAt = failedAt == null ? Instant.now() : failedAt;
    }
}
