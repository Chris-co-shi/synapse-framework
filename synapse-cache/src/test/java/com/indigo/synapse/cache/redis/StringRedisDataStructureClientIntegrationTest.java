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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        client.listSet("list:key", 1, "aa");
        assertEquals("aa", client.listIndex("list:key", 1));
        assertEquals(4L, client.listRightPush("list:key", "aa", "after-aa"));
        assertEquals(5L, client.listLeftPush("list:key", "aa", "before-aa"));
        assertEquals("c", client.listLeftPop("list:key"));
        assertEquals(4L, client.listSize("list:key"));

        client.hashPut("hash:key", "f1", "v1");
        client.hashPutAll("hash:key", Map.of("f2", "v2"));
        assertEquals("v1", client.hashGet("hash:key", "f1").orElseThrow());
        assertEquals(2L, client.hashEntries("hash:key").size());
        assertTrue(client.hashHasKey("hash:key", "f2"));
        assertEquals(Set.of("f1", "f2"), client.hashKeys("hash:key"));
        assertTrue(client.hashValues("hash:key").containsAll(List.of("v1", "v2")));
        assertEquals(2L, client.hashSize("hash:key"));
        assertEquals(1L, client.hashDelete("hash:key", "f1"));

        assertEquals(2L, client.setAdd("set:key", "x", "y"));
        assertEquals(2L, client.setAdd("set:other", "y", "z"));
        assertTrue(client.setContains("set:key", "x"));
        assertEquals(2L, client.setSize("set:key"));
        assertEquals(Set.of("y"), client.setIntersect("set:key", "set:other"));
        assertEquals(Set.of("x", "y", "z"), client.setUnion("set:key", "set:other"));
        assertEquals(Set.of("x"), client.setDifference("set:key", "set:other"));
        assertNotNull(client.setRandomMember("set:key"));
        assertEquals(1L, client.setRemove("set:key", "x"));
        assertFalse(client.setContains("set:key", "x"));
        assertNotNull(client.setPop("set:key"));

        assertEquals(1L, client.zsetAdd("zset:key", "a", 1.0));
        assertEquals(2L, client.zsetAdd("zset:key", Map.of("b", 2.0, "c", 3.0)));
        assertEquals(3L, client.zsetSize("zset:key"));
        assertEquals(Set.of("a", "b", "c"), client.zsetRange("zset:key", 0, -1));
        assertEquals(Set.of("c", "b", "a"), client.zsetReverseRange("zset:key", 0, -1));
        assertEquals(2.0, client.zsetScore("zset:key", "b"), 0.0001);
        assertEquals(1L, client.zsetRank("zset:key", "b"));
        assertEquals(1L, client.zsetReverseRank("zset:key", "b"));
        assertEquals(2L, client.zsetCount("zset:key", 1.0, 2.0));
        assertEquals(1L, client.zsetRemoveRangeByScore("zset:key", 3.0, 3.0));
        assertEquals(2L, client.zsetSize("zset:key"));
        assertEquals(1L, client.zsetRemove("zset:key", "a"));
        assertEquals(1L, client.zsetRemoveRange("zset:key", 0, 0));
    }

    @Test
    void shouldValidateStructureInput() {
        StringRedisDataStructureClient client = new StringRedisDataStructureClient(redisTemplate());

        assertThrows(IllegalArgumentException.class, () -> client.listLeftPush("", "a"));
        assertThrows(IllegalArgumentException.class, () -> client.listLeftPush("list:invalid"));
        assertThrows(IllegalArgumentException.class, () -> client.hashPut("hash:invalid", "", "v"));
        assertThrows(IllegalArgumentException.class, () -> client.setAdd("set:invalid"));
        assertThrows(IllegalArgumentException.class, () -> client.zsetAdd("zset:invalid", "", 1.0));
    }

    private StringRedisTemplate redisTemplate() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
