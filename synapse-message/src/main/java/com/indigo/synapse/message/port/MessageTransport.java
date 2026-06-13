package com.indigo.synapse.message.port;

import com.indigo.synapse.message.core.MessageEnvelope;
import com.indigo.synapse.message.core.MessagePublishResult;

/**
 * MQ 无关消息传输端口。
 */
public interface MessageTransport {

    MessagePublishResult send(MessageEnvelope message);
}
