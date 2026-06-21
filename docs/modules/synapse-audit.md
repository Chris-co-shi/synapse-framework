# synapse-audit 使用手册

## 1. 定位

`synapse-audit` 定义审计事件、可信上下文补全、脱敏、注解入口以及成功/失败事务策略。
它依赖 `synapse-messaging`，但不实现 Broker、Outbox Store、审计表、查询 API 或报表。

## 2. 上下文与脱敏

`AuditEventContextEnricher` 使用当前可信 `OperationContext` 补齐主体、租户、traceId、requestId 和来源。
普通 HTTP 身份 Header 不得建立或覆盖审计主体。无法确定真实主体或 traceId 时拒绝记录。

`AuditSanitizer` 在发送前屏蔽凭据类字段；`@Audited` 不采集方法参数、返回值或完整请求体。

## 3. 成功审计

- `AuditSuccessPolicy.BEST_EFFORT`：立即尝试发送；失败记录告警，不改变成功业务结果。
- `AuditSuccessPolicy.TRANSACTIONAL_OUTBOX`：在当前业务本地事务内登记 Outbox。

Transactional Outbox 要求应用提供 `ReliableMessagePublisher` 和本地 `OutboxStore`，且 Outbox 与业务数据使用同一数据源和事务。事务提交时二者同时提交，事务回滚时二者同时回滚。

## 4. 失败审计

- `AuditFailurePolicy.NONE`：不产生失败审计，默认值。
- `BEST_EFFORT_AFTER_ROLLBACK`：业务事务回滚完成后尝试发送。
- `REQUIRES_NEW_AFTER_ROLLBACK`：业务事务回滚完成后，在独立事务内登记 Outbox。
- `EXTERNAL_SINK`：业务事务回滚完成后调用应用提供的 `AuditFailureSink`。

失败审计错误只会作为 suppressed error 附加到原业务异常，不能覆盖原业务异常。Framework 不提供外部 Sink、Outbox Store 或事务管理器实现。

## 5. 注解入口

```java
@Audited(
    action = "order.approve",
    targetType = "ORDER",
    successPolicy = AuditSuccessPolicy.TRANSACTIONAL_OUTBOX,
    failurePolicy = AuditFailurePolicy.REQUIRES_NEW_AFTER_ROLLBACK
)
@Transactional
public void approveOrder() {
    // business operation
}
```

`targetId` 未指定时使用方法名作为技术标识；需要真实业务目标 ID 时应显式构造 `AuditEvent`。

## 6. AOP 与事务顺序

调用顺序固定为：

```text
Security(-200) -> Transaction(0) -> Audit(200) -> business method
```

Audit 只注册 Advisor，不创建 `AutoProxyCreator`。Framework 自动将 Spring 标准 Transaction Advisor 的 order 校正为 `0`，消费方不需要手工配置 `@EnableTransactionManagement(order = 0)`。

当可靠 Publisher 存在且 Audit AOP 启用时，缺少标准事务 Advisor 或顺序异常会启动失败，防止可靠审计静默退化到事务外。

## 7. 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `synapse.audit.enabled` | `true` | 启用 Audit 自动配置 |
| `synapse.audit.destination` | `synapseAudit-out-0` | 审计消息逻辑 binding 名 |
| `synapse.audit.aop-enabled` | `true` | 启用 `@Audited` Advisor |

## 8. 兼容入口与边界

`AuditRecorder`/`AuditLogPort` 仅作为显式本地输出兼容入口，不参与默认消息链路。

- 不实现持久化、查询、报表、归档和保留策略。
- 不记录完整敏感请求体或凭据。
- 不承诺 Exactly-once；可靠审计继承 Messaging 的 At-least-once 语义。
