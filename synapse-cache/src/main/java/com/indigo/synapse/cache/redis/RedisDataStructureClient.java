package com.indigo.synapse.cache.redis;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface RedisDataStructureClient {

    Long listLeftPush(String key, String... values);

    Long listRightPush(String key, String... values);

    List<String> listRange(String key, long start, long end);

    String listLeftPop(String key);

    String listRightPop(String key);

    Long listTrim(String key, long start, long end);

    Long listSize(String key);

    Long listRemove(String key, long count, String value);

    String listIndex(String key, long index);

    void hashPut(String key, String hashKey, String value);

    void hashPutAll(String key, Map<String, String> entries);

    Optional<String> hashGet(String key, String hashKey);

    Map<Object, Object> hashEntries(String key);

    Long hashDelete(String key, String... hashKeys);

    Long hashIncrement(String key, String hashKey, long delta);

    Long setAdd(String key, String... values);

    Long setRemove(String key, String... values);

    Set<String> setMembers(String key);

    boolean setContains(String key, String value);

    Long setSize(String key);

    Long zsetAdd(String key, String value, double score);

    Long zsetAdd(String key, Map<String, Double> values);

    Set<String> zsetRange(String key, long start, long end);

    Set<String> zsetRangeByScore(String key, double min, double max);

    Long zsetRemove(String key, String... values);

    Double zsetScore(String key, String value);

    Long zsetSize(String key);

    Double zsetIncrementScore(String key, String value, double delta);
}
