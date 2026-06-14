package com.indigo.synapse.cache.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        String namespace = "test:" + UUID.randomUUID() + ":";
        String listKey = namespace + "list";
        String hashKey = namespace + "hash";
        String setKey = namespace + "set";
        String otherSetKey = namespace + "set:other";
        String zsetKey = namespace + "zset";

        client.listRightPush(listKey, "a", "b");
        assertEquals(2L, client.listSize(listKey));
        client.listLeftPush(listKey, "c");
        assertEquals(List.of("c", "a", "b"), client.listRange(listKey, 0, -1));
        client.listSet(listKey, 1, "aa");
        assertEquals("aa", client.listIndex(listKey, 1));
        client.listRightPush(listKey, "aa", "after-aa");
        client.listLeftPush(listKey, "aa", "before-aa");
        assertEquals(List.of("c", "before-aa", "aa", "after-aa", "b"), client.listRange(listKey, 0, -1));
        assertEquals("c", client.listLeftPop(listKey));
        assertEquals(4L, client.listSize(listKey));

        client.hashPut(hashKey, "f1", "v1");
        client.hashPutAll(hashKey, Map.of("f2", "v2"));
        assertEquals("v1", client.hashGet(hashKey, "f1").orElseThrow());
        assertEquals(2L, client.hashEntries(hashKey).size());
        assertTrue(client.hashHasKey(hashKey, "f2"));
        assertEquals(Set.of("f1", "f2"), client.hashKeys(hashKey));
        assertTrue(client.hashValues(hashKey).containsAll(List.of("v1", "v2")));
        assertEquals(2L, client.hashSize(hashKey));
        client.hashDelete(hashKey, "f1");
        assertFalse(client.hashHasKey(hashKey, "f1"));
        assertEquals(1L, client.hashSize(hashKey));

        client.setAdd(setKey, "x", "y");
        client.setAdd(otherSetKey, "y", "z");
        assertTrue(client.setContains(setKey, "x"));
        assertEquals(2L, client.setSize(setKey));
        assertEquals(Set.of("y"), client.setIntersect(setKey, otherSetKey));
        assertEquals(Set.of("x", "y", "z"), client.setUnion(setKey, otherSetKey));
        assertEquals(Set.of("x"), client.setDifference(setKey, otherSetKey));
        assertNotNull(client.setRandomMember(setKey));
        client.setRemove(setKey, "x");
        assertFalse(client.setContains(setKey, "x"));
        assertNotNull(client.setPop(setKey));
        assertEquals(0L, client.setSize(setKey));

        client.zsetAdd(zsetKey, "a", 1.0);
        client.zsetAdd(zsetKey, Map.of("b", 2.0, "c", 3.0));
        assertEquals(3L, client.zsetSize(zsetKey));
        assertEquals(Set.of("a", "b", "c"), client.zsetRange(zsetKey, 0, -1));
        assertEquals(Set.of("c", "b", "a"), client.zsetReverseRange(zsetKey, 0, -1));
        assertEquals(2.0, client.zsetScore(zsetKey, "b"), 0.0001);
        assertEquals(1L, client.zsetRank(zsetKey, "b"));
        assertEquals(1L, client.zsetReverseRank(zsetKey, "b"));
        assertEquals(2L, client.zsetCount(zsetKey, 1.0, 2.0));
        client.zsetRemoveRangeByScore(zsetKey, 3.0, 3.0);
        assertEquals(2L, client.zsetSize(zsetKey));
        client.zsetRemove(zsetKey, "a");
        assertEquals(1L, client.zsetSize(zsetKey));
        client.zsetRemoveRange(zsetKey, 0, 0);
        assertEquals(0L, client.zsetSize(zsetKey));
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
