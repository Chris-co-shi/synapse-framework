package com.indigo.synapse.messaging.core;

import java.util.Objects;

/**
 * 发布入口的技术结果。
 *
 * <p>{@link Status#SENT} 仅表示 Transport 接受发送；{@link Status#STORED} 仅表示消息已在
 * 当前本地事务中登记到 Outbox。二者都不表示消费者已处理。</p>
 */
public record MessagePublishResult(Status status, String messageId, String transportMessageId, String reason) {
    public MessagePublishResult {
        status = Objects.requireNonNull(status, "status must not be null");
        if (messageId == null || messageId.isBlank()) throw new IllegalArgumentException("messageId must not be blank");
        reason = reason == null ? "" : reason;
    }

    public static MessagePublishResult sent(String messageId, String transportMessageId) {
        return new MessagePublishResult(Status.SENT, messageId, transportMessageId, "");
    }

    public static MessagePublishResult stored(String messageId) {
        return new MessagePublishResult(Status.STORED, messageId, null, "");
    }

    public static MessagePublishResult failed(String messageId, String reason) {
        return new MessagePublishResult(Status.FAILED, messageId, null, reason);
    }

    public boolean isAccepted() {
        return status != Status.FAILED;
    }

    public enum Status {SENT, STORED, FAILED}
}
