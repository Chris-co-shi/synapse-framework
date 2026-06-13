package com.indigo.synapse.cache.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.DefaultTypedTuple;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 基于 {@link StringRedisTemplate} 的 Redis 常用数据结构客户端。
 */
public final class StringRedisDataStructureClient implements RedisDataStructureClient {

    private final StringRedisTemplate redisTemplate;

    public StringRedisDataStructureClient(StringRedisTemplate redisTemplate) {
        if (redisTemplate == null) {
            throw new IllegalArgumentException("redisTemplate must not be null");
        }
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Long listLeftPush(String key, String... values) {
        validateKey(key);
        validateValues(values);
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    @Override
    public Long listRightPush(String key, String... values) {
        validateKey(key);
        validateValues(values);
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    @Override
    public List<String> listRange(String key, long start, long end) {
        validateKey(key);
        List<String> values = redisTemplate.opsForList().range(key, start, end);
        return values == null ? List.of() : values;
    }

    @Override
    public String listLeftPop(String key) {
        validateKey(key);
        return redisTemplate.opsForList().leftPop(key);
    }

    @Override
    public String listRightPop(String key) {
        validateKey(key);
        return redisTemplate.opsForList().rightPop(key);
    }

    @Override
    public Long listTrim(String key, long start, long end) {
        validateKey(key);
        redisTemplate.opsForList().trim(key, start, end);
        return listSize(key);
    }

    @Override
    public Long listSize(String key) {
        validateKey(key);
        Long size = redisTemplate.opsForList().size(key);
        return size == null ? 0L : size;
    }

    @Override
    public Long listRemove(String key, long count, String value) {
        validateKey(key);
        validateValue(value, "value");
        return redisTemplate.opsForList().remove(key, count, value);
    }

    @Override
    public String listIndex(String key, long index) {
        validateKey(key);
        return redisTemplate.opsForList().index(key, index);
    }

    @Override
    public void listSet(String key, long index, String value) {
        validateKey(key);
        validateValue(value, "value");
        redisTemplate.opsForList().set(key, index, value);
    }

    @Override
    public Long listLeftPush(String key, String pivot, String value) {
        validateKey(key);
        validateValue(pivot, "pivot");
        validateValue(value, "value");
        return redisTemplate.opsForList().leftPush(key, pivot, value);
    }

    @Override
    public Long listRightPush(String key, String pivot, String value) {
        validateKey(key);
        validateValue(pivot, "pivot");
        validateValue(value, "value");
        return redisTemplate.opsForList().rightPush(key, pivot, value);
    }

    @Override
    public void hashPut(String key, String hashKey, String value) {
        validateKey(key);
        validateValue(hashKey, "hashKey");
        validateValue(value, "value");
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public void hashPutAll(String key, Map<String, String> entries) {
        validateKey(key);
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("entries must not be empty");
        }
        redisTemplate.opsForHash().putAll(key, entries);
    }

    @Override
    public Optional<String> hashGet(String key, String hashKey) {
        validateKey(key);
        validateValue(hashKey, "hashKey");
        return Optional.ofNullable((String) redisTemplate.opsForHash().get(key, hashKey));
    }

    @Override
    public Map<Object, Object> hashEntries(String key) {
        validateKey(key);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        return entries == null ? Map.of() : Collections.unmodifiableMap(entries);
    }

    @Override
    public Long hashDelete(String key, String... hashKeys) {
        validateKey(key);
        validateValues(hashKeys);
        return redisTemplate.opsForHash().delete(key, (Object[]) hashKeys);
    }

    @Override
    public Long hashIncrement(String key, String hashKey, long delta) {
        validateKey(key);
        validateValue(hashKey, "hashKey");
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    @Override
    public boolean hashHasKey(String key, String hashKey) {
        validateKey(key);
        validateValue(hashKey, "hashKey");
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    @Override
    public Set<Object> hashKeys(String key) {
        validateKey(key);
        Set<Object> keys = redisTemplate.opsForHash().keys(key);
        return keys == null ? Set.of() : keys;
    }

    @Override
    public List<Object> hashValues(String key) {
        validateKey(key);
        List<Object> values = redisTemplate.opsForHash().values(key);
        return values == null ? List.of() : values;
    }

    @Override
    public Long hashSize(String key) {
        validateKey(key);
        Long size = redisTemplate.opsForHash().size(key);
        return size == null ? 0L : size;
    }

    @Override
    public Long setAdd(String key, String... values) {
        validateKey(key);
        validateValues(values);
        return redisTemplate.opsForSet().add(key, values);
    }

    @Override
    public Long setRemove(String key, String... values) {
        validateKey(key);
        validateValues(values);
        return redisTemplate.opsForSet().remove(key, (Object[]) values);
    }

    @Override
    public Set<String> setMembers(String key) {
        validateKey(key);
        Set<String> members = redisTemplate.opsForSet().members(key);
        return members == null ? Set.of() : members;
    }

    @Override
    public boolean setContains(String key, String value) {
        validateKey(key);
        validateValue(value, "value");
        Boolean result = redisTemplate.opsForSet().isMember(key, value);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public Long setSize(String key) {
        validateKey(key);
        Long size = redisTemplate.opsForSet().size(key);
        return size == null ? 0L : size;
    }

    @Override
    public Set<String> setIntersect(String key, String otherKey) {
        validateKey(key);
        validateKey(otherKey);
        Set<String> values = redisTemplate.opsForSet().intersect(key, otherKey);
        return values == null ? Set.of() : values;
    }

    @Override
    public Set<String> setUnion(String key, String otherKey) {
        validateKey(key);
        validateKey(otherKey);
        Set<String> values = redisTemplate.opsForSet().union(key, otherKey);
        return values == null ? Set.of() : values;
    }

    @Override
    public Set<String> setDifference(String key, String otherKey) {
        validateKey(key);
        validateKey(otherKey);
        Set<String> values = redisTemplate.opsForSet().difference(key, otherKey);
        return values == null ? Set.of() : values;
    }

    @Override
    public String setRandomMember(String key) {
        validateKey(key);
        return redisTemplate.opsForSet().randomMember(key);
    }

    @Override
    public String setPop(String key) {
        validateKey(key);
        return redisTemplate.opsForSet().pop(key);
    }

    @Override
    public Long zsetAdd(String key, String value, double score) {
        validateKey(key);
        validateValue(value, "value");
        return redisTemplate.opsForZSet().add(key, value, score) ? 1L : 0L;
    }

    @Override
    public Long zsetAdd(String key, Map<String, Double> values) {
        validateKey(key);
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }
        Set<ZSetOperations.TypedTuple<String>> tuples = values.entrySet()
                .stream()
                .map(entry -> new DefaultTypedTuple<>(entry.getKey(), entry.getValue()))
                .collect(java.util.stream.Collectors.toSet());
        return redisTemplate.opsForZSet().add(key, tuples);
    }

    @Override
    public Set<String> zsetRange(String key, long start, long end) {
        validateKey(key);
        Set<String> values = redisTemplate.opsForZSet().range(key, start, end);
        return values == null ? Set.of() : values;
    }

    @Override
    public Set<String> zsetRangeByScore(String key, double min, double max) {
        validateKey(key);
        Set<String> values = redisTemplate.opsForZSet().rangeByScore(key, min, max);
        return values == null ? Set.of() : values;
    }

    @Override
    public Long zsetRemove(String key, String... values) {
        validateKey(key);
        validateValues(values);
        return redisTemplate.opsForZSet().remove(key, (Object[]) values);
    }

    @Override
    public Double zsetScore(String key, String value) {
        validateKey(key);
        validateValue(value, "value");
        return redisTemplate.opsForZSet().score(key, value);
    }

    @Override
    public Long zsetSize(String key) {
        validateKey(key);
        Long size = redisTemplate.opsForZSet().size(key);
        return size == null ? 0L : size;
    }

    @Override
    public Double zsetIncrementScore(String key, String value, double delta) {
        validateKey(key);
        validateValue(value, "value");
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    @Override
    public Set<String> zsetReverseRange(String key, long start, long end) {
        validateKey(key);
        Set<String> values = redisTemplate.opsForZSet().reverseRange(key, start, end);
        return values == null ? Set.of() : values;
    }

    @Override
    public Long zsetRemoveRangeByScore(String key, double min, double max) {
        validateKey(key);
        return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    @Override
    public Long zsetRemoveRange(String key, long start, long end) {
        validateKey(key);
        return redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    @Override
    public Long zsetRank(String key, String value) {
        validateKey(key);
        validateValue(value, "value");
        return redisTemplate.opsForZSet().rank(key, value);
    }

    @Override
    public Long zsetReverseRank(String key, String value) {
        validateKey(key);
        validateValue(value, "value");
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    @Override
    public Long zsetCount(String key, double min, double max) {
        validateKey(key);
        Long count = redisTemplate.opsForZSet().count(key, min, max);
        return count == null ? 0L : count;
    }

    private static void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
    }

    private static void validateValue(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    private static void validateValues(String[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values must not be empty");
        }
        Arrays.stream(values).forEach(value -> validateValue(value, "value"));
    }
}
