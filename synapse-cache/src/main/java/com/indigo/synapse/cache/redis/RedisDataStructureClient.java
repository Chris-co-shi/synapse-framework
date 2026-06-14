package com.indigo.synapse.cache.redis;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Redis 常用数据结构字符串操作端口。
 *
 * <p>该端口面向框架基础设施和消费方轻量使用场景，只暴露 String key/value。
 * 复杂对象序列化、业务 topic、可靠消息和补偿语义不在本接口中表达。</p>
 */
public interface RedisDataStructureClient {

    /**
     * 从列表左侧批量压入元素。
     */
    Long listLeftPush(String key, String... values);

    /**
     * 从列表右侧批量压入元素。
     */
    Long listRightPush(String key, String... values);

    /**
     * 按 Redis 下标范围读取列表元素，支持负数下标。
     */
    List<String> listRange(String key, long start, long end);

    /**
     * 从列表左侧弹出一个元素，列表为空时返回 {@code null}。
     */
    String listLeftPop(String key);

    /**
     * 从列表右侧弹出一个元素，列表为空时返回 {@code null}。
     */
    String listRightPop(String key);

    /**
     * 裁剪列表并返回裁剪后的长度。
     */
    Long listTrim(String key, long start, long end);

    /**
     * 返回列表长度。
     */
    Long listSize(String key);

    /**
     * 按 Redis LREM 语义移除列表元素。
     */
    Long listRemove(String key, long count, String value);

    /**
     * 按下标读取列表元素，不存在时返回 {@code null}。
     */
    String listIndex(String key, long index);

    /**
     * 按下标设置列表元素。
     */
    void listSet(String key, long index, String value);

    /**
     * 在 pivot 左侧插入元素。
     *
     * <p>该方法显式使用 insert 命名，避免和批量 listLeftPush(String, String...) 产生 Java 重载歧义。</p>
     */
    Long listInsertBefore(String key, String pivot, String value);

    /**
     * 在 pivot 右侧插入元素。
     *
     * <p>该方法显式使用 insert 命名，避免和批量 listRightPush(String, String...) 产生 Java 重载歧义。</p>
     */
    Long listInsertAfter(String key, String pivot, String value);

    /**
     * 写入 hash 字段。
     */
    void hashPut(String key, String hashKey, String value);

    /**
     * 批量写入 hash 字段。
     */
    void hashPutAll(String key, Map<String, String> entries);

    /**
     * 读取 hash 字段。
     */
    Optional<String> hashGet(String key, String hashKey);

    /**
     * 读取 hash 所有字段和值。
     */
    Map<Object, Object> hashEntries(String key);

    /**
     * 删除 hash 字段。
     */
    Long hashDelete(String key, String... hashKeys);

    /**
     * 对 hash 数值字段做增量操作。
     */
    Long hashIncrement(String key, String hashKey, long delta);

    /**
     * 判断 hash 字段是否存在。
     */
    boolean hashHasKey(String key, String hashKey);

    /**
     * 返回 hash 所有字段名。
     */
    Set<Object> hashKeys(String key);

    /**
     * 返回 hash 所有字段值。
     */
    List<Object> hashValues(String key);

    /**
     * 返回 hash 字段数量。
     */
    Long hashSize(String key);

    /**
     * 向 set 增加成员。
     */
    Long setAdd(String key, String... values);

    /**
     * 从 set 移除成员。
     */
    Long setRemove(String key, String... values);

    /**
     * 返回 set 所有成员。
     */
    Set<String> setMembers(String key);

    /**
     * 判断 set 成员是否存在。
     */
    boolean setContains(String key, String value);

    /**
     * 返回 set 成员数量。
     */
    Long setSize(String key);

    /**
     * 返回两个 set 的交集。
     */
    Set<String> setIntersect(String key, String otherKey);

    /**
     * 返回两个 set 的并集。
     */
    Set<String> setUnion(String key, String otherKey);

    /**
     * 返回 key 相对 otherKey 的差集。
     */
    Set<String> setDifference(String key, String otherKey);

    /**
     * 随机读取一个 set 成员，不移除。
     */
    String setRandomMember(String key);

    /**
     * 随机弹出一个 set 成员。
     */
    String setPop(String key);

    /**
     * 向 zset 增加成员分数。
     */
    Long zsetAdd(String key, String value, double score);

    /**
     * 批量向 zset 增加成员分数。
     */
    Long zsetAdd(String key, Map<String, Double> values);

    /**
     * 按排名正序读取 zset 成员。
     */
    Set<String> zsetRange(String key, long start, long end);

    /**
     * 按分数范围读取 zset 成员。
     */
    Set<String> zsetRangeByScore(String key, double min, double max);

    /**
     * 从 zset 移除成员。
     */
    Long zsetRemove(String key, String... values);

    /**
     * 读取 zset 成员分数。
     */
    Double zsetScore(String key, String value);

    /**
     * 返回 zset 成员数量。
     */
    Long zsetSize(String key);

    /**
     * 增加 zset 成员分数。
     */
    Double zsetIncrementScore(String key, String value, double delta);

    /**
     * 按排名倒序读取 zset 成员。
     */
    Set<String> zsetReverseRange(String key, long start, long end);

    /**
     * 按分数范围删除 zset 成员。
     */
    Long zsetRemoveRangeByScore(String key, double min, double max);

    /**
     * 按排名范围删除 zset 成员。
     */
    Long zsetRemoveRange(String key, long start, long end);

    /**
     * 读取 zset 成员正序排名。
     */
    Long zsetRank(String key, String value);

    /**
     * 读取 zset 成员倒序排名。
     */
    Long zsetReverseRank(String key, String value);

    /**
     * 统计指定分数范围内的 zset 成员数量。
     */
    Long zsetCount(String key, double min, double max);
}
