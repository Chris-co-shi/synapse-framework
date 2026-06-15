# synapse-core Skill

## 1. 模块职责

`synapse-core` 是 Synapse Framework 的纯 Java 底层契约模块，只承载所有技术模块共同依赖的基础抽象。

允许内容：

- 通用错误码和异常模型。
- ID 生成抽象。
- `OperationContext`、`OperationActor`、`OperationSource`。
- `OperationContextHolder`、`OperationContextScope`、`OperationContextSnapshot`。
- 纯 Java carrier、codec、propagator。
- 显式 system actor 工厂。

禁止内容：

- Spring / Servlet / WebFlux / Feign / MQ SDK / MyBatis / Redis 依赖。
- Controller、启动类、业务 Service。
- 用户、角色、菜单、组织、配置中心、消息中心、文件中心等业务模型。
- 业务 Entity / Mapper / Repository / migration。
- starter、demo、example、sample application。

## 2. OperationContext 规则

- `OperationContext` 是技术上下文，不是业务用户模型。
- `actor` 表示当前执行主体，`initiator` 表示最初发起主体，二者允许不同。
- 缺少上下文时不得自动创建 `SYSTEM`、`UNKNOWN` 或默认 `USER`。
- 需要 system actor 时必须通过 `SystemOperationActorFactory.system(id, name)` 显式创建。
- `OperationContextHolder.scope(...)` 和 `restore(...)` 必须配合 try-with-resources 使用。
- 线程池、异步任务、消息消费入口必须显式快照和恢复，避免 ThreadLocal 污染。

## 3. 统一传播模式

跨边界传播优先使用：

```text
OperationContextSnapshot
  -> OperationContextSnapshotCodec
  -> OperationContextSnapshotCarrier
  -> 协议模块做 key 映射或载体适配
```

约束：

- `OperationContextSnapshotCodec` 只能处理 `Map<String, String>`。
- Header / Message key 使用 `OperationContextPropagationKeys` 作为统一语义来源。
- WebMVC、WebFlux、Cloud、MQ 不应复制一套不同的 actor 兜底规则。
- 缺少 actor type 或 actor id 时 decode 返回 empty。
- roles、permissions、raw token、password、credential、业务数据不得进入 carrier。

## 4. 异步执行模式

提交异步任务前使用：

```java
Runnable task = OperationContextExecutor.wrap(delegate);
Callable<T> task = OperationContextExecutor.wrap(delegate);
```

包装器必须保证：

- 执行前恢复捕获时的 snapshot。
- 执行后恢复线程原有上下文。
- delegate 抛异常时仍然恢复上下文。

## 5. 测试要求

修改 core 上下文能力时至少覆盖：

- holder set / current / clear。
- nested scope 恢复顺序。
- 异常后 scope 恢复。
- snapshot encode / decode。
- 缺少 actor 时不恢复上下文。
- system actor 必须显式创建。
- Runnable / Callable 包装传播。
- 线程池执行后不污染复用线程。

## 6. 执行前必读

- `AGENTS.md`
- `docs/phase-2/00-framework-boundary.md`
- `docs/phase-2/03-boundary-checklist.md`
- `docs/modules/synapse-core.md`

## 7. 常见错误

- 在 core 中加入 Spring 注解或自动配置。
- 为了方便在缺少 actor 时默认创建 system actor。
- 让 HTTP Header、MQ Header 或 Feign 类型进入 core。
- 在线程池任务中直接 `OperationContextHolder.set(...)` 但不恢复。
- 把 security 的认证主体或平台 IAM 模型放进 core。
