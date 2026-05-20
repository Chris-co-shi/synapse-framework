# TASK-YYYYMMDD-NNN-task-name

## 1. Metadata

- Task ID: TASK-YYYYMMDD-NNN
- Title:
- Status: backlog | active | done | cancelled
- Phase:
- Owner Agent:
- Related Agents:
- Related Skills:
- Related Issue:
- Related Branch:
- Related PR:
- Created At:
- Updated At:

## 2. Background

说明为什么要做这个任务。

需要回答：

1. 当前问题是什么？
2. 为什么现在要做？
3. 和当前框架阶段有什么关系？
4. 如果不做，会产生什么影响？

## 3. Goal

本次任务要达成的目标。

示例：

- 建立 Maven 多模块工程骨架。
- 实现统一响应结构。
- 实现 Auth 登录最小闭环。
- 实现 `iam_user` 持久化层。
- 实现字典管理 CRUD。

## 4. Non-Goals

本次明确不做什么，防止 Codex / Agent 扩大范围。

示例：

- 不实现前端页面。
- 不实现微服务拆分。
- 不引入工作流。
- 不实现完整低代码。
- 不重构无关模块。

## 5. Scope

### 5.1 Allowed Changes

允许修改：

- 

### 5.2 Forbidden Changes

禁止修改：

- 

### 5.3 Caution Areas

谨慎修改：

- 

## 6. Required Reading

执行前必须读取：

- AGENTS.md
- docs/00-positioning.md
- docs/01-architecture.md
- docs/02-module-boundary.md
- docs/03-package-rules.md
- docs/04-database-rules.md
- docs/05-api-rules.md
- docs/06-security-rules.md
- docs/07-test-rules.md

按任务类型读取：

- skills/synapse-architecture-review/SKILL.md
- skills/synapse-java-backend/SKILL.md
- skills/synapse-mybatis-plus-persistence/SKILL.md
- skills/synapse-security-rbac/SKILL.md
- skills/synapse-vue-admin/SKILL.md
- skills/synapse-test-engineering/SKILL.md

## 7. Implementation Requirements

1. 
2. 
3. 

## 8. Architecture Constraints

1. Controller 不允许直接依赖 Mapper。
2. Controller 不允许直接返回 Entity。
3. Domain Model 不允许依赖 MyBatis-Plus。
4. Entity 只允许存在于 infrastructure persistence 层。
5. Repository Port 必须面向 application/domain 暴露。
6. Repository Adapter 必须封装 MyBatis-Plus 细节。
7. 不允许使用 IService / ServiceImpl 作为业务分层。
8. 不允许 Entity 继承 Model<T>。
9. 不允许使用 Map<String, Object> 代替明确 DTO。
10. 不允许直接复制开源项目代码。
11. 不允许为了快速实现破坏模块边界。
12. 不允许引入无必要依赖。

## 9. Security Constraints

涉及认证、授权、用户、角色、菜单、租户、数据权限时必须遵守：

1. 权限判断不能只依赖前端菜单。
2. Token 操作必须校验归属关系。
3. Refresh Token rotation 必须具备原子性。
4. 数据权限不能只在 Controller 层实现。
5. 租户 ID 不能由前端传入后直接信任。
6. 安全失败必须返回明确错误码。
7. 不允许吞异常或返回 null 表示失败。

## 10. Acceptance Criteria

- [ ] 编译通过
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 必要的接口测试通过
- [ ] 分层规则未破坏
- [ ] 安全规则未破坏
- [ ] 数据库 migration 可重复执行验证通过
- [ ] 文档已更新
- [ ] Run Log 已记录
- [ ] Review 通过
- [ ] Commit / PR 已关联 Task ID

## 11. Suggested Execution Plan

由 Codex / Agent 执行前填写或输出：

1. 
2. 
3. 

## 12. Result Summary

任务完成后填写。

### 12.1 Files Changed

新增：

- 

修改：

- 

删除：

- 

### 12.2 Test Results

执行命令：

```bash

```

结果：

```text

```

### 12.3 Architecture Self-Check

- Controller 是否直接依赖 Mapper：
- Controller 是否直接返回 Entity：
- Domain 是否依赖 MyBatis-Plus：
- 是否使用 IService / ServiceImpl：
- Repository Port / Adapter 是否清晰：
- 是否影响无关模块：

### 12.4 Security Self-Check

- 是否涉及认证：
- 是否涉及授权：
- 是否涉及 Token：
- 是否涉及数据权限：
- 是否涉及租户隔离：
- 是否存在越权风险：

### 12.5 Known Risks

1. 
2. 

### 12.6 Follow-up Tasks

- [ ] 
- [ ] 

## 13. Change Log

| Time | Actor | Change |
|---|---|---|
|  |  |  |
