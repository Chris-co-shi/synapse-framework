package com.indigo.synapse.message.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.message.publisher.MessagePublisher;
import com.indigo.synapse.message.redis.RedisMessagePublisher;
import com.indigo.synapse.message.redis.RedisMessageSubscriber;
import com.indigo.synapse.message.redis.RedisPubSubMessageCodec;
import com.indigo.synapse.message.subscriber.MessageSubscriber;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis Pub/Sub 消息自动配置。
 *
 * <p>该能力默认关闭，只在配置显式开启并且 Redis 运行时 Bean 存在时注册。
 * Redis Pub/Sub 只提供在线广播，不替代可靠消息投递。</p>
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass({StringRedisTemplate.class, RedisMessageListenerContainer.class})
@ConditionalOnProperty(prefix = "synapse.message.redis.pubsub", name = "enabled", havingValue = "true")
public class SynapseRedisPubSubMessageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisPubSubMessageCodec synapseRedisPubSubMessageCodec(ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new RedisPubSubMessageCodec(objectMapperProvider.getIfAvailable(ObjectMapper::new));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisMessageListenerContainer synapseMessageRedisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        return container;
    }

    @Bean
    @ConditionalOnMissingBean(MessagePublisher.class)
    @ConditionalOnBean(StringRedisTemplate.class)
    public MessagePublisher synapseRedisMessagePublisher(
            StringRedisTemplate stringRedisTemplate,
            RedisPubSubMessageCodec messageCodec
    ) {
        return new RedisMessagePublisher(stringRedisTemplate, messageCodec);
    }

    @Bean
    @ConditionalOnMissingBean(MessageSubscriber.class)
    @ConditionalOnBean(RedisMessageListenerContainer.class)
    public MessageSubscriber synapseRedisMessageSubscriber(
            RedisMessageListenerContainer listenerContainer,
            RedisPubSubMessageCodec messageCodec
    ) {
        return new RedisMessageSubscriber(listenerContainer, messageCodec);
    }
}
