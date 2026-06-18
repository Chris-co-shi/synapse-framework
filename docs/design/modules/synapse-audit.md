# synapse-audit 设计说明

## 1. 模块使命

`synapse-audit` 定义统一、可追溯的审计事件契约和输出 Port。它确保事件在发送到数据库、日志、MQ 或外部审计平台前具有明确 subject 和 traceId，但不实现具体审计中心。

## 2. 边界

负责：

- `AuditEvent`、Subject、Target、Outcome 模型。
- 显式 `AuditContext` Scope。
- 基于 OperationContext 的事件补齐。
- `AuditRecorder` 标准入口。
- `AuditLogPort`、Noop 与 Composite 实现。

不负责：

- 审计表、Repository、查询 API 和后台。
- 审计消息可靠投递、重试和归档。
- 业务动作枚举全集。
- 敏感字段自动识别的完整数据安全方案。

## 3. 为什么必须显式可追溯

默认写 `system` 会掩盖异步、任务或补偿流程的真实来源。因此 `AuditRecorder` 在最终事件缺少 subject 或 traceId 时失败，迫使入口显式建立 `OperationContext` 或 `AuditContext`。

## 4. 核心对象角色

### 4.1 `AuditEvent`

描述 action、subject、target、occurredAt、outcome、traceId、message 和 attributes。它是事件契约，不是审计数据库 Entity。

### 4.2 `AuditEventContextEnricher`

补齐优先级：

```text
explicit event fields
  -> AuditContext
  -> OperationContextProvider
```

显式值永远优先，Framework 不覆盖调用方已经确认的主体和技术属性。

### 4.3 `AuditContext`

为只需审计主体的特殊入口提供轻量 ThreadLocal Scope，适合任务、补偿和批处理。它不是 OperationContext 的替代品。

### 4.4 `AuditRecorder`

统一编排：enrich -> validate recordable -> port.record。业务代码不应绕过 recorder 直接调用 Port，否则可能跳过上下文补齐和校验。

### 4.5 `AuditLogPort`

输出方向由消费方决定。Noop 是安全退让的空输出，不代表事件已经持久化；Composite 只是顺序广播，不提供失败隔离。

## 5. 主链路

```text
business / technical operation
  -> construct AuditEvent
  -> AuditRecorder
  -> AuditEventContextEnricher
  -> require subject + traceId
  -> AuditLogPort(s)
  -> consumer adapter: DB / MQ / log / external platform
```

## 6. 失败与可靠性边界

- 缺少 subject / traceId：记录前失败。
- Noop Port：事件被校验但不会保存。
- Composite 某个 Port 抛异常：后续 Port 可能不执行。
- 异步、重试、Outbox 和至少一次投递由消费方 adapter 设计。
- attributes 不能承载完整业务对象、密码、token 和敏感明文。
- 审计失败是否阻断主业务必须由应用层策略决定。

## 7. 扩展原则

- 数据库审计：业务/Platform 实现 `AuditLogPort`。
- 可靠 MQ：Port 内使用 Outbox 或消息基础设施。
- 特殊主体：入口显式建立 AuditContext。
- 多输出隔离：消费方提供异步/容错 composite，不修改基础 Port 语义。

## 8. 源码阅读顺序

```text
AuditSubject / AuditTarget / AuditOutcome
  -> AuditEvent
  -> AuditContextSnapshot / Scope / Context
  -> AuditEventContextEnricher
  -> AuditLogPort implementations
  -> AuditRecorder
  -> AutoConfiguration
  -> recordability and enrichment tests
```

## 9. 手写练习

1. 在 USER OperationContext 下记录未设置 subject 的事件。
2. 验证 enricher 补齐 subject、traceId 和 source attributes。
3. 无任何上下文时验证 recorder 拒绝不可追溯事件。
4. 注册两个 Port，模拟第一个失败并分析 Composite 行为。

## 10. 修改检查清单

- 是否把审计 Entity、表或查询 API 放入 Framework。
- 是否默认生成 system subject。
- 是否允许绕过 recorder 跳过校验。
- 是否把 Composite 描述成可靠广播。
- 是否把敏感/大对象写入 attributes。
- 是否让 audit 直接依赖 data 或 mq。
- Noop 默认实现是否被误解为已落库。
