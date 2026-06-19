# Phase 3 Core Contracts

> 历史说明：本文保留 core 契约演进背景，其中模块路径应按当前聚合结构和
> `synapse-messaging` 名称理解。

本文档冻结第三阶段需要维护的核心技术契约。后续 TASK-302 至 TASK-306 的实现和测试必须以此为基线。

## 1. 通用原则

- 当前事实优先于历史计划。
- 缺少上下文时不伪造 actor。
- 显式值优先于自动补齐值。
- 进入 scope 的组件必须负责在结束时恢复旧值。
- ThreadLocal 不得作为 WebFlux 的主传播通道。
- Framework 不判定业务权限、业务缓存语义或业务审计规则。

## 2. OperationContext 契约

### 2.1 actor

`actor` 表示当前实际执行操作的主体。可表示 USER、SERVICE、JOB、MESSAGE_CONSUMER、SYSTEM、ANONYMOUS 或 UNKNOWN 等技术主体。

约束：

- 从外部 carrier 恢复时，actor type 和 actor id 必须同时有效。
- 缺少关键字段时不恢复不完整 actor。
- 不因缺少 actor 自动创建 system actor。

### 2.2 initiator

`initiator` 表示链路最初发起者。服务调用、异步处理和 MQ 消费可以改变 actor，但不得无理由覆盖已有 initiator。

### 2.3 source

`source` 表示当前入口和运行来源，至少应能区分 HTTP、Feign、MQ、Job、Async 等技术来源。source 不是业务渠道字典。

### 2.4 traceId 与 requestId

- traceId 用于跨服务链路关联。
- requestId 用于一次入口请求或一次消息处理实例标识。
- Web 层可以生成缺失或非法的 traceId/requestId。
- 下游传播必须优先保留已有合法值。

### 2.5 scope

```text
previous context
  -> enter new scope
  -> execute
  -> close scope
  -> restore previous context
```

无论正常返回还是抛出异常，都必须恢复旧上下文。重复 close 不得造成上下文污染。

## 3. HTTP WebMVC 入站契约

```text
HTTP Request
  -> exception bridge
  -> trace/request context initialization
  -> OperationContext header decode
  -> OAuth2 Resource Server authentication
  -> SecurityContext and OperationContext adaptation
  -> DispatcherServlet
  -> response
  -> reverse-order cleanup
```

约束：

- Filter 异常必须可以转换为统一 JSON 响应。
- traceId 响应头、MDC、Result.traceId 应保持一致。
- Bearer Token 的可信性由 OAuth2 Resource Server 适配模块处理；webmvc 只做技术 carrier 解码。
- 请求结束必须清理 TraceContext、RequestContext、SecurityContext 和当前请求建立的 OperationContext scope。
- Filter 顺序必须通过测试而不是文档约定单独保证。

## 4. HTTP WebFlux 入站契约

```text
WebFlux Request
  -> traceId/requestId resolve
  -> OperationContext header decode
  -> Reactor Context
  -> handler chain
  -> unified error response
```

约束：

- Reactor Context 是主传播通道。
- 不依赖 Servlet API。
- 不依赖请求线程固定不变。
- complete、error、cancel 路径均不得泄漏上下文。
- Result 字段、错误码语义、时间格式应与 WebMVC 保持兼容。

## 5. Security 契约

```text
verified Bearer Token
  -> JWT validation
  -> AuthenticatedPrincipal
  -> SecurityContext
  -> OperationActor(USER)
  -> OperationContext
```

约束：

- roles 和 permissions 是当前请求快照，不是权限数据源。
- PermissionChecker 只检查当前快照或消费方实现，不查询用户表。
- `@RequirePermission` 是适配入口，不是唯一安全边界。
- MQ、Job、Async 等非 AOP 入口应支持显式权限检查。
- SecurityContext.clear 必须恢复设置主体前的 OperationContext，而不是直接破坏外层 Job/Async scope。

## 6. Data 审计填充契约

```text
OperationContextProvider
  -> SynapseAuditorProvider
  -> MetaObjectHandler
  -> createdBy / updatedBy / tenantId
```

约束：

- insert 只填充未显式赋值字段。
- update 必须刷新 updatedAt；存在 actor 时刷新 updatedBy。
- 缺少 actor 时不写固定 system。
- data 不读取 SecurityContext。
- tenantId 只是承载位，不代表已经实现多租户 SQL 隔离。
- Entity、Mapper、Repository、migration 和数据源配置由消费方拥有。

## 7. Cache 契约

### 7.1 CacheClient

- L1/L2 命中规则必须明确。
- 反序列化失败不得静默返回伪命中。
- loader 返回 null 的行为必须固定并测试。
- put/evict 部分失败时的返回或异常语义必须固定。

### 7.2 RedisReentrantLock

- owner 必须由调用方提供且在 acquire/release 期间稳定。
- 只有同一 owner 可以释放。
- 当前无自动续约、无阻塞等待，不得在文档中暗示存在。
- 非法 lease time 必须拒绝。

### 7.3 SlidingWindowRateLimiter

- Framework 只返回技术判定结果。
- key 维度、拒绝响应、降级和排队策略由消费方决定。
- 时间源和窗口边界必须可测试。

### 7.4 IdempotencyGuard

- `tryAcquire` 只表示技术占位成功。
- 不等价于业务执行成功。
- 不保存业务响应。
- 失败后是否释放、重试或补偿由上层策略决定。

## 8. Audit 契约

```text
explicit event fields
  -> AuditContext
  -> OperationContextProvider
  -> requireRecordable
  -> AuditLogPort
```

约束：

- 显式字段优先，自动补齐不得覆盖调用方输入。
- 最终缺少 subject 或 traceId 时拒绝记录。
- 不自动把 UNKNOWN 或缺失主体写成 system。
- 显式 SystemOperationActor 可以被识别，但必须保留可追溯 id/name/source。
- AuditLogPort 只定义输出端口，不保证落库、可靠投递或查询能力。
- 多端口失败策略必须显式，不得靠偶然调用顺序形成行为。

## 9. 异步与线程池契约

```text
submit thread context
  -> snapshot
  -> wrapped task
  -> worker restore
  -> execute
  -> worker previous context restore
```

必须测试：

- 无上下文提交。
- 有上下文提交。
- worker 已存在旧上下文。
- task 正常完成。
- task 抛异常。
- Runnable 与 Callable。
- 连续复用同一 worker thread。

## 10. MQ 与 Job 兼容契约

虽然 MQ 和 Job 不是第三阶段目标模块，但 core 契约变更不得破坏：

- `synapse-messaging` 对 OperationContextSnapshotCodec 的复用。
- MQ header 小写 key 约定。
- Job 入口显式 actor/source 策略。
- Feign/HTTP carrier 已有字段兼容性。
