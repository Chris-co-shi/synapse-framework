package com.indigo.synapse.cache.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.cache.CacheClient;
import com.indigo.synapse.cache.CacheSpec;
import com.indigo.synapse.cache.CacheValueCodec;
import com.indigo.synapse.cache.DefaultCacheValueCodec;
import com.indigo.synapse.cache.TieredCacheClient;
import com.indigo.synapse.cache.idempotency.IdempotencyGuard;
import com.indigo.synapse.cache.idempotency.RedisIdempotencyGuard;
import com.indigo.synapse.cache.local.CaffeineLocalCacheStore;
import com.indigo.synapse.cache.local.LocalCacheStore;
import com.indigo.synapse.cache.lock.RedisReentrantLock;
import com.indigo.synapse.cache.redis.RedisDataStructureClient;
import com.indigo.synapse.cache.redis.RedisCacheStore;
import com.indigo.synapse.cache.redis.SpringDataRedisScriptExecutor;
import com.indigo.synapse.cache.redis.StringRedisDataStructureClient;
import com.indigo.synapse.cache.redis.StringRedisCacheStore;
import com.indigo.synapse.cache.ratelimit.SlidingWindowRateLimiter;
import com.indigo.synapse.cache.script.RedisScriptExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Synapse Cache 自动配置。
 *
 * <p>该配置只在 Spring Data Redis 可用时启用，并允许消费方通过声明同类型 Bean 覆盖默认实现。
 * 本模块只装配缓存、锁、限流、幂等和 Redis 基础数据结构能力，不定义业务缓存 key、业务锁粒度、
 * 业务限流维度或消息发布订阅语义。</p>
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(StringRedisTemplate.class)
@EnableConfigurationProperties(com.indigo.synapse.cache.SynapseCacheProperties.class)
public class SynapseCacheAutoConfiguration {

    /**
     * 根据配置创建默认缓存规格。
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheSpec synapseCacheSpec(com.indigo.synapse.cache.SynapseCacheProperties properties) {
        return properties.toCacheSpec();
    }

    /**
     * 创建缓存值编解码器。
     *
     * <p>如果业务系统已经提供 ObjectMapper，会复用业务 ObjectMapper；否则使用本模块默认 ObjectMapper。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheValueCodec synapseCacheValueCodec(ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new DefaultCacheValueCodec(objectMapperProvider.getIfAvailable(ObjectMapper::new));
    }

    /**
     * 创建 L1 本地缓存。
     *
     * <p>可通过 synapse.cache.l1.enabled=false 关闭。关闭后 CacheClient 仍可使用 Redis L2。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "synapse.cache.l1", name = "enabled", havingValue = "true", matchIfMissing = true)
    public LocalCacheStore synapseLocalCacheStore(com.indigo.synapse.cache.SynapseCacheProperties properties) {
        return new CaffeineLocalCacheStore(properties.getL1().getMaximumSize());
    }

    /**
     * 创建 Redis 字符串缓存存储。
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisCacheStore synapseRedisCacheStore(StringRedisTemplate stringRedisTemplate) {
        return new StringRedisCacheStore(stringRedisTemplate);
    }

    /**
     * 创建 Redis 常用数据结构客户端。
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisDataStructureClient synapseRedisDataStructureClient(StringRedisTemplate stringRedisTemplate) {
        return new StringRedisDataStructureClient(stringRedisTemplate);
    }

    /**
     * 创建 Redis Lua 脚本执行器。
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisScriptExecutor synapseRedisScriptExecutor(StringRedisTemplate stringRedisTemplate) {
        return new SpringDataRedisScriptExecutor(stringRedisTemplate);
    }

    /**
     * 创建 Redis Lua 可重入锁。
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisReentrantLock synapseRedisReentrantLock(RedisScriptExecutor redisScriptExecutor) {
        return new RedisReentrantLock(redisScriptExecutor);
    }

    /**
     * 创建 Redis Lua 滑动窗口限流器。
     */
    @Bean
    @ConditionalOnMissingBean
    public SlidingWindowRateLimiter synapseSlidingWindowRateLimiter(RedisScriptExecutor redisScriptExecutor) {
        return new SlidingWindowRateLimiter(redisScriptExecutor);
    }

    /**
     * 创建 Redis 幂等 Guard。
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotencyGuard synapseIdempotencyGuard(StringRedisTemplate stringRedisTemplate) {
        return new RedisIdempotencyGuard(stringRedisTemplate);
    }

    /**
     * 创建默认两级缓存客户端。
     */
    @Bean
    @ConditionalOnMissingBean(CacheClient.class)
    @ConditionalOnBean(RedisCacheStore.class)
    public CacheClient synapseCacheClient(
            @org.springframework.lang.Nullable LocalCacheStore localCacheStore,
            RedisCacheStore redisCacheStore,
            CacheValueCodec cacheValueCodec,
            CacheSpec cacheSpec
    ) {
        return new TieredCacheClient(localCacheStore, redisCacheStore, cacheValueCodec, cacheSpec);
    }
}
