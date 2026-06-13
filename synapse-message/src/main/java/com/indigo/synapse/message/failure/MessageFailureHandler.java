package com.indigo.synapse.message.failure;

/**
 * 消息失败策略决策接口。
 */
@FunctionalInterface
public interface MessageFailureHandler {

    MessageFailureStrategy handle(MessageFailureContext context);
}
