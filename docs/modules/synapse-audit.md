# synapse-audit 使用手册

## 1. 定位

`synapse-audit` 定义审计事件、上下文补全、脱敏、显式/注解入口和失败策略。
它依赖 `synapse-messaging`，但只通过 `BestEffortMessagePublisher` 与 `ReliableMessagePublisher` 发布，
不直接使用 StreamBridge、Broker、OutboxStore，也不实现审计中心、审计表、查询 API 或报表。

## 2. 事件与上下文

`AuditEvent` 包含 action、subject、target、occurredAt、outcome、traceId、message 和 attributes。
`AuditEventContextEnricher` 按“显式字段、AuditContext、OperationContext”补充：

- subject/principal 与 tenant。
- traceId、requestId 和 operation source。
- `audit.eventId`。
- `audit.sourceService`。

无法确定真实主体时不会伪造 system/unknown，记录前仍要求 subject 和 traceId。

## 3. 脱敏

`AuditSanitizer` 是发送前最终脱敏端口，默认实现屏蔽 key 中包含 password、token、secret、key、
authorization、cookie、credential 等关键词的值。`@Audited` 切面不采集方法参数或返回值，避免把完整请求体、
Authorization、Access/Refresh Token、Password、Private Key 和 Cookie 写入事件。

## 4. 发布策略

- `AuditFailurePolicy.CONTINUE`：普通审计委托 `BestEffortMessagePublisher`；失败告警并继续业务。
- `AuditFailurePolicy.ROLLBACK`：关键审计委托 `ReliableMessagePublisher`；异常向外传播，由当前本地事务回滚。

可靠审计要求 Messaging 已显式启用 reliable、应用提供本地 `OutboxStore`，且调用处存在活动本地事务。
Audit 不创建第二套 Envelope、Transport、Outbox、Retry Scheduler 或消费组。

## 5. 注解入口

```java
@Audited(
    action = "order.approve",
    targetType = "ORDER",
    failurePolicy = AuditFailurePolicy.ROLLBACK
)
public void approveOrder() {
    // business operation
}
```

切面根据返回或异常生成 SUCCESS/FAILURE 事件。`targetId` 未指定时使用方法名作为技术标识；
需要真实业务目标 ID 时，建议显式构造 `AuditEvent` 并调用 `AuditPublisher`。

## 6. 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `synapse.audit.enabled` | `true` | 启用 Audit 自动配置 |
| `synapse.audit.destination` | `synapseAudit-out-0` | 审计消息逻辑 binding 名 |
| `synapse.audit.aop-enabled` | `true` | 启用 `@Audited` 切面 |

## 7. 兼容入口

`AuditRecorder`/`AuditLogPort` 保留为显式本地输出兼容入口，不参与默认消息发布链路。
新消息投递统一使用 `AuditPublisher`，不得在自定义 Publisher 中直接访问 StreamBridge 或 Broker。

## 8. 边界

- 不记录完整敏感请求体或凭据。
- 不实现持久化、查询、报表、归档和保留策略。
- 不创建 Controller、Entity、Repository、migration、starter 或 demo。
- 不承诺 Exactly-once；可靠审计继承 Messaging 的 At-least-once 语义。
