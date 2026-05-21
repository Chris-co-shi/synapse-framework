package com.indigo.synapse.cache.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class StringRedisDataStructureClientIntegrationTest {

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
    void shouldOperateListHashSetAndZset() {
        StringRedisDataStructureClient client = new StringRedisDataStructureClient(redisTemplate());

        assertEquals(2L, client.listRightPush("list:key", "a", "b"));
        assertEquals(3L, client.listLeftPush("list:key", "c"));
        assertEquals(List.of("c", "a", "b"), client.listRange("list:key", 0, -1));
        assertEquals("c", client.listLeftPop("list:key"));
        assertEquals(2L, client.listSize("list:key"));

        client.hashPut("hash:key", "f1", "v1");
        client.hashPutAll("hash:key", Map.of("f2", "v2"));
        assertEquals("v1", client.hashGet("hash:key", "f1").orElseThrow());
        assertEquals(2L, client.hashEntries("hash:key").size());
        assertEquals(1L, client.hashDelete("hash:key", "f1"));

        assertEquals(2L, client.setAdd("set:key", "x", "y"));
        assertTrue(client.setContains("set:key", "x"));
        assertEquals(2L, client.setSize("set:key"));
        assertEquals(1L, client.setRemove("set:key", "x"));
        assertFalse(client.setContains("set:key", "x"));

        assertEquals(1L, client.zsetAdd("zset:key", "a", 1.0));
        assertEquals(2L, client.zsetAdd("zset:key", Map.of("b", 2.0, "c", 3.0)));
        assertEquals(3L, client.zsetSize("zset:key"));
        assertEquals(Set.of("a", "b", "c"), client.zsetRange("zset:key", 0, -1));
        assertEquals(2.0, client.zsetScore("zset:key", "b"), 0.0001);
        assertEquals(1L, client.zsetRemove("zset:key", "a"));
    }

    private StringRedisTemplate redisTemplate() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
