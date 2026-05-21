package com.indigo.synapse.cache.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.cache.CacheClient;
import com.indigo.synapse.cache.CacheSpec;
import com.indigo.synapse.cache.CacheValueCodec;
import com.indigo.synapse.cache.DefaultCacheValueCodec;
import com.indigo.synapse.cache.TieredCacheClient;
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

@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(StringRedisTemplate.class)
@EnableConfigurationProperties(com.indigo.synapse.cache.SynapseCacheProperties.class)
public class SynapseCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheSpec synapseCacheSpec(com.indigo.synapse.cache.SynapseCacheProperties properties) {
        return properties.toCacheSpec();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheValueCodec synapseCacheValueCodec(ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new DefaultCacheValueCodec(objectMapperProvider.getIfAvailable(ObjectMapper::new));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "synapse.cache.l1", name = "enabled", havingValue = "true", matchIfMissing = true)
    public LocalCacheStore synapseLocalCacheStore(com.indigo.synapse.cache.SynapseCacheProperties properties) {
        return new CaffeineLocalCacheStore(properties.getL1().getMaximumSize());
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisCacheStore synapseRedisCacheStore(StringRedisTemplate stringRedisTemplate) {
        return new StringRedisCacheStore(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisDataStructureClient synapseRedisDataStructureClient(StringRedisTemplate stringRedisTemplate) {
        return new StringRedisDataStructureClient(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisScriptExecutor synapseRedisScriptExecutor(StringRedisTemplate stringRedisTemplate) {
        return new SpringDataRedisScriptExecutor(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisReentrantLock synapseRedisReentrantLock(RedisScriptExecutor redisScriptExecutor) {
        return new RedisReentrantLock(redisScriptExecutor);
    }

    @Bean
    @ConditionalOnMissingBean
    public SlidingWindowRateLimiter synapseSlidingWindowRateLimiter(RedisScriptExecutor redisScriptExecutor) {
        return new SlidingWindowRateLimiter(redisScriptExecutor);
    }

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
