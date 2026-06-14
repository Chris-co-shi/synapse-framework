package com.indigo.synapse.mq.core;

/**
 * 消息发布结果。
 *
 * <p>该类型只描述框架层发布动作结果，不表达业务处理结果。</p>
 */
public record MessagePublishResult(
        boolean success,
        String messageId,
        String brokerMessageId,
        String reason
) {

    public static MessagePublishResult success(String messageId, String brokerMessageId) {
        return new MessagePublishResult(true, messageId, brokerMessageId, null);
    }

    public static MessagePublishResult failure(String messageId, String reason) {
        return new MessagePublishResult(false, messageId, null, reason);
    }
}
