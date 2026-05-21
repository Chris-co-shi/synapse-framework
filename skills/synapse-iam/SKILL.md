---
name: synapse-iam
description: Synapse IAM 验证模块最佳实践。Use when Codex implements or reviews synapse-iam code involving IAM/Auth/RBAC validation, users, clients, roles, permissions, login flows, permission checks, audit integration, or future IAM persistence.
---

# Synapse IAM

## 必读

- `AGENTS.md`
- `docs/00-positioning.md`
- `docs/01-architecture.md`
- `docs/02-module-boundary.md`
- `docs/03-package-rules.md`
- `docs/04-database-rules.md`
- `docs/05-api-rules.md`
- `docs/06-security-rules.md`
- `docs/07-test-rules.md`
- `docs/08-ai-development-rules.md`
- `skills/synapse-common/SKILL.md`
- `skills/synapse-web/SKILL.md`
- `skills/synapse-security/SKILL.md`
- `skills/synapse-audit/SKILL.md`
- 涉及持久化时读取 `skills/synapse-data/SKILL.md`

## 模块职责和边界

- `synapse-iam` 是 IAM/Auth/RBAC 验证模块，用来验证 Web、Security、Audit、后续 Data/Cache 能否支撑真实后台能力。
- 可以实现用户、客户端、角色、权限、登录、权限摘要和权限判断的最小闭环。
- 不属于 framework core，不允许反向修改 common/web/security/audit/starter 来适配业务。
- 不实现完整后台管理系统，不实现前端页面，不实现低代码。
- 不把 IAM 用户、角色、菜单、登录流程写入 `synapse-security`。

## 推荐包结构

```text
com.indigo.synapse.iam
├── interfaces
│   ├── controller
│   ├── request
│   └── response
├── application
│   ├── service
│   ├── command
│   └── result
├── domain
│   ├── model
│   └── repository
└── infrastructure
    └── persistence
        ├── entity
        ├── mapper
        ├── repository
        └── converter
```

## 允许使用的技术和禁止事项

允许：

- 依赖 `synapse-common`、`synapse-web`、`synapse-security`、`synapse-audit`。
- 使用 `SynapseJwtService` 签发和验签 access token。
- 使用 `PasswordEncoder` 校验密码 hash。
- 使用 `AuditRecorder` 记录登录成功、登录失败和权限变更等事件。
- 后续持久化切片使用 MyBatis-Plus、Flyway、H2/Testcontainers。

禁止：

- Controller 直接依赖 Mapper。
- Entity 直接暴露给 Controller 或前端。
- Domain Model 依赖 MyBatis-Plus。
- 在日志或审计属性中记录明文密码、access token、refresh token、client secret。
- 把 refresh token rotation、登录风控、数据权限一次性塞进第一条切片。
- 为了让 IAM 通过测试而放宽 security 默认拒绝规则。

## 标准实现模式

- 登录链路：

```text
AuthController
  -> IamAuthApplicationService
  -> IamClientRepository / IamUserRepository / IamPermissionApplicationService
  -> PasswordEncoder / SynapseJwtService / AuditRecorder
```

- 权限链路：

```text
IamPermissionApplicationService
  -> IamRoleRepository
  -> IamPermissionRepository
  -> PermissionSummary
```

- 登录失败必须写审计事件，再抛出稳定错误码。
- 登录成功必须返回用户摘要、角色标识、权限标识和 access token。
- 权限判断失败抛出 `PERMISSION_DENIED`，不返回 `null` 或布尔值吞掉错误。
- IAM Controller 只做 request 到 command、result 到 response 的转换。
- 公开端点 `/api/admin/auth/login` 只在 IAM 模块启用时出现，不进入 security 基座默认白名单。

## 测试要求

- 覆盖登录成功、用户不存在、密码错误、用户禁用、客户端不可用。
- 覆盖 JWT access token 真实签发和真实验签。
- 覆盖角色权限摘要加载。
- 覆盖权限不足抛出业务异常。
- 覆盖登录成功和失败审计写入。
- 后续持久化切片必须覆盖 Flyway migration、Repository Adapter、唯一约束、乐观锁和租户预留。
- 模块完成后运行 `/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn -pl synapse-iam -am test`。
- 关键切片完成后运行根目录 `/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn clean test`。

## 常见错误

- 把 IAM 业务逻辑写进 `synapse-security`。
- 只 mock JWT，不做真实签发验签。
- 登录失败不写审计。
- 用“用户无角色返回空权限”代替明确权限拒绝测试。
- 让 Controller 直接访问 Repository 或 Mapper。
- 在第一条切片就实现完整 refresh token、菜单、数据权限、租户隔离，导致边界失控。

## 示例任务拆分方式

- TASK-009-01：IAM Auth/RBAC 内存验证闭环。
- TASK-009-02：IAM Flyway 表结构、Entity、Mapper、Repository Adapter。
- TASK-009-03：OAuth2 RegisteredClientRepository 持久化接入。
- TASK-009-04：Refresh Token hash 存储、rotation 和复用检测。
- TASK-009-05：Controller 集成测试和资源服务器 401/403 验证。
