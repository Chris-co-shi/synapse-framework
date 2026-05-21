package com.indigo.synapse.cache.integration;

import com.indigo.synapse.cache.lock.LockAcquireResult;
import com.indigo.synapse.cache.lock.LockReleaseResult;
import com.indigo.synapse.cache.lock.RedisReentrantLock;
import com.indigo.synapse.cache.ratelimit.RateLimitDecision;
import com.indigo.synapse.cache.ratelimit.SlidingWindowRateLimiter;
import com.indigo.synapse.cache.redis.SpringDataRedisScriptExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class RedisLuaIntegrationTest {

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private LettuceConnectionFactory connectionFactory;

    @AfterEach
    void tearDown() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @Test
    void shouldAcquireReenterAndReleaseLockOnRedis() {
        StringRedisTemplate redisTemplate = redisTemplate();
        RedisReentrantLock lock = new RedisReentrantLock(new SpringDataRedisScriptExecutor(redisTemplate));
        String key = "synapse:cache:test:lock:1";

        LockAcquireResult first = lock.acquire(key, "owner-1", Duration.ofSeconds(30));
        LockAcquireResult reentered = lock.acquire(key, "owner-1", Duration.ofSeconds(30));
        LockAcquireResult blocked = lock.acquire(key, "owner-2", Duration.ofSeconds(30));
        LockReleaseResult mismatch = lock.release(key, "owner-2", Duration.ofSeconds(30));
        LockReleaseResult partial = lock.release(key, "owner-1", Duration.ofSeconds(30));
        LockReleaseResult released = lock.release(key, "owner-1", Duration.ofSeconds(30));

        assertTrue(first.acquired());
        assertFalse(first.reentered());
        assertTrue(reentered.acquired());
        assertTrue(reentered.reentered());
        assertFalse(blocked.acquired());
        assertFalse(mismatch.ownerMatched());
        assertFalse(partial.released());
        assertEquals(1, partial.remainingHoldCount());
        assertTrue(released.released());
        assertNull(redisTemplate.opsForHash().get(key, "owner-1"));
    }

    @Test
    void shouldApplySlidingWindowRateLimitOnRedis() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(new SpringDataRedisScriptExecutor(redisTemplate()));
        String key = "synapse:cache:test:rate:user-1";

        RateLimitDecision first = limiter.allow(key, 2, Duration.ofSeconds(10), 1000L);
        RateLimitDecision second = limiter.allow(key, 2, Duration.ofSeconds(10), 2000L);
        RateLimitDecision denied = limiter.allow(key, 2, Duration.ofSeconds(10), 3000L);
        RateLimitDecision recovered = limiter.allow(key, 2, Duration.ofSeconds(10), 12001L);

        assertTrue(first.allowed());
        assertEquals(1, first.remaining());
        assertTrue(second.allowed());
        assertEquals(0, second.remaining());
        assertFalse(denied.allowed());
        assertEquals(11000L, denied.resetAtMillis());
        assertTrue(recovered.allowed());
    }

    private StringRedisTemplate redisTemplate() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
