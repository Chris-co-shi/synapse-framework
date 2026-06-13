package com.indigo.synapse.message.redis;

import com.indigo.synapse.message.core.MessageEnvelope;
import com.indigo.synapse.message.core.MessagePublishResult;
import com.indigo.synapse.message.port.MessageTransport;
import com.indigo.synapse.message.publisher.MessagePublisher;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基于 Redis Pub/Sub 的消息发布器。
 *
 * <p>Redis Pub/Sub 是在线广播能力，只返回当前在线订阅者数量，不保证持久化、
 * 离线消费、确认、重试或补偿。</p>
 */
public final class RedisMessagePublisher implements MessagePublisher, MessageTransport {

    private final StringRedisTemplate redisTemplate;
    private final RedisPubSubMessageCodec messageCodec;

    public RedisMessagePublisher(StringRedisTemplate redisTemplate, RedisPubSubMessageCodec messageCodec) {
        if (redisTemplate == null) {
            throw new IllegalArgumentException("redisTemplate must not be null");
        }
        if (messageCodec == null) {
            throw new IllegalArgumentException("messageCodec must not be null");
        }
        this.redisTemplate = redisTemplate;
        this.messageCodec = messageCodec;
    }

    @Override
    public MessagePublishResult publish(MessageEnvelope message) {
        validate(message);
        Long subscribers = redisTemplate.convertAndSend(message.topic(), messageCodec.encode(message));
        return new MessagePublishResult(message.topic(), message.messageId(), subscribers == null ? 0L : subscribers);
    }

    @Override
    public MessagePublishResult send(MessageEnvelope message) {
        return publish(message);
    }

    private static void validate(MessageEnvelope message) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        if (message.topic() == null || message.topic().isBlank()) {
            throw new IllegalArgumentException("message topic must not be blank");
        }
    }
}
