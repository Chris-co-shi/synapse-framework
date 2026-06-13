package com.indigo.synapse.message.redis;

import com.indigo.synapse.message.core.MessageEnvelope;
import com.indigo.synapse.message.core.MessagePublishResult;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedisMessagePublisherTest {

    @Test
    void shouldPublishEncodedMessageEnvelope() {
        CapturingStringRedisTemplate redisTemplate = new CapturingStringRedisTemplate(2L);
        RedisMessagePublisher publisher = new RedisMessagePublisher(redisTemplate, new RedisPubSubMessageCodec(null));
        MessageEnvelope message = MessageEnvelope.create("topic-1", "tag", "key", Map.of(), "payload", "trace", "tenant");

        MessagePublishResult result = publisher.publish(message);

        assertEquals("topic-1", redisTemplate.channel);
        assertEquals(2L, result.subscriberCount());
        assertEquals(message.messageId(), result.messageId());
    }

    @Test
    void shouldValidatePublisherInput() {
        RedisMessagePublisher publisher = new RedisMessagePublisher(new CapturingStringRedisTemplate(0L), new RedisPubSubMessageCodec(null));

        assertThrows(IllegalArgumentException.class, () -> new RedisMessagePublisher(null, new RedisPubSubMessageCodec(null)));
        assertThrows(IllegalArgumentException.class, () -> new RedisMessagePublisher(new CapturingStringRedisTemplate(0L), null));
        assertThrows(IllegalArgumentException.class, () -> publisher.publish(null));
    }

    private static final class CapturingStringRedisTemplate extends StringRedisTemplate {

        private final Long result;
        private String channel;

        private CapturingStringRedisTemplate(Long result) {
            this.result = result;
        }

        @Override
        public Long convertAndSend(String channel, Object message) {
            this.channel = channel;
            return result;
        }
    }
}
