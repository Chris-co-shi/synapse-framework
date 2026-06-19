# synapse-cache 使用手册

## 1. 模块定位

`synapse-cache` 是 Synapse Framework 的缓存与 Redis 基础设施模块。

它提供通用缓存、L1/L2 两级缓存、Redis Lua、可重入锁、滑动窗口限流、幂等占位和 Redis 常用数据结构能力，不沉淀任何业务缓存规则。

当前核心能力：

- `CacheClient` 通用缓存客户端。
- `CacheKey` / `CacheKeyRef` 缓存 key 约定。
- `CacheSpec` 两级缓存规格。
- `CacheValueCodec` 缓存值编解码。
- L1 Caffeine 本地缓存。
- L2 Redis 字符串缓存。
- Redis 常用数据结构客户端。
- Redis Lua 脚本执行端口。
- Redis Lua 可重入锁。
- Redis Lua 滑动窗口限流器。
- Redis 幂等 Guard。

## 2. 适用场景

业务系统或平台系统在以下场景可以引入 `synapse-cache`：

- 需要统一缓存客户端。
- 需要 L1 本地缓存 + Redis L2 缓存。
- 需要 Redis Lua 原子脚本执行。
- 需要 Redis 可重入锁。
- 需要滑动窗口限流。
- 需要接口或操作幂等占位。
- 需要轻量操作 Redis list、hash、set、zset。

## 3. 不适用场景

`synapse-cache` 不适合承担以下职责：

- 业务缓存 key 设计。
- 用户权限缓存规则。
- 订单库存锁规则。
- 业务限流策略。
- 业务幂等语义。
- 缓存预热平台。
- 缓存管理后台。
- Redis 消息发布订阅。
- 可靠消息。
- 分布式事务。

这些应由业务系统或后续平台服务决定。

## 4. Maven 引入

推荐先引入 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.indigo.synapse</groupId>
            <artifactId>synapse-bom</artifactId>
            <version>${synapse.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

再引入 cache 模块：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-cache</artifactId>
</dependency>
```

业务系统仍需自行配置 Redis 连接，例如 Spring Boot 的 Redis 配置。

## 5. 核心能力

### 5.1 自动配置

核心类型：

```java
SynapseCacheAutoConfiguration
SynapseCacheProperties
```

自动注册：

```java
CacheSpec
CacheValueCodec
LocalCacheStore
RedisCacheStore
RedisDataStructureClient
RedisScriptExecutor
RedisReentrantLock
SlidingWindowRateLimiter
IdempotencyGuard
CacheClient
```

说明：

- 只有 `StringRedisTemplate` 存在时自动配置才生效。
- 所有核心 Bean 都可以由消费方自定义覆盖。
- L1 本地缓存可以关闭。

### 5.2 CacheClient

核心类型：

```java
CacheClient
TieredCacheClient
```

常用方法：

```java
Optional<T> get(CacheKeyRef key, Class<T> valueType);
void put(CacheKeyRef key, T value, Duration ttl);
void evict(CacheKeyRef key);
T getOrLoad(CacheKeyRef key, Class<T> valueType, Supplier<T> loader, CacheSpec cacheSpec);
```

默认实现：

```text
L1: Caffeine 本地缓存，可关闭
L2: Redis 字符串缓存
```

`getOrLoad` 只做单 JVM single-flight，不提供跨 JVM 加载互斥。跨 JVM 互斥应使用 `RedisReentrantLock` 或业务侧更高层控制。

### 5.3 CacheKey

核心类型：

```java
CacheKey
CacheKeyRef
```

默认格式：

```text
namespace:module:domain:purpose:parts...
```

示例：

```java
CacheKey key = CacheKey.withNamespace(
        "sample",
        "platform",
        "resource",
        "detail",
        "10001"
);
```

注意：

- 每个 segment 不能为空。
- 每个 segment 不能包含冒号。
- key 中业务语义由消费方决定。

### 5.4 CacheValueCodec

核心类型：

```java
CacheValueCodec
DefaultCacheValueCodec
```

默认使用 Jackson 编解码。

如需加密、压缩、跨语言兼容或版本兼容，应提供自定义 `CacheValueCodec`。

### 5.5 Redis 常用数据结构

核心类型：

```java
RedisDataStructureClient
StringRedisDataStructureClient
```

支持：

- list
- hash
- set
- zset

该客户端只暴露 String key/value，不表达业务 topic、可靠消息或补偿语义。

### 5.6 Redis Lua

核心类型：

```java
RedisLuaScript
RedisScriptExecutor
SpringDataRedisScriptExecutor
SynapseRedisScripts
```

内置脚本：

- 可重入加锁。
- 可重入解锁。
- 滑动窗口限流。

Lua 脚本用于保证 Redis 侧的原子性。

### 5.7 可重入锁

核心类型：

```java
RedisReentrantLock
LockAcquireResult
LockReleaseResult
```

用法：

```java
LockAcquireResult result = redisReentrantLock.acquire(
        "sample:lock:resource:10001",
        "instance-1-thread-1-request-1",
        Duration.ofSeconds(30)
);

if (result.acquired()) {
    try {
        // do work
    } finally {
        redisReentrantLock.release(
                "sample:lock:resource:10001",
                "instance-1-thread-1-request-1",
                Duration.ofSeconds(30)
        );
    }
}
```

注意：

- `owner` 必须稳定且唯一。
- 释放时必须使用同一个 owner。
- 当前实现不提供自动续约。
- 当前实现不提供阻塞等待。

### 5.8 滑动窗口限流

核心类型：

```java
SlidingWindowRateLimiter
RateLimitDecision
```

示例：

```java
RateLimitDecision decision = slidingWindowRateLimiter.allow(
        "sample:rate-limit:resource:10001",
        100,
        Duration.ofMinutes(1),
        System.currentTimeMillis()
);

if (!decision.allowed()) {
    // reject or degrade
}
```

注意：

- key 维度由消费方决定。
- 多实例部署建议使用统一时间源。
- 限流失败后的响应、降级或排队由业务系统决定。

### 5.9 幂等 Guard

核心类型：

```java
IdempotencyGuard
IdempotencyKeyBuilder
RedisIdempotencyGuard
```

示例：

```java
boolean first = idempotencyGuard.tryAcquire(
        "sample-operation",
        requestId,
        Duration.ofMinutes(10)
);

if (!first) {
    // duplicate request
}
```

说明：

- `tryAcquire` 成功表示首次占用。
- 已存在时返回 false。
- 该能力不保存业务执行结果。
- 业务失败后是否允许重试由消费方决定。

## 6. 快速使用

### 6.1 使用缓存

```java
CacheKey key = CacheKey.of("sample", "resource", "detail", "10001");

SampleValue value = cacheClient.getOrLoad(
        key,
        SampleValue.class,
        () -> loadFromDatabase("10001"),
        CacheSpec.defaults()
);
```

### 6.2 删除缓存

```java
cacheClient.evict(CacheKey.of("sample", "resource", "detail", "10001"));
```

### 6.3 关闭 L1 本地缓存

```yaml
synapse:
  cache:
    l1:
      enabled: false
```

### 6.4 修改默认 TTL

```yaml
synapse:
  cache:
    l1:
      expire-after-write: 5m
      maximum-size: 1000
    l2:
      ttl: 30m
```

## 7. 扩展方式

### 7.1 替换 CacheValueCodec

```java
@Bean
CacheValueCodec cacheValueCodec() {
    return new CustomCacheValueCodec();
}
```

### 7.2 替换 CacheClient

```java
@Bean
CacheClient cacheClient() {
    return new CustomCacheClient();
}
```

### 7.3 替换 RedisScriptExecutor

```java
@Bean
RedisScriptExecutor redisScriptExecutor() {
    return new CustomRedisScriptExecutor();
}
```

### 7.4 替换 IdempotencyGuard

```java
@Bean
IdempotencyGuard idempotencyGuard() {
    return new CustomIdempotencyGuard();
}
```

## 8. 配置项

配置前缀：

```yaml
synapse.cache
```

配置项：

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `l1.enabled` | `true` | 是否启用 L1 本地缓存 |
| `l1.expire-after-write` | `5m` | L1 默认写入过期时间 |
| `l1.maximum-size` | `1000` | L1 最大条目数 |
| `l2.ttl` | `30m` | L2 Redis 默认 TTL |

## 9. 边界与注意事项

### 9.1 缓存 key 由消费方负责

framework 不知道你的业务对象和一致性要求。key 命名、粒度、失效策略必须由业务系统决定。

### 9.2 L1 只做近端加速

L1 本地缓存不跨实例同步，适合读多写少或短 TTL 场景。强一致场景应谨慎启用。

### 9.3 getOrLoad 不是分布式锁

默认 `getOrLoad` 只在当前 JVM 内做 single-flight。多实例并发加载需要使用 Redis 锁或业务侧控制。

### 9.4 幂等 Guard 不保存业务结果

它只判断是否首次占用 key，不保存接口响应或业务执行状态。需要“相同请求返回相同结果”时，业务系统需要自己保存结果。

### 9.5 可重入锁需要正确 owner

owner 必须在 acquire 和 release 中保持一致，否则释放会失败。

## 10. 常见问题

### Q1：为什么 cache 模块依赖 Redis？

当前 cache 模块定位为缓存基础设施，Redis 是 L2 缓存、Lua 锁、限流和幂等的运行基础。

### Q2：可以不用 L1 吗？

可以，通过 `synapse.cache.l1.enabled=false` 关闭。

### Q3：CacheClient 会自动缓存数据库查询吗？

不会。framework 不接管业务 Repository。业务系统需要显式调用 `getOrLoad`。

### Q4：RedisDataStructureClient 可以做消息队列吗？

不建议。它只提供轻量 Redis 数据结构操作，不表达可靠消息、顺序消费、重试、死信等语义。

### Q5：如何处理缓存穿透、击穿、雪崩？

当前只提供基础工具。具体策略，例如空值缓存、随机 TTL、热点 key 保护、预热机制，应由业务系统或平台能力设计。

## 11. Configuration Metadata

`synapse-cache` 发布 jar 必须包含 `META-INF/spring-configuration-metadata.json`，覆盖 `synapse.cache.l1.*` 和 `synapse.cache.l2.*`。新增配置项时必须补充字段 Javadoc，并运行 metadata 测试。
