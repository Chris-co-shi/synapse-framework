package com.indigo.synapse.message.port;

import com.indigo.synapse.message.core.MessageEnvelope;
import com.indigo.synapse.message.core.MessagePublishResult;

/**
 * 消息中间件无关的传输端口。
 *
 * <p>该端口表示 broker/transport 层投递能力，不表示钉钉、邮件、短信等业务触达渠道。</p>
 */
public interface MessageTransport {

    MessagePublishResult send(MessageEnvelope message);
}
