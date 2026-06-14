package com.indigo.synapse.mq.core;

/**
 * 消息发布结果。
 *
 * <p>该类型只描述框架层发布动作结果，不表达业务处理结果。
 * {@code brokerMessageId} 由未来具体 MQ 适配器在发布成功后填充。</p>
 */
public record MessagePublishResult(
        Status status,
        String messageId,
        String brokerMessageId,
        String reason
) {

    public MessagePublishResult {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        reason = reason == null ? "" : reason;
    }

    public static MessagePublishResult success(String messageId, String brokerMessageId) {
        return new MessagePublishResult(Status.SUCCESS, messageId, brokerMessageId, "");
    }

    public static MessagePublishResult failure(String messageId, String reason) {
        return new MessagePublishResult(Status.FAILED, messageId, null, reason);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    /**
     * 发布动作状态。
     */
    public enum Status {

        /**
         * 发布动作成功。
         */
        SUCCESS,

        /**
         * 发布动作失败。
         */
        FAILED
    }
}
