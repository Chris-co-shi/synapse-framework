# TASK-009-01 IAM Auth/RBAC 最小验证闭环

## 目标

新增 `synapse-iam` 验证模块，证明 IAM/Auth/RBAC 可以在不污染 framework core 的前提下复用 Synapse 技术基座。

本切片只做内存 Repository 驱动的最小应用层闭环：

- 用户登录
- 客户端可用性校验
- 密码 hash 校验
- 用户状态校验
- JWT access token 签发和验签
- 角色权限摘要加载
- 权限判断
- 登录成功/失败审计事件

## 范围

修改：

- 根 `pom.xml`
- `synapse-iam`
- `skills/synapse-iam/SKILL.md`
- `docs/tasks/CURRENT.md`

不修改：

- `synapse-common`
- `synapse-web`
- `synapse-data`
- `synapse-cache`
- `synapse-security`
- `synapse-audit`
- `synapse-starter`

## 明确不做

- 不实现完整后台管理系统。
- 不实现前端页面。
- 不新增数据库 migration。
- 不新增 Mapper、Entity、Repository Adapter。
- 不实现 refresh token rotation。
- 不实现登出黑名单。
- 不实现菜单、组织、租户、数据权限。
- 不创建 `docs/run-logs`。

## 验收标准

- `synapse-iam` 模块可编译。
- 登录成功返回 access token、用户摘要、角色和权限。
- access token 可通过 `SynapseJwtService` 真实验签。
- 用户不存在、密码错误、用户禁用、客户端不可用均拒绝登录。
- 登录成功和失败均写入审计事件。
- 权限不足抛出业务异常。
- 模块测试通过。
- 根目录测试通过。

## 验证命令

```bash
/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn -pl synapse-iam -am test
/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn clean test
```
