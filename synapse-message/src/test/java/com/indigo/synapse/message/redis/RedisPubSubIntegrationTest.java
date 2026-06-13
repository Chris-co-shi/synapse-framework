package com.indigo.synapse.message.redis;

import com.indigo.synapse.message.core.MessageEnvelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class RedisPubSubIntegrationTest {

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private LettuceConnectionFactory connectionFactory;
    private RedisMessageListenerContainer listenerContainer;

    @AfterEach
    void tearDown() throws Exception {
        if (listenerContainer != null) {
            listenerContainer.stop();
            listenerContainer.destroy();
        }
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @Test
    void shouldSubscribePublishAndUnsubscribe() throws Exception {
        StringRedisTemplate redisTemplate = redisTemplate();
        RedisPubSubMessageCodec codec = new RedisPubSubMessageCodec(null);
        listenerContainer = listenerContainer();
        RedisMessageSubscriber subscriber = new RedisMessageSubscriber(listenerContainer, codec);
        RedisMessagePublisher publisher = new RedisMessagePublisher(redisTemplate, codec);
        CountDownLatch received = new CountDownLatch(1);
        AtomicReference<MessageEnvelope> receivedMessage = new AtomicReference<>();
        MessageEnvelope message = MessageEnvelope.create("synapse:message:pubsub:test", "tag", "key", Map.of(), "hello", "trace", "tenant");

        subscriber.subscribe("synapse:message:pubsub:test", envelope -> {
            receivedMessage.set(envelope);
            received.countDown();
        });
        long subscribers = publisher.publish(message).subscriberCount();

        assertTrue(subscribers >= 1);
        assertTrue(received.await(2, TimeUnit.SECONDS));
        assertEquals(message.messageId(), receivedMessage.get().messageId());
        assertEquals("hello", receivedMessage.get().payload());

        subscriber.unsubscribe("synapse:message:pubsub:test");
        CountDownLatch afterUnsubscribe = new CountDownLatch(1);
        subscriber.subscribe("synapse:message:pubsub:other", envelope -> afterUnsubscribe.countDown());
        publisher.publish(MessageEnvelope.create("synapse:message:pubsub:test", null, null, Map.of(), "ignored", null, null));

        assertFalse(afterUnsubscribe.await(Duration.ofMillis(300).toMillis(), TimeUnit.MILLISECONDS));
    }

    private StringRedisTemplate redisTemplate() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    private RedisMessageListenerContainer listenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.afterPropertiesSet();
        return container;
    }
}
