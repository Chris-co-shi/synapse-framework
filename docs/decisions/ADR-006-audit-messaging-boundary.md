# ADR-006：Audit 与 Messaging 边界

## Status

Accepted

## Context

审计需要可靠或尽力发布，但自行接入 Broker、实现 envelope、重试和失败存储会形成第二套
消息基础设施。

## Decision

`synapse-audit` 负责审计模型、注解/API、上下文补全、脱敏、失败策略和审计语义，并依赖
`synapse-messaging`。`AuditPublisher` 只能委托 `ReliableMessagePublisher` 或
`BestEffortMessagePublisher`，不得直接依赖 StreamBridge 或 Broker。

## Consequences

消息传输与审计语义边界清晰。关键审计可在 Outbox 登记失败时回滚当前本地事务；普通审计
可继续业务并产生告警。Audit 对 Messaging 的单向依赖是允许的。

## Rejected Alternatives

- Audit 自建消息链路：重复 envelope、重试和存储语义。
- Messaging 依赖 Audit：形成反向依赖和循环风险。
- 所有审计都强制回滚业务：可用性和业务风险不可接受。

## Date

2026-06-19
