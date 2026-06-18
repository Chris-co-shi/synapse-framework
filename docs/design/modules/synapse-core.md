# synapse-core 设计说明

## 1. 模块使命

`synapse-core` 是所有 Framework 模块共享的最底层技术语言。它提供错误语义、操作上下文、上下文快照/传播和通用 ID 抽象，但不绑定 Web、Security、数据库、Redis 或 MQ SDK。

## 2. 为什么需要独立 core

如果 data、audit、mq、security 各自定义“当前用户、traceId、来源”，不同入口会产生不兼容上下文。core 通过 `OperationContext` 建立统一操作链路语义，使 HTTP、MQ、Task、Async 和内部调用都能共享同一模型。

## 3. 边界

负责：

- `ErrorCode` 与通用异常。
- `OperationActor`、`OperationSource`、`OperationContext`。
- Holder、Scope、Snapshot、Provider、Codec、Propagator。
- 异步任务上下文包装。
- 通用 `IdGenerator`。

不负责：

- 登录用户模型与权限。
- HTTP 状态码和统一响应。
- JWT、Bearer Token。
- 数据层自动填充。
- MQ、Feign 或 Servlet Header 的具体适配。
- 缺失上下文时自动创建 system actor。

## 4. 依赖方向

```text
core
  <- security
  <- webmvc / webflux / cloud
  <- data / audit / mq
```

core 不依赖任何其他 `synapse-*` 模块。上层模块只能把自己的对象适配到 core，不能把上层概念反向塞入 core。

## 5. 核心对象角色

### 5.1 `OperationContext`

表达一次操作的技术事实：

- 当前 actor：现在是谁在执行。
- initiator：最初是谁发起。
- source：从什么入口和实例进入。
- traceId / requestId：如何追踪。
- occurredAt：上下文建立时刻。
- attributes：小规模技术扩展信息。

它不是用户资料，也不是权限快照。

### 5.2 `OperationContextHolder`

ThreadLocal 容器，适用于同步线程执行模型。只保存当前线程上下文，不自动传播到新线程。

### 5.3 `OperationContextScope`

保存进入 scope 前的旧上下文，在关闭时恢复。嵌套调用可以安全进入临时上下文，而不是简单 clear 掉调用方上下文。

### 5.4 Snapshot / Codec / Propagator

```text
OperationContext
  -> Snapshot
  -> Carrier(Map<String,String>)
  -> HTTP / MQ adapter
  -> decode
  -> restore scope
```

core 只定义纯字符串 carrier；HTTP Header、MQ Header 的命名映射属于上层适配模块。

### 5.5 `OperationContextProvider`

读取端口用于避免下游模块直接绑定静态 Holder，便于测试和替换上下文来源。

## 6. 主链路

```text
HTTP / MQ / Task / Async entry
  -> create or decode OperationContextSnapshot
  -> OperationContextHolder.scope / restore
  -> data / audit / mq read through OperationContextProvider
  -> scope.close
  -> previous context restored
```

## 7. 生命周期与失败边界

- ThreadLocal 必须在执行结束时恢复或清理。
- 异步任务必须在提交前捕获 snapshot，在执行线程中恢复。
- 缺少 actor type 或 actor id 时，codec 不恢复伪造上下文。
- 缺少上下文不是自动写 `system` 的理由。
- Bearer Token、密码和权限集合不得进入 OperationContext。

## 8. 扩展原则

- 自定义上下文来源：实现 `OperationContextProvider`。
- 自定义 ID：实现 `IdGenerator`。
- 系统任务：入口显式使用 `SystemOperationActorFactory`。
- 新协议传播：在协议模块适配 core carrier，不复制另一套上下文规则。

## 9. 源码阅读顺序

```text
OperationActor / OperationSource
  -> OperationContext
  -> OperationContextHolder
  -> OperationContextScope
  -> OperationContextSnapshot
  -> OperationContextSnapshotCodec
  -> OperationContextProvider
  -> ContextAwareRunnable / Callable
  -> OperationContextExecutor
```

## 10. 手写练习

1. 手写一个支持嵌套恢复的 ThreadLocal Scope。
2. 在主线程建立 USER context。
3. 进入临时 SERVICE context。
4. 关闭后验证恢复 USER。
5. 包装线程池任务，验证执行后没有污染线程。

## 11. 修改检查清单

- 是否引入了 Web、Security、Redis、MyBatis 或 MQ SDK。
- 是否把用户、角色、菜单等业务模型加入 core。
- 是否在上下文缺失时隐式创建 system。
- 是否丢失嵌套 scope 的旧上下文。
- 是否把敏感凭证放入 snapshot / attributes。
- 新增传播字段是否真的跨所有技术模块通用。
