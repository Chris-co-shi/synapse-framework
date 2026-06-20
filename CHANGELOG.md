# Changelog

本项目遵循语义化版本。0.x 阶段公开 API 仍可能演进，破坏性变更必须在本文件说明。

## [Unreleased]

### Added

- Broker 中立的 Messaging Envelope、Transport、可靠发布和消费分发基础。
- AuditPublisher、审计失败策略、脱敏和 `@Audited` 切面。
- 事务、数据库迁移和自动配置测试规范。
- 架构一致性脚本和 Maven 发布质量门禁。

### Changed

- Audit 消息投递统一委托 `BestEffortMessagePublisher` 或 `ReliableMessagePublisher`。
- 正式构建生成 sources、javadoc、JaCoCo、SpotBugs 和 Checkstyle 报告。

### Deprecated

- `AuditRecorder` 自 0.1.0 起弃用，新代码使用 `AuditPublisher`。
- `AuditLogPort` 自 0.1.0 起弃用；消息投递使用 `AuditPublisher`，本地兼容输出逐步迁移。
- `AuditOperation` 已由 `@Audited` 替代。

### Removed

- 无。

### Fixed

- 无。

### Security

- PR 禁止跳过测试；漏洞扫描移至每周和人工触发工作流。
