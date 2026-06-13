package com.indigo.synapse.message.publisher;

import com.indigo.synapse.message.core.MessageEnvelope;
import com.indigo.synapse.message.core.MessagePublishResult;

/**
 * 通用消息发布端口。
 */
public interface MessagePublisher {

    /**
     * 发布消息外壳。
     */
    MessagePublishResult publish(MessageEnvelope message);
}
