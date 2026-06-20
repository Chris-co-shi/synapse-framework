package com.indigo.synapse.messaging.core;

import java.util.Objects;

/**
 * 消费处理结论；Transport 适配器据此决定 ACK、重试或停止重试。
 */
public record MessageHandleResult(Status status, String reason) {
    public MessageHandleResult {
        status = Objects.requireNonNull(status, "status must not be null");
        reason = reason == null ? "" : reason;
    }

    public static MessageHandleResult success() {
        return new MessageHandleResult(Status.SUCCESS, "");
    }

    public static MessageHandleResult duplicate() {
        return new MessageHandleResult(Status.DUPLICATE, "already processed");
    }

    public static MessageHandleResult retry(String reason) {
        return new MessageHandleResult(Status.RETRY, reason);
    }

    public static MessageHandleResult discard(String reason) {
        return new MessageHandleResult(Status.DISCARD, reason);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS || status == Status.DUPLICATE;
    }

    public enum Status {SUCCESS, DUPLICATE, RETRY, DISCARD}
}
