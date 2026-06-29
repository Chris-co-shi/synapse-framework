# Synapse Framework 仓库差距分析

本文记录 2026-06-29 `main` 分支与 [V1 Security Baseline](v1-security-baseline.md) 的差距。

## 差距表

| 编号 | 当前事实 | 目标 | 成本 | 决策 |
| --- | --- | --- | --- | --- |
| F-001 | `synapse-security` 包含完整 GatewayProof 协议和配置 | 删除 GatewayProof | M | NOW |
| F-002 | WebMVC 包含 `GatewayProofVerificationFilter` | 删除 | S | NOW |
| F-003 | WebFlux 包含 `GatewayProofWebFilter` | 删除 | S | NOW |
| F-004 | `AuthenticatedUser` / `AuthenticatedClient` 包含 tenant、roles、permissions | 收敛为最小身份主体 | M | NOW |
| F-005 | JWT Authority 默认映射 scope、roles、permissions | 默认只映射 Scope | M | NOW |
| F-006 | Resource Server 已支持 issuer、audience、required claims、clock skew | 保留并补充标准 JWT Profile | M | NOW |
| F-007 | WebFlux 已提供自定义安全链配置入口 | 支持 Gateway Authentication Only | S | NOW |
| F-008 | denylist 默认语义较重 | 不阻塞 V1，默认关闭 | S | NOW |
| F-009 | 文档仍把 GatewayProof 描述为正式能力 | 标记废弃并删除引用 | S | NOW |
| F-010 | 部分历史 phase-2 文档包含已删除模块或旧边界 | 以新基线为最高优先级 | S | NOW |

## 保留能力

- WebMVC / WebFlux 分离；
- Resource Server core 共享校验语义；
- JWT Decoder 与 Claim Accessor；
- CurrentPrincipalContext；
- Reactive CurrentPrincipalContext；
- OperationContext 单向桥接；
- PermissionChecker 和声明式权限入口；
- OAuth2 Authorization Server 技术支持模块；
- OAuth2 Client 技术模块。

## 实施顺序

1. 文档与配置基线；
2. 删除 GatewayProof；
3. 调整 Principal 最小字段；
4. 调整 Authority 映射；
5. 增加 `typ=at+jwt`、单 Audience 和 USER/CLIENT Claim 测试；
6. 增加 Gateway Authentication Only 测试；
7. 更新 BOM、模块手册、配置 Metadata 和 Skill。

## 高风险

- 文档声称 GatewayProof 取消，但生产自动配置仍启用；
- JWT 继续携带 Permission，导致 Platform 权限模型与标准第三方接入耦合；
- tenant 字段在单租户版本中形成虚假默认值；
- 删除 GatewayProof 时误删 JWT Resource Server 或 Bearer Token 转发能力。

每个 Codex 任务只处理一个 Gap，并同步更新本文状态。
