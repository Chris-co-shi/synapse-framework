package com.indigo.synapse.message.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.message.publisher.MessagePublisher;
import com.indigo.synapse.message.redis.RedisPubSubMessageCodec;
import com.indigo.synapse.message.subscriber.MessageSubscriber;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseRedisPubSubMessageAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseRedisPubSubMessageAutoConfiguration.class));

    @Test
    void shouldNotRegisterRedisPubSubBeansByDefault() {
        contextRunner
                .withBean(RedisConnectionFactory.class, TestRedisConnectionFactory::new)
                .withBean(StringRedisTemplate.class, () -> new StringRedisTemplate(new TestRedisConnectionFactory()))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(MessagePublisher.class);
                    assertThat(context).doesNotHaveBean(MessageSubscriber.class);
                });
    }

    @Test
    void shouldRegisterRedisPubSubBeansWhenEnabledAndRedisBeansExist() {
        contextRunner
                .withPropertyValues("synapse.message.redis.pubsub.enabled=true")
                .withBean(RedisConnectionFactory.class, TestRedisConnectionFactory::new)
                .withBean(StringRedisTemplate.class, () -> new StringRedisTemplate(new TestRedisConnectionFactory()))
                .run(context -> {
                    assertThat(context).hasSingleBean(RedisPubSubMessageCodec.class);
                    assertThat(context).hasSingleBean(RedisMessageListenerContainer.class);
                    assertThat(context).hasSingleBean(MessagePublisher.class);
                    assertThat(context).hasSingleBean(MessageSubscriber.class);
                });
    }

    @Test
    void shouldNotRegisterPublisherWhenRedisTemplateMissing() {
        contextRunner
                .withPropertyValues("synapse.message.redis.pubsub.enabled=true")
                .withBean(RedisConnectionFactory.class, TestRedisConnectionFactory::new)
                .run(context -> assertThat(context).doesNotHaveBean(MessagePublisher.class));
    }

    @Test
    void shouldBackOffWhenApplicationProvidesBeans() {
        MessagePublisher publisher = message -> null;
        MessageSubscriber subscriber = new MessageSubscriber() {
            @Override
            public void subscribe(String topic, com.indigo.synapse.message.subscriber.MessageHandler handler) {
            }

            @Override
            public void unsubscribe(String topic) {
            }
        };

        contextRunner
                .withPropertyValues("synapse.message.redis.pubsub.enabled=true")
                .withBean(RedisConnectionFactory.class, TestRedisConnectionFactory::new)
                .withBean(StringRedisTemplate.class, () -> new StringRedisTemplate(new TestRedisConnectionFactory()))
                .withBean(ObjectMapper.class, ObjectMapper::new)
                .withBean(MessagePublisher.class, () -> publisher)
                .withBean(MessageSubscriber.class, () -> subscriber)
                .run(context -> {
                    assertThat(context).hasSingleBean(MessagePublisher.class);
                    assertThat(context).hasSingleBean(MessageSubscriber.class);
                    assertThat(context.getBean(MessagePublisher.class)).isSameAs(publisher);
                    assertThat(context.getBean(MessageSubscriber.class)).isSameAs(subscriber);
                });
    }

    private static final class TestRedisConnectionFactory implements RedisConnectionFactory {

        @Override
        public RedisConnection getConnection() {
            throw new UnsupportedOperationException("connection is not required for auto configuration test");
        }

        @Override
        public RedisClusterConnection getClusterConnection() {
            throw new UnsupportedOperationException("cluster connection is not required for auto configuration test");
        }

        @Override
        public boolean getConvertPipelineAndTxResults() {
            return true;
        }

        @Override
        public RedisSentinelConnection getSentinelConnection() {
            throw new UnsupportedOperationException("sentinel connection is not required for auto configuration test");
        }

        @Override
        public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
            return null;
        }
    }
}
