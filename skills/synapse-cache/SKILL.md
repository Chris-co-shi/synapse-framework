---
name: synapse-cache
description: Synapse Cache 基础能力最佳实践。Use when Codex implements or reviews synapse-cache code involving unified cache abstractions, Redis, Caffeine local cache, Spring Data Redis, Lettuce, Lua scripts, cache key conventions, reentrant distributed locks, sliding-window rate limiting, or Redis Testcontainers.
---

# Synapse Cache

## 必读

- `AGENTS.md`
- `docs/01-architecture.md`
- `docs/02-module-boundary.md`
- `docs/07-test-rules.md`
- `docs/10-technical-foundation-baseline.md`

## 职责和边界

- 提供统一缓存抽象。
- 默认支持 `L1` 本地缓存 + `L2` Redis 缓存。
- 提供 Redis 数据结构基础客户端（list/hash/set/zset）。
- 提供 Redis Lua 脚本执行封装。
- 提供可重入 Redis 分布式锁。
- 提供 Redis Lua 滑动窗口限流。
- 提供缓存 key 命名规范。
- 不做 Redlock。
- 不把业务级缓存策略写死进框架。

## 推荐包结构

```text
com.indigo.synapse.cache
├── key
├── local
├── redis
├── lock
├── ratelimit
├── script
└── autoconfigure
```

## 标准实现模式

- 统一缓存入口优先使用 `CacheClient`。
- `CacheKey.of(module, domain, purpose, parts...)` 使用默认 `synapse` namespace。
- 自定义 namespace 使用 `CacheKey.withNamespace(...)`。
- key segment 不允许为空，不允许包含 `:`.
- 默认缓存策略：
  - `l1.enabled=true`
  - `l1.maximumSize=1000`
  - `l1.expireAfterWrite=5m`
  - `l2.ttl=30m`
  - 默认不缓存空值
- `TieredCacheClient` 负责 L1/L2 编排：
  - 先查 L1
  - miss 再查 L2
  - 命中后回填 L1
  - 写入/失效同步两层
  - `getOrLoad` 的 loader 返回 `null` 表示没有可缓存值：不写入 L1/L2，直接返回 `null`
  - `put(key, null, ttl)` 表示主动失效该 key，必须同步清理 L1/L2
- Redis 通用能力只做缓存基础封装，不扩成完整 Redis SDK。
- Redis 数据结构客户端只做基础结构操作，不扩成完整 Redis SDK。
- Redis Lua 业务能力统一通过 `RedisScriptExecutor` 执行。
- 锁必须使用唯一 owner 标识，释放时必须校验 owner。
- 限流第一版使用 ZSet + Lua 滑动窗口。

## 允许技术和禁止事项

允许：

- Spring Data Redis。
- Lettuce。
- Caffeine。
- Redis Lua。
- Testcontainers Redis 集成测试。
- `StringRedisTemplate` 作为脚本执行底层适配。

禁止：

- Redlock。
- 普通 `delete` 释放锁。
- 未校验 owner 的释放脚本。
- 在业务类中散落 Lua 字符串。
- 在框架模块中写具体业务缓存策略。
- 在没有 TTL 的限流/锁 key 上写入 Redis。
- 用缓存 store 代替 Redis 数据结构客户端。

## 测试要求

- 纯 Java API 必须覆盖 key 校验、默认值、锁返回值转换、限流返回值转换。
- 缓存抽象必须覆盖 L1 命中、L2 回填、失效同步、`getOrLoad` 回源。
- 缓存抽象必须覆盖 `getOrLoad` loader 返回 `null` 不缓存且返回 `null`。
- 缓存抽象必须覆盖 `put(key, null, ttl)` 清理既有缓存值。
- Redis 数据结构客户端必须覆盖 list/hash/set/zset 的基础操作与参数校验。
- 锁测试必须覆盖首次获得、重入、竞争失败、完全释放、部分释放、owner 不匹配、非法参数。
- 限流测试必须覆盖允许、拒绝、参数非法、脚本返回值非法。
- 使用 Redis Testcontainers 验证锁和限流。
- 自动配置测试必须验证 `CacheClient`、`RedisScriptExecutor`、`RedisReentrantLock`、`SlidingWindowRateLimiter` 可装配。

## 常见错误

- 使用 `ThreadLocal` 误承载跨线程缓存语义。
- 在业务类中拼接 Redis Lua。
- 没有默认配置，导致调用方不配置时无法启动。
- 缓存 key 设计不包含模块、域和用途。
- 忘记 `AutoConfiguration.imports`。
- 把 `RedisCacheStore` 当成完整 Redis SDK 使用。
- loader 返回 `null` 时写入空值哨兵或字符串 `"null"`，导致后续读取语义混乱。
- 只清理 L2 不清理 L1，导致 null/失效后仍读到旧值。

## 示例任务拆分

- 实现 `CacheClient` 与 `CacheSpec`。
- 实现 Caffeine 本地缓存适配。
- 实现 Redis 缓存适配。
- 实现 Redis 数据结构客户端。
- 实现双层缓存编排。
- 补齐 null 缓存语义测试。
- 实现 Cache 自动配置并验证默认值。
- 使用 Redis Testcontainers 验证 Lua 脚本真实执行语义。
