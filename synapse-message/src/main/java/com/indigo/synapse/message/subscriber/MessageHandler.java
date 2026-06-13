package com.indigo.synapse.message.subscriber;

import com.indigo.synapse.message.core.MessageEnvelope;

/**
 * 通用消息处理器。
 */
@FunctionalInterface
public interface MessageHandler {

    /**
     * 处理收到的消息外壳。
     */
    void handle(MessageEnvelope message);
}
