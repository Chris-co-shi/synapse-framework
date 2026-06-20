package com.indigo.synapse.messaging.producer;

import com.indigo.synapse.messaging.core.MessageEnvelope;
import com.indigo.synapse.messaging.core.MessagePublishResult;

/** Broker 中立的消息发布入口。 */
@FunctionalInterface
public interface MessagePublisher {
    MessagePublishResult publish(MessageEnvelope envelope);
}
