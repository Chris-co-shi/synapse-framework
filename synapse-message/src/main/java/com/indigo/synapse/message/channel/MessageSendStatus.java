package com.indigo.synapse.message.channel;

/**
 * 消息发送结果状态。
 */
public enum MessageSendStatus {

    ACCEPTED,
    SENT,
    FAILED,
    RETRYABLE_FAILED,
    IGNORED,
    UNKNOWN
}
