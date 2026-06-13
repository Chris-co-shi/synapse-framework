package com.indigo.synapse.message.failure;

import com.indigo.synapse.message.core.MessageEnvelope;

import java.time.Instant;

/**
 * 消息处理失败事实。
 */
public record MessageFailure(
        MessageEnvelope message,
        String stage,
        Throwable cause,
        Instant occurredAt
) {

    public MessageFailure {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        if (stage == null || stage.isBlank()) {
            throw new IllegalArgumentException("stage must not be blank");
        }
        if (cause == null) {
            throw new IllegalArgumentException("cause must not be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
    }
}
