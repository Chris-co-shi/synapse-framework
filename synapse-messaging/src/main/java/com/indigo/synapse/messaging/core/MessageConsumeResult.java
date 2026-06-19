package com.indigo.synapse.messaging.core;

import java.util.Objects;

/**
 * 消息消费结果。
 *
 * <p>消费方通过该结果表达本次处理结论。
 * 具体 ACK、重试、死信、丢弃等行为由 MQ 适配器根据状态转换。</p>
 */
public record MessageConsumeResult(
        Status status,
        String reason
) {

    public MessageConsumeResult {
        status = Objects.requireNonNull(status, "status must not be null");
        reason = reason == null ? "" : reason;
    }

    /**
     * 消费成功。
     *
     * <p>适配器通常应将其转换为 ACK / CONSUME_SUCCESS。</p>
     */
    public static MessageConsumeResult success() {
        return new MessageConsumeResult(Status.SUCCESS, "");
    }

    /**
     * 消费失败，建议后续重试。
     *
     * <p>适配器通常应将其转换为 RECONSUME / retry。</p>
     */
    public static MessageConsumeResult retry(String reason) {
        return new MessageConsumeResult(Status.RETRY, reason);
    }

    /**
     * 消费失败，但不建议继续重试。
     *
     * <p>适配器可以根据自身策略选择 ACK 后记录、投递死信、告警或丢弃。</p>
     */
    public static MessageConsumeResult discard(String reason) {
        return new MessageConsumeResult(Status.DISCARD, reason);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isRetryable() {
        return status == Status.RETRY;
    }

    public boolean isDiscardable() {
        return status == Status.DISCARD;
    }

    /**
     * 消费处理状态。
     */
    public enum Status {

        /**
         * 处理成功。
         */
        SUCCESS,

        /**
         * 处理失败，建议重试。
         */
        RETRY,

        /**
         * 处理失败，不建议继续重试。
         */
        DISCARD
    }
}