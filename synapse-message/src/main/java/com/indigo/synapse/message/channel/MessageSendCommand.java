package com.indigo.synapse.message.channel;

import java.util.Map;

/**
 * 消息发送命令契约。
 *
 * <p>channel 表示业务触达渠道，不表示消息中间件 broker。该命令不负责发送、不负责落库。</p>
 */
public record MessageSendCommand(
        String messageId,
        String topic,
        String tag,
        String key,
        MessageChannel channel,
        String receiver,
        String templateCode,
        Map<String, String> headers,
        Object payload,
        String traceId,
        String tenantId,
        String requestId,
        String idempotencyKey,
        Map<String, String> attributes
) {

    public MessageSendCommand {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId must not be blank");
        }
        if (channel == null) {
            throw new IllegalArgumentException("channel must not be null");
        }
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
