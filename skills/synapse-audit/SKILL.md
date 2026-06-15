# synapse-audit Skill

## 职责

`synapse-audit` 只提供审计事件模型、审计上下文、上下文补齐、审计记录入口和 `AuditLogPort`。

## 禁止事项

- 不做 audit-service。
- 不新增审计表结构、Repository、查询 API、报表或后台。
- 不绑定数据库或消息队列作为默认审计落地实现。
- 不定义业务操作枚举全集。
- 不新增 Controller、Entity、Mapper、Repository、migration。
- 不创建 starter、demo、example、sample application。

## 标准实现

- 记录入口使用 `AuditRecorder`。
- 输出端口使用 `AuditLogPort`。
- 默认端口可以是 `NoopAuditLogPort`。
- `AuditEventContextEnricher` 只能补齐技术上下文，不得伪造 system/unknown 用户。

## 测试要求

- 覆盖审计事件创建。
- 覆盖上下文补齐。
- 覆盖敏感值脱敏。
- 覆盖 no-op 和 composite port。
- 覆盖无上下文不伪造身份。

## 必读

- `AGENTS.md`
- `docs/modules/synapse-audit.md`
- `docs/phase-2/03-boundary-checklist.md`
