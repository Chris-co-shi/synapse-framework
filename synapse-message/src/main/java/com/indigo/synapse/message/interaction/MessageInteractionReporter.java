package com.indigo.synapse.message.interaction;

/**
 * 消息交互事件上报端口。
 */
@FunctionalInterface
public interface MessageInteractionReporter {

    void report(MessageInteractionEvent event);
}
