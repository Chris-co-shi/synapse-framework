package com.indigo.synapse.messaging.reliability;

/**
 * 消息消费幂等键。
 *
 * @param consumerId 消费方稳定标识
 * @param handlerId 处理器稳定标识
 * @param messageType 消息类型
 * @param messageIdentity eventId 优先、messageId 兜底的消息身份
 */
public record MessageIdempotencyKey(
        String consumerId,
        String handlerId,
        String messageType,
        String messageIdentity
) {
    public MessageIdempotencyKey {
        consumerId = requireText(consumerId, "consumerId");
        handlerId = requireText(handlerId, "handlerId");
        messageType = requireText(messageType, "messageType");
        messageIdentity = requireText(messageIdentity, "messageIdentity");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
