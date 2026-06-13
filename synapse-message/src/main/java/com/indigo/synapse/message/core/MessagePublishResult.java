package com.indigo.synapse.message.core;

/**
 * 消息发布结果。
 *
 * @param topic 目标 topic
 * @param messageId 消息 ID
 * @param subscriberCount Redis 返回的当前在线订阅者数量
 */
public record MessagePublishResult(String topic, String messageId, long subscriberCount) {

    public MessagePublishResult {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("topic must not be blank");
        }
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId must not be blank");
        }
        if (subscriberCount < 0) {
            throw new IllegalArgumentException("subscriberCount must not be negative");
        }
    }
}
