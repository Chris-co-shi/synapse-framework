package com.indigo.synapse.cache.autoconfigure;

import com.indigo.synapse.cache.CacheClient;
import com.indigo.synapse.cache.CacheSpec;
import com.indigo.synapse.cache.lock.RedisReentrantLock;
import com.indigo.synapse.cache.local.LocalCacheStore;
import com.indigo.synapse.cache.ratelimit.SlidingWindowRateLimiter;
import com.indigo.synapse.cache.redis.RedisCacheStore;
import com.indigo.synapse.cache.redis.SpringDataRedisScriptExecutor;
import com.indigo.synapse.cache.script.RedisScriptExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SynapseCacheAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseCacheAutoConfiguration.class))
            .withBean(RedisConnectionFactory.class, TestRedisConnectionFactory::new)
            .withBean(StringRedisTemplate.class, () -> new StringRedisTemplate(new TestRedisConnectionFactory()));

    @Test
    void shouldRegisterCacheFoundationBeansByDefault() {
        contextRunner.run(context -> {
            assertInstanceOf(SpringDataRedisScriptExecutor.class, context.getBean(RedisScriptExecutor.class));
            assertNotNull(context.getBean(RedisReentrantLock.class));
            assertNotNull(context.getBean(SlidingWindowRateLimiter.class));
            assertInstanceOf(CacheSpec.class, context.getBean(CacheSpec.class));
            assertNotNull(context.getBean(RedisCacheStore.class));
            assertNotNull(context.getBean(LocalCacheStore.class));
            assertNotNull(context.getBean(CacheClient.class));
        });
    }

    @Test
    void shouldAllowDisablingLocalCache() {
        contextRunner
                .withPropertyValues("synapse.cache.l1.enabled=false")
                .run(context -> {
                    assertFalse(context.containsBean("synapseLocalCacheStore"));
                    assertNotNull(context.getBean(CacheClient.class));
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
