package com.indigo.synapse.message.interaction;

/**
 * 消息交互阶段。
 */
public enum MessageInteractionStage {

    ACCEPTED,
    ROUTED,
    SEND_ATTEMPT,
    SENT,
    CALLBACK_RECEIVED,
    DELIVERED,
    READ,
    FAILED,
    RETRY_SCHEDULED,
    DEAD_LETTERED,
    COMPENSATED,
    CANCELLED
}
