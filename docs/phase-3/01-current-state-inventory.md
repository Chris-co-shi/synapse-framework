# Phase 3 Current State Inventory

本文档记录第三阶段开始时的代码事实、依赖事实和已知缺口。内容以 root `pom.xml`、目标模块 POM、模块手册及当前实现为依据。

## 1. 总体状态

七个目标模块均已进入 root reactor：

```text
synapse-core
synapse-webmvc
synapse-webflux
synapse-data
synapse-cache
synapse-security
synapse-audit
```

当前不是空模块建设阶段。第三阶段应采用“审计 -> 契约冻结 -> 定向强化 -> 跨模块验证”的方式推进。

## 2. 模块现状矩阵

### 2.1 synapse-core

| 维度 | 当前事实 |
| --- | --- |
| 定位 | 最底层纯 Java 核心契约模块 |
| 公开能力 | ErrorCode、CommonErrorCode、SynapseException、OperationContext、OperationActor、OperationSource、Holder、Scope、Snapshot、Carrier、Codec、Propagator、异步包装器、IdGenerator |
| 自动配置 | 无 |
| Framework 依赖 | 无其他 `synapse-*` 依赖 |
| 主要外部依赖 | 生产代码无 Spring Web、Security、Redis、MyBatis 或 MQ SDK |
| 当前优势 | OperationContext 已覆盖快照、carrier 编解码、线程包装和显式 system actor 工厂 |
| 已知缺口 | 需要冻结 actor、initiator、source、traceId、requestId 的必填与缺失语义；需要验证嵌套 scope、异常路径和线程复用清理 |

### 2.2 synapse-webmvc

| 维度 | 当前事实 |
| --- | --- |
| 定位 | Servlet MVC 技术支撑 |
| 公开能力 | 复用 web-core Result/ErrorHttpStatusResolver/Jackson 定制；GlobalExceptionHandler、Filter 异常桥接、TraceContext、RequestContext、MvcTraceFilter、OpenAPI 可见性策略 |
| 自动配置 | 已提供 WebMVC 自动配置，核心 Bean 支持消费方覆盖 |
| Framework 依赖 | `synapse-core` |
| 主要外部依赖 | Jackson、Spring Web、Spring WebMVC、Servlet API、Validation |
| 当前优势 | MVC 异常与 Filter 异常已区分；traceId 可进入 Header、MDC、请求上下文和响应体 |
| 已知缺口 | 需要持续验证 TraceContext、RequestContext 的建立和清理顺序；认证 OperationContext 属于 Resource Server 适配层 |

### 2.3 synapse-webflux

| 维度 | 当前事实 |
| --- | --- |
| 定位 | WebFlux 技术支撑，不是 Gateway 服务 |
| 公开能力 | 复用 web-core Result/ErrorHttpStatusResolver/Jackson 定制；ReactiveRequestContext、SynapseWebFluxContextFilter、异常处理器 |
| 自动配置 | `SynapseWebFluxAutoConfiguration` |
| Framework 依赖 | `synapse-core` |
| 主要外部依赖 | Jackson、Spring Web、Spring WebFlux、Reactor |
| 当前优势 | POM 未引入 MVC 或 Servlet；Reactor Context 是请求上下文主通道 |
| 已知缺口 | Result、状态解析和 Jackson 默认规则已统一到 web-core；仍需持续验证 cancel、error、empty publisher 等路径的上下文清理和响应行为 |

### 2.4 synapse-security

| 维度 | 当前事实 |
| --- | --- |
| 定位 | Web 无关安全主体、安全上下文和权限检查基础模块 |
| 公开能力 | AuthenticatedPrincipal、AuthenticatedUser、AuthenticatedClient、CurrentPrincipalContext、PermissionChecker、`@RequirePermission`、Security 到 OperationContext 适配、PasswordEncoder |
| 自动配置 | 安全基础自动配置；不创建 SecurityFilterChain |
| Framework 依赖 | `synapse-core` |
| 主要外部依赖 | Spring Boot AutoConfigure、Spring AOP、spring-security-crypto |
| 当前优势 | data/audit/mq 无需依赖 security；权限检查既有显式入口也有注解适配 |
| 已知缺口 | CurrentPrincipalContext 同时维护当前主体 ThreadLocal 与 OperationContextScope，需要重点验证嵌套 set/clear、异常清理和已有 OperationContext 恢复 |

### 2.5 synapse-data

| 维度 | 当前事实 |
| --- | --- |
| 定位 | MyBatis-Plus 数据层技术支撑和审计字段自动填充 |
| 公开能力 | 分页、乐观锁、IdentifierGenerator、OperationContextProvider、SynapseAuditorProvider、SynapseMetaObjectHandler |
| 自动配置 | `SynapseDataAutoConfiguration` |
| Framework 依赖 | `synapse-core`，不依赖 security |
| 主要外部依赖 | MyBatis-Plus、dynamic-datasource、Flyway core、Flyway PostgreSQL、Spring Boot AutoConfigure |
| 当前优势 | created/updated 字段通过 OperationContext 获取当前操作人，不默认写死 system |
| 已知缺口 | 文档声明不负责连接池和 migration，但生产 POM 直接引入 dynamic-datasource 与 Flyway；需要确认是否存在实际使用，若无则移除或调整为可选依赖；ID 示例仍使用 Long，与项目 String/varchar(19) 规范存在表达冲突；字段名约定和类型兼容需要测试固化 |

### 2.6 synapse-cache

| 维度 | 当前事实 |
| --- | --- |
| 定位 | 缓存、Redis 数据结构、Lua、锁、限流和幂等基础设施 |
| 公开能力 | CacheClient、CacheKey、CacheSpec、CacheValueCodec、L1/L2 cache、RedisDataStructureClient、RedisScriptExecutor、RedisReentrantLock、SlidingWindowRateLimiter、IdempotencyGuard |
| 自动配置 | `SynapseCacheAutoConfiguration`，依赖 StringRedisTemplate 条件生效 |
| Framework 依赖 | `synapse-core` |
| 主要外部依赖 | Jackson、Caffeine、Spring Data Redis |
| 当前优势 | 业务 key、业务限流和业务幂等语义保留给消费方；Bean 支持覆盖 |
| 已知缺口 | 可重入锁没有自动续约和阻塞等待；幂等 Guard 只保存占位不保存业务结果；需要冻结 Redis 异常、编解码异常、TTL 非法值和部分失败的行为；Testcontainers 依赖是否提供真实 Redis 集成测试需要核对 |

### 2.7 synapse-audit

| 维度 | 当前事实 |
| --- | --- |
| 定位 | 审计事件契约、上下文补齐和输出端口 |
| 公开能力 | AuditEvent、AuditSubject、AuditTarget、AuditOutcome、AuditContext、AuditEventContextEnricher、AuditRecorder、AuditLogPort、CompositeAuditLogPort、NoopAuditLogPort |
| 自动配置 | 已提供审计自动配置 |
| Framework 依赖 | `synapse-core`，不依赖 data/cache/mq |
| 主要外部依赖 | Spring Boot AutoConfigure、Spring Context |
| 当前优势 | subject/traceId 不可追溯时拒绝记录；不默认伪造 system；输出方式由消费方 Port 决定 |
| 已知缺口 | CompositeAuditLogPort 当前串行调用且不做失败隔离；Noop 默认实现可能让调用方误以为已经持久化；SYSTEM/UNKNOWN actor 不自动成为 subject 的规则需要与显式 SystemOperationActorFactory 对齐 |

## 3. 跨模块现状

### 3.1 已形成的主链路

```text
HTTP Header
  -> WebMVC/WebFlux technical context
  -> Security authenticated user
  -> OperationContext
  -> Data auditor fields
  -> Audit event enrichment
```

```text
OperationContext snapshot
  -> Async wrapper / HTTP carrier / MQ carrier
  -> restore
  -> execute
  -> restore previous context
```

### 3.2 当前关键一致性问题

1. WebMVC 与 WebFlux 仍保留各自技术栈异常工厂，但共享 Result、状态解析器和 Jackson Builder 定制。
2. WebMVC 同时存在 TraceContext、RequestContext 和 OperationContext，需要冻结建立、覆盖和清理顺序。
3. CurrentPrincipalContext 管理 OperationContextScope，嵌套安全上下文的行为需要明确。
4. Data 模块生产依赖与文档边界不完全一致。
5. Cache 的失败语义与可观测性尚未形成统一契约。
6. Audit 的多端口失败策略和默认 Noop 行为需要明确。
7. 模块手册需要持续避免把历史阶段表述写成当前事实。

## 4. 第三阶段处理原则

- TASK-301 只记录事实、冻结边界和生成任务输入，不修改公共 API。
- 后续任务先补测试再改实现，避免依靠文档猜测行为。
- 删除生产依赖前必须确认没有代码使用和消费方兼容风险。
- WebMVC/WebFlux 若抽取公共响应契约，必须避免引入新的 Web 技术栈耦合。
- 所有上下文改动都必须覆盖正常、异常、嵌套和线程复用路径。
