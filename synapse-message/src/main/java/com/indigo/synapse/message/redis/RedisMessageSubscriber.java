package com.indigo.synapse.message.redis;

import com.indigo.synapse.message.subscriber.MessageHandler;
import com.indigo.synapse.message.subscriber.MessageSubscriber;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于 Redis Pub/Sub 的消息订阅器。
 */
public final class RedisMessageSubscriber implements MessageSubscriber {

    private final RedisMessageListenerContainer listenerContainer;
    private final RedisPubSubMessageCodec messageCodec;
    private final RedisSerializer<String> stringSerializer = RedisSerializer.string();
    private final ConcurrentMap<String, MessageListener> listeners = new ConcurrentHashMap<>();

    public RedisMessageSubscriber(RedisMessageListenerContainer listenerContainer, RedisPubSubMessageCodec messageCodec) {
        if (listenerContainer == null) {
            throw new IllegalArgumentException("listenerContainer must not be null");
        }
        if (messageCodec == null) {
            throw new IllegalArgumentException("messageCodec must not be null");
        }
        this.listenerContainer = listenerContainer;
        this.messageCodec = messageCodec;
    }

    @Override
    public void subscribe(String topic, MessageHandler handler) {
        validateTopic(topic);
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        MessageListener listener = (message, pattern) -> handler.handle(messageCodec.decode(resolveBody(message)));
        MessageListener previous = listeners.put(topic, listener);
        if (previous != null) {
            listenerContainer.removeMessageListener(previous, new ChannelTopic(topic));
        }
        listenerContainer.addMessageListener(listener, new ChannelTopic(topic));
        if (!listenerContainer.isRunning()) {
            listenerContainer.start();
        }
    }

    @Override
    public void unsubscribe(String topic) {
        validateTopic(topic);
        MessageListener listener = listeners.remove(topic);
        if (listener != null) {
            listenerContainer.removeMessageListener(listener, new ChannelTopic(topic));
        }
    }

    private String resolveBody(org.springframework.data.redis.connection.Message message) {
        String body = stringSerializer.deserialize(message.getBody());
        if (body == null) {
            return new String(message.getBody(), StandardCharsets.UTF_8);
        }
        return body;
    }

    private static void validateTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("topic must not be blank");
        }
    }
}
