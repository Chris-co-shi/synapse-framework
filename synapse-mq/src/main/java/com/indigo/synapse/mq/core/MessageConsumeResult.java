package com.indigo.synapse.mq.core;

/**
 * 消息消费结果。
 *
 * <p>消费方通过该结果表达本次处理是否成功，以及失败时是否建议重试。
 * 具体重试、死信和确认机制由 MQ 适配器实现。</p>
 */
public record MessageConsumeResult(
        boolean success,
        boolean retryable,
        String reason
) {

    public static MessageConsumeResult success() {
        return new MessageConsumeResult(true, false, null);
    }

    public static MessageConsumeResult retryableFailure(String reason) {
        return new MessageConsumeResult(false, true, reason);
    }

    public static MessageConsumeResult nonRetryableFailure(String reason) {
        return new MessageConsumeResult(false, false, reason);
    }
}
