# 测试规则

## 1. 测试目标

Synapse Framework 的测试不是为了覆盖率数字，而是为了保证：

- 权限不越权
- 数据不串租户
- 登录链路安全
- 持久化行为稳定
- 代码生成结果可用
- AI Agent 修改不会破坏边界

## 2. 测试分层

```text
Unit Test
  -> Domain / Service / Converter
Slice Test
  -> Repository / Controller
Integration Test
  -> Auth / Permission / Flyway / DB
Contract Test
  -> API response format
Generated Code Test
  -> Codegen output compile/test
```

## 3. 命名规范

```text
XxxTest
XxxRepositoryTest
XxxControllerTest
XxxApplicationServiceTest
XxxIntegrationTest
```

## 4. 必测场景

每个功能至少覆盖：

1. 正常流程
2. 参数为空
3. 参数非法
4. 数据不存在
5. 重复数据
6. 权限不足
7. 数据权限不足
8. 租户隔离
9. 并发更新冲突
10. 审计日志写入

## 5. Auth 模块必测

- 登录成功
- 用户不存在
- 密码错误
- 用户禁用
- 临时锁定
- 锁定过期后可恢复
- refresh token 正常刷新
- refresh token 过期
- refresh token 被吊销
- refresh token rotation 并发冲突
- logout 校验 token 归属
- logout 后 access token 黑名单生效

## 6. RBAC 必测

- 用户无角色不能访问受限接口
- 用户有角色但无权限不能访问
- 用户有权限可以访问
- 禁用角色后权限失效
- 修改角色菜单后权限缓存刷新
- 动态菜单只返回有权限菜单

## 7. 数据权限必测

- SELF 只能看自己的数据
- DEPT 只能看本部门数据
- DEPT_AND_CHILDREN 能看子部门数据
- CUSTOM_DEPT 只能看授权部门数据
- ALL 只能授权给高权限角色

## 8. Repository 必测

- 新增
- 修改
- 逻辑删除
- 乐观锁冲突
- 租户过滤
- 唯一约束冲突
- 分页查询
- 排序白名单

## 9. Controller 必测

- 参数校验错误响应
- 未登录返回 401
- 无权限返回 403
- 业务错误码返回
- traceId 返回
- 响应结构一致

## 10. Codegen 必测

代码生成器输出必须验证：

- 文件路径正确
- 包名正确
- 能编译
- 能运行测试
- 不生成 Controller -> Mapper 直连代码
- 不生成 Domain 依赖 MyBatis-Plus 代码
- 不生成无白名单排序代码

## 11. 测试数据规则

- 测试数据必须最小化。
- 不依赖本地已有数据库状态。
- 不共享可变全局状态。
- 必要时使用 Testcontainers。
- Data 关键路径使用 H2 + Testcontainers 验证数据库兼容。
- Cache 关键路径使用 Redis Testcontainers 验证 Lua 锁和限流。
- Security 关键路径验证 OAuth2 token 签发、JWK 暴露、资源服务器验签、401/403。
- Flyway migration 必须在集成测试中验证。

## 12. Skill 交付测试规则

每个模块完成时，验收必须同时包含：

- 模块代码完成。
- 模块级测试通过。
- 关键模块根目录 `mvn clean test` 通过。
- `skills/<module-name>/SKILL.md` 已更新为最佳实践。

模块测试失败时，不允许把当前实现沉淀为最终 Skill。

## 13. 命令要求

后端至少提供：

```bash
mvn test
mvn verify
```

前端至少提供：

```bash
pnpm lint
pnpm test
pnpm build
```

## 14. 禁止行为

- 禁止删除失败测试。
- 禁止为了通过测试降低断言。
- 禁止只测 happy path。
- 禁止 mock 掉核心权限逻辑后声称权限测试通过。
- 禁止集成测试依赖开发者本机数据。
