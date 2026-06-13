package com.indigo.synapse.message.redis;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RedisMessageSubscriberTest {

    @Test
    void shouldValidateSubscriberInput() {
        RedisMessageSubscriber subscriber = new RedisMessageSubscriber(
                new RedisMessageListenerContainer(),
                new RedisPubSubMessageCodec(null)
        );

        assertThrows(IllegalArgumentException.class, () -> new RedisMessageSubscriber(null, new RedisPubSubMessageCodec(null)));
        assertThrows(IllegalArgumentException.class, () -> new RedisMessageSubscriber(new RedisMessageListenerContainer(), null));
        assertThrows(IllegalArgumentException.class, () -> subscriber.subscribe("", message -> {
        }));
        assertThrows(IllegalArgumentException.class, () -> subscriber.subscribe("topic", null));
        assertThrows(IllegalArgumentException.class, () -> subscriber.unsubscribe(""));
    }
}
