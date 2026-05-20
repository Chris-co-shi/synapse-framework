# TASK-YYYYMMDD-001-task-name

## 1. Metadata

- Task ID: TASK-YYYYMMDD-001
- Title:
- Status: backlog | active | done | cancelled
- Phase:
- Owner Agent:
- Related Agent:
- Related Skills:
- Related Issue:
- Related Branch:
- Related PR:
- Created At:
- Updated At:

## 2. Background

说明为什么要做这个任务。

## 3. Goal

本次任务要达成什么目标。

## 4. Non-Goals

本次明确不做什么，防止 Codex 扩大范围。

## 5. Scope

允许修改：

-

禁止修改：

-

## 6. Required Reading

Codex 执行前必须读取：

- AGENTS.md
- docs/00-positioning.md
- docs/01-architecture.md
- docs/03-package-rules.md
- docs/07-test-rules.md

按需读取：

- skills/xxx/SKILL.md

## 7. Implementation Requirements

1.
2.
3.

## 8. Architecture Constraints

1. Controller 不允许直接依赖 Mapper。
2. Domain 不允许依赖 MyBatis-Plus。
3. Entity 只允许存在 infrastructure.persistence.entity。
4. 不允许使用 IService / ServiceImpl 作为业务分层。
5. 不允许直接复制开源项目代码。

## 9. Acceptance Criteria

- [ ] 编译通过
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 分层规则未破坏
- [ ] 安全规则未破坏
- [ ] 文档已更新
- [ ] Review 通过

## 10. Execution Plan

由 Codex 执行前填写或输出：

1.
2.
3.

## 11. Result Summary

任务完成后填写：

- 修改文件：
- 新增文件：
- 删除文件：
- 测试结果：
- 已知风险：
- 后续任务：

## 12. Change Log

| Time | Actor | Change |
|---|---|---|
|  |  |  |