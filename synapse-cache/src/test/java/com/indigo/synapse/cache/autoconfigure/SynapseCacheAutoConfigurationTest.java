package com.indigo.synapse.cache.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.indigo.synapse.cache.CacheClient;
import com.indigo.synapse.cache.CacheSpec;
import com.indigo.synapse.cache.CacheValueCodec;
import com.indigo.synapse.cache.idempotency.IdempotencyGuard;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

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
            assertNotNull(context.getBean(IdempotencyGuard.class));
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

    @Test
    void shouldReuseObjectMapperFromApplicationContext() {
        ObjectMapper objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        contextRunner
                .withBean(ObjectMapper.class, () -> objectMapper)
                .run(context -> {
                    assertNotNull(context.getBean(ObjectMapper.class));

                    String encoded = context.getBean(CacheValueCodec.class).encode(new CacheJsonSample("value"));

                    assertTrue(encoded.contains("\"some_value\":\"value\""));
                });
    }

    @Test
    void shouldBackOffWhenStringRedisTemplateIsMissing() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SynapseCacheAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(CacheClient.class);
                    assertThat(context).doesNotHaveBean(RedisCacheStore.class);
                });
    }

    @Test
    void shouldKeepUserCacheClient() {
        CacheClient custom = new CacheClient() {
            @Override
            public <T> java.util.Optional<T> get(com.indigo.synapse.cache.CacheKeyRef key, Class<T> valueType) {
                return java.util.Optional.empty();
            }

            @Override
            public <T> void put(com.indigo.synapse.cache.CacheKeyRef key, T value, java.time.Duration ttl) {
            }

            @Override
            public void evict(com.indigo.synapse.cache.CacheKeyRef key) {
            }

            @Override
            public <T> T getOrLoad(com.indigo.synapse.cache.CacheKeyRef key, Class<T> valueType,
                                   java.util.function.Supplier<T> loader, CacheSpec cacheSpec) {
                return loader.get();
            }
        };

        contextRunner.withBean(CacheClient.class, () -> custom)
                .run(context -> assertThat(context.getBean(CacheClient.class)).isSameAs(custom));
    }

    private record CacheJsonSample(String someValue) {
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
