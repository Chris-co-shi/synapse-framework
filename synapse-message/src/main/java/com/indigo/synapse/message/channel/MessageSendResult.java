package com.indigo.synapse.message.channel;

import java.time.Instant;
import java.util.Map;

/**
 * 消息发送结果契约。
 *
 * <p>该模型只保存第三方消息 ID、请求 ID、响应码和摘要信息，不保存完整原始响应或凭据。</p>
 */
public record MessageSendResult(
        String messageId,
        MessageSendStatus status,
        boolean success,
        MessageChannel channel,
        String externalMessageId,
        String providerRequestId,
        String providerResponseCode,
        String providerResponseMessage,
        String errorCode,
        String errorMessage,
        boolean retryable,
        Instant sentAt,
        Long latencyMillis,
        Map<String, String> attributes
) {

    public MessageSendResult {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId must not be blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (channel == null) {
            throw new IllegalArgumentException("channel must not be null");
        }
        if (latencyMillis != null && latencyMillis < 0) {
            throw new IllegalArgumentException("latencyMillis must not be negative");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
