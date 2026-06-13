# Redis 数据结构与 Pub/Sub 引用操作文档

本文档说明 `synapse-cache` 提供的 Redis 字符串结构能力。框架只提供通用技术端口，不定义业务 key 或业务数据结构语义。

## 1. 可注入 Bean

启用 Spring Data Redis 后，自动配置默认提供：

```java
RedisDataStructureClient redisDataStructureClient;
CacheClient cacheClient;
RedisReentrantLock redisReentrantLock;
SlidingWindowRateLimiter slidingWindowRateLimiter;
IdempotencyGuard idempotencyGuard;
```

消费方可声明同类型 Bean 覆盖默认实现。

## 2. Key 与 Channel 命名

推荐 key 格式：

```text
synapse:<module>:<domain>:<purpose>:<parts...>
```

推荐 channel 格式：

```text
synapse:<module>:<domain>:<event>
```

要求：

- 不把用户、角色、订单等业务模型沉入框架模块。
- key/channel 由消费方按业务维度决定。
- key 分段中避免使用冒号，优先通过 `CacheKey` 统一构造缓存 key。

## 3. List 操作

适用场景：轻量队列、最近记录、固定长度列表。可靠队列和消费确认不应使用 List 直接替代 MQ。

```java
redisDataStructureClient.listRightPush("synapse:demo:list:recent", "a", "b");
redisDataStructureClient.listLeftPush("synapse:demo:list:recent", "c");
redisDataStructureClient.listRange("synapse:demo:list:recent", 0, -1);
redisDataStructureClient.listSet("synapse:demo:list:recent", 0, "head");
redisDataStructureClient.listRightPush("synapse:demo:list:recent", "head", "after-head");
redisDataStructureClient.listTrim("synapse:demo:list:recent", 0, 99);
redisDataStructureClient.listLeftPop("synapse:demo:list:recent");
```

## 4. Hash 操作

适用场景：同一个 Redis key 下保存多个字符串字段，例如技术状态、轻量配置片段。

```java
redisDataStructureClient.hashPut("synapse:demo:hash:state", "status", "ready");
redisDataStructureClient.hashPutAll("synapse:demo:hash:state", Map.of("version", "1"));
redisDataStructureClient.hashGet("synapse:demo:hash:state", "status");
redisDataStructureClient.hashHasKey("synapse:demo:hash:state", "version");
redisDataStructureClient.hashKeys("synapse:demo:hash:state");
redisDataStructureClient.hashValues("synapse:demo:hash:state");
redisDataStructureClient.hashSize("synapse:demo:hash:state");
redisDataStructureClient.hashDelete("synapse:demo:hash:state", "status");
```

## 5. Set 操作

适用场景：唯一成员集合、标签集合、在线实例集合。集合关系计算只适合有限大小集合，避免大 key 阻塞 Redis。

```java
redisDataStructureClient.setAdd("synapse:demo:set:a", "x", "y");
redisDataStructureClient.setAdd("synapse:demo:set:b", "y", "z");
redisDataStructureClient.setMembers("synapse:demo:set:a");
redisDataStructureClient.setContains("synapse:demo:set:a", "x");
redisDataStructureClient.setIntersect("synapse:demo:set:a", "synapse:demo:set:b");
redisDataStructureClient.setUnion("synapse:demo:set:a", "synapse:demo:set:b");
redisDataStructureClient.setDifference("synapse:demo:set:a", "synapse:demo:set:b");
redisDataStructureClient.setRandomMember("synapse:demo:set:a");
redisDataStructureClient.setPop("synapse:demo:set:a");
```

## 6. ZSet 操作

适用场景：排行榜、按时间排序的窗口、带权重集合。限流模块内部也使用 ZSET 表达滑动窗口。

```java
redisDataStructureClient.zsetAdd("synapse:demo:zset:rank", "a", 1.0);
redisDataStructureClient.zsetAdd("synapse:demo:zset:rank", Map.of("b", 2.0, "c", 3.0));
redisDataStructureClient.zsetRange("synapse:demo:zset:rank", 0, -1);
redisDataStructureClient.zsetReverseRange("synapse:demo:zset:rank", 0, -1);
redisDataStructureClient.zsetRangeByScore("synapse:demo:zset:rank", 1.0, 3.0);
redisDataStructureClient.zsetRank("synapse:demo:zset:rank", "b");
redisDataStructureClient.zsetReverseRank("synapse:demo:zset:rank", "b");
redisDataStructureClient.zsetCount("synapse:demo:zset:rank", 1.0, 2.0);
redisDataStructureClient.zsetIncrementScore("synapse:demo:zset:rank", "a", 1.0);
redisDataStructureClient.zsetRemoveRangeByScore("synapse:demo:zset:rank", 0.0, 1.0);
redisDataStructureClient.zsetRemoveRange("synapse:demo:zset:rank", 0, 0);
```

## 7. 发布订阅边界

Redis Pub/Sub、广播和消息推送能力属于 `synapse-message`。`synapse-cache` 不注册消息发布订阅 Bean，只保留缓存、Redis 数据结构、锁、限流和幂等能力。

需要 Redis Pub/Sub 在线广播时，请参考 `docs/message/redis-pubsub-usage.md`。

## 8. TTL 与一致性边界

- `CacheClient.put` 写入 L1 时会把 L1 TTL 截断到不超过调用方 TTL。
- `CacheClient.getOrLoad` 写入 L1 时会把 L1 TTL 截断到不超过 L2 TTL。
- Redis 命中后回填 L1 时，如能读取 Redis 剩余 TTL，会使用 Redis 剩余 TTL 与默认 L1 TTL 的较小值。
- `TieredCacheClient` 的 single-flight 只在当前 JVM 内生效，跨 JVM 加载互斥应使用 Redis 锁或消费方并发控制。

## 9. 禁止事项

- 禁止在 `synapse-cache` 中定义业务 key 枚举、业务 channel、业务消息体。
- 禁止把大集合、大列表作为无边界结构长期堆积。
- 禁止为了复用结构客户端绕过消费方自身权限、租户和审计边界。
