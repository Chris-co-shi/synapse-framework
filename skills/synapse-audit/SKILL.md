# synapse-audit Skill

## 职责

提供 AuditEvent、AuditOutcome、AuditFailurePolicy、AuditPublisher、AuditSanitizer、`@Audited` 和 AuditAspect。
消息投递必须委托 Messaging 发布器。

## 使用规则

- 普通审计使用 `CONTINUE`，通过 BestEffort 发布，失败告警但不中断业务。
- 关键审计使用 `ROLLBACK`，通过 Reliable 发布，并确保调用处已有活动本地事务和 OutboxStore。
- 显式字段优先于 AuditContext 和 OperationContext。
- 不确定真实主体时不得伪造 system/unknown。

## 安全规则

- 不采集注解方法参数和返回值。
- 禁止记录 Authorization、Access/Refresh Token、Password、Secret、Private Key、Cookie 和完整敏感请求体。
- 自定义 AuditSanitizer 只能加强脱敏，不能绕过基本凭据保护。

## 禁止事项

- 不直接使用 StreamBridge 或 Broker SDK。
- 不实现第二套 Envelope、Outbox、Retry Scheduler、消费组或审计表。
- 不创建 audit-service、Controller、Repository、migration、starter 或 demo。

## 测试要求

- 覆盖上下文补齐、eventId/service、脱敏和无上下文失败。
- 覆盖普通审计继续业务、关键审计可靠登记及失败传播。
- 覆盖 `@Audited` 成功/失败事件与不采集参数。
- 覆盖自动配置退让、开关和 Configuration Metadata。

## 必读

- `AGENTS.md`
- `docs/modules/synapse-audit.md`
- `docs/modules/synapse-messaging.md`
- `docs/phase-2/03-boundary-checklist.md`
