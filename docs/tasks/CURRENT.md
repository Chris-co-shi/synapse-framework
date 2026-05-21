# Current Task

## Active Task

TASK-009-01 IAM Auth/RBAC 最小验证闭环

## Task File

docs/tasks/active/TASK-009-01-iam-auth-rbac-validation.md

## Goal

新增 `synapse-iam` 验证模块，完成 IAM/Auth/RBAC 第一条最小闭环：客户端校验、用户登录、密码校验、用户状态校验、JWT access token 签发、角色权限摘要、权限判断和审计事件。

TASK-009 目标是验证基座能力是否能支撑第一个后台管理验证模块，而不是直接实现完整后台管理系统。本切片暂不做持久化，下一切片再接入 Flyway、Entity、Mapper、Repository Adapter。

## Completed Foundation Tasks

| Task ID | 状态 | 说明 |
|---|---|---|
| TASK-002 | 已完成 | Common Foundation：错误码、异常、基础契约与测试已沉淀。 |
| TASK-003 / TASK-003-01 | 已完成并补强 | Web Foundation：统一响应、异常、分页、Trace、OpenAPI；已拆分 Base/MVC/WebFlux 自动配置。 |
| TASK-004 | 已完成并补强 | Cache Foundation：Redis、Lua、可重入锁、滑动窗口限流；已明确 null 缓存语义。 |
| TASK-005 | 已完成并补强 | Security OAuth2 Foundation：Authorization Server、Resource Server、JWT/JWK；已补生产模式保护。 |
| TASK-006 | 已完成 | Audit Foundation：审计事件、审计 Port、操作日志注解与测试已完成。 |
| TASK-007 | 已完成并补强 | Starter Auto Configuration：统一自动配置、条件装配、feature switch；已同步过滤 Data/Cache 外部自动配置。 |
| TASK-008 | 已完成并补强 | Example App：基座接入示例；上下文测试通过 feature switch 验证 Starter 接入。 |

## Explicitly Out of Scope

Do not implement:

- 完整后台管理系统
- 生产业务模块
- 复杂多租户
- 代码生成器
- 低代码平台
- 工作流
- 消息中心
- frontend pages
- 数据库 migration
- Mapper / Entity / Repository Adapter
- refresh token rotation
- 登出黑名单
- docs/run-logs
- standalone Run Log

## Required Validation

Run:

```bash
/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn clean test
```

## Next Step

1. 完成 TASK-009-01 根目录测试验收。
2. 下一切片进入 TASK-009-02：IAM Flyway 表结构、Entity、Mapper、Repository Adapter。
3. 后续再接 OAuth2 RegisteredClientRepository 持久化、refresh token rotation、Controller 集成测试和资源服务器 401/403。
