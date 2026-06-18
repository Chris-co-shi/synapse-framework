# synapse-cache 设计说明

## 1. 模块使命

`synapse-cache` 提供缓存、Redis 原子脚本、锁、限流和幂等的通用技术工具。它解决“如何可靠执行基础设施操作”，不决定“哪个业务对象如何缓存、锁多大粒度、失败后如何补偿”。

## 2. 边界

负责：

- Cache Client 与 key/spec/codec 契约。
- Caffeine L1 + Redis L2 默认实现。
- Redis 常用 String 数据结构封装。
- Lua 脚本执行端口。
- Redis 可重入锁。
- 滑动窗口限流。
- 幂等占位 Guard。

不负责：

- 业务缓存 key 和失效规则。
- 库存锁、订单幂等等业务语义。
- 缓存管理后台与预热平台。
- Redis Pub/Sub、可靠消息和分布式事务。
- 保存业务执行结果的幂等状态机。

## 3. 缓存分层设计

```text
CacheClient
  -> L1 LocalCacheStore (optional Caffeine)
  -> L2 RedisCacheStore
  -> CacheValueCodec
```

`getOrLoad` 的 single-flight 只在单 JVM 内避免重复 loader；它不是跨实例锁。

L1 是近端性能优化，不提供跨实例失效广播，因此强一致和频繁写入场景应禁用或使用很短 TTL。

## 4. 核心对象角色

### 4.1 `CacheKey` / `CacheKeyRef`

统一 key segment 格式，防止随意拼接，但 key 的业务粒度仍由消费方决定。

### 4.2 `CacheSpec`

描述 L1/L2 TTL、是否启用等加载策略，而不是全局业务缓存政策。

### 4.3 `CacheValueCodec`

负责序列化边界。加密、压缩、跨语言 schema 和版本兼容应通过替换 codec 实现。

### 4.4 `RedisScriptExecutor`

隐藏 Spring Data Redis 执行细节，Lua 脚本负责 Redis 侧原子性。

### 4.5 `RedisReentrantLock`

通过 owner 和重入计数表达可重入。当前没有自动续约和阻塞等待，调用方必须在 finally 使用相同 owner 释放。

### 4.6 `SlidingWindowRateLimiter`

只返回允许/拒绝技术决策；请求维度、阈值和拒绝后的降级由业务层决定。

### 4.7 `IdempotencyGuard`

只完成 key 首次占位，不保存业务响应，也不能表达 PROCESSING/SUCCESS/FAILED 的复杂状态。

## 5. 主链路

缓存：

```text
getOrLoad
  -> L1 hit: return
  -> L2 hit: populate L1 and return
  -> local single-flight loader
  -> encode and write L2/L1
```

锁/限流/幂等：

```text
business-defined key
  -> Redis Lua atomic operation
  -> technical decision/result
  -> business decides retry / reject / compensate
```

## 6. 失败与一致性边界

- Redis 不可用时是否 fail-open / fail-closed 不能由通用模块统一猜测。
- loader 返回 null 的缓存策略必须明确，避免穿透。
- L1 与 L2 短暂不一致是两级缓存天然风险。
- lock TTL 小于业务执行时间可能提前释放；当前无 watchdog。
- owner 生成不唯一会导致错误重入或误释放。
- 限流时间应尽量使用统一 Redis/服务时间策略。
- 幂等占位成功后业务失败，是否释放由业务语义决定。

## 7. 扩展原则

- 序列化/加密：替换 `CacheValueCodec`。
- 缓存拓扑：替换 `CacheClient` 或 Store。
- 新 Lua 功能：通过脚本和 executor 扩展，不在 Java 中拆成多条非原子命令。
- 生产级锁续约可作为单独 adapter/能力设计，不能假装当前锁已具备。
- 复杂业务幂等应由业务状态表或专门平台实现。

## 8. 源码阅读顺序

```text
CacheKey / CacheSpec
  -> CacheValueCodec
  -> LocalCacheStore / RedisCacheStore
  -> TieredCacheClient
  -> RedisLuaScript / RedisScriptExecutor
  -> RedisReentrantLock
  -> SlidingWindowRateLimiter
  -> RedisIdempotencyGuard
  -> SynapseCacheAutoConfiguration
  -> concurrency and Lua tests
```

## 9. 手写练习

1. 写 L1/L2 getOrLoad 流程并记录 loader 调用次数。
2. 模拟两个 JVM 说明 local single-flight 无法跨实例互斥。
3. 用相同 owner 连续加锁两次并释放两次。
4. 模拟锁 TTL 先到期，分析为什么不能宣称绝对安全。
5. 设计“请求占位”与“保存执行结果”两种幂等差异。

## 10. 修改检查清单

- 是否加入业务 key、库存或订单语义。
- 是否把 getOrLoad 描述成分布式锁。
- 是否把 L1 描述成跨实例一致。
- 是否忽略 lock owner、TTL 和无续约限制。
- 是否把 IdempotencyGuard 描述成完整业务幂等。
- Redis 失败策略是否被隐式固定。
- 新配置是否有 metadata 和边界说明。
