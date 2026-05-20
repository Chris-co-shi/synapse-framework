# TASK-20260520-001-init-task-traceability

## 1. Metadata

- Task ID: TASK-20260520-001
- Title: Initialize task traceability structure
- Status: backlog
- Phase: Phase 1 - AI Collaboration Rules
- Owner Agent: architect_agent
- Related Agents: backend_agent, test_review_agent
- Related Skills: synapse-architecture-review
- Related Issue:
- Related Branch: docs/TASK-20260520-001-init-task-traceability
- Related PR:
- Created At: 2026-05-20
- Updated At: 2026-05-20

## 2. Background

当前项目使用 Codex / Agent 辅助开发。若每次都覆盖同一个 `task.md`，会导致任务背景、输入提示、执行过程、测试结果和 Review 结论丢失。

为了让 Synapse Framework 的长期演进可追溯，需要建立轻量级 Task Spec + Run Log 机制。

## 3. Goal

建立 `docs/tasks` 任务追溯目录和基础模板，支持后续每个开发任务具备独立任务文件、执行记录、状态流转和 Commit / PR 关联。

## 4. Non-Goals

- 不实现业务功能。
- 不修改后端生产代码。
- 不修改前端生产代码。
- 不引入 Jira、禅道等外部任务系统。
- 不修改 CI/CD 流程。

## 5. Scope

### 5.1 Allowed Changes

允许修改：

- docs/tasks/**
- AGENTS.md

### 5.2 Forbidden Changes

禁止修改：

- backend source code
- frontend source code
- database migration
- build scripts

### 5.3 Caution Areas

谨慎修改：

- AGENTS.md，只追加 Task Traceability Rules，不改已有规则。

## 6. Required Reading

执行前必须读取：

- AGENTS.md
- docs/08-ai-development-rules.md

按任务类型读取：

- skills/synapse-architecture-review/SKILL.md

## 7. Implementation Requirements

1. 新增 `docs/tasks/README.md`。
2. 新增 `docs/tasks/CURRENT.md`。
3. 新增 Task Spec 模板。
4. 新增 Run Log 模板。
5. 新增 backlog / active / done / cancelled / runs 目录。
6. 每个空目录添加 `.gitkeep`。
7. 提供可追加到 AGENTS.md 的 Task Traceability Rules。
8. 提供 Codex Task Prompt 模板。

## 8. Architecture Constraints

1. 不影响生产代码。
2. 不修改现有模块边界。
3. 不引入外部系统依赖。
4. 不把任务追溯规则散落到多个位置。

## 9. Security Constraints

1. Task 文件和 Run Log 中不得记录密钥、Token、密码或私有凭证。
2. Run Log 中粘贴 Prompt 时必须移除敏感信息。
3. 不允许把本地私有路径、私有账号、API Key 写入模板示例。

## 10. Acceptance Criteria

- [ ] `docs/tasks` 目录结构完整
- [ ] Task Spec 模板可直接复制使用
- [ ] Run Log 模板可直接复制使用
- [ ] `CURRENT.md` 只作为任务指针
- [ ] AGENTS 规则片段已提供
- [ ] 空目录已添加 `.gitkeep`
- [ ] 无生产代码变更

## 11. Suggested Execution Plan

1. 创建 `docs/tasks` 目录结构。
2. 写入 README、CURRENT、templates、snippets。
3. 检查是否存在空目录无法提交的问题。
4. 输出后续使用方式。

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
find docs/tasks -maxdepth 3 -type f | sort
```

结果：

```text

```

### 12.3 Architecture Self-Check

- Controller 是否直接依赖 Mapper：否
- Controller 是否直接返回 Entity：否
- Domain 是否依赖 MyBatis-Plus：否
- 是否使用 IService / ServiceImpl：否
- Repository Port / Adapter 是否清晰：不涉及
- 是否影响无关模块：否

### 12.4 Security Self-Check

- 是否涉及认证：否
- 是否涉及授权：否
- 是否涉及 Token：否
- 是否涉及数据权限：否
- 是否涉及租户隔离：否
- 是否存在越权风险：否

### 12.5 Known Risks

1. 如果后续不强制 Codex 创建 Run Log，机制会流于形式。
2. 如果任务粒度过大，Run Log 仍然会难以追溯。

### 12.6 Follow-up Tasks

- [ ] 将 Task Traceability Rules 追加到 AGENTS.md。
- [ ] 后续每次 Codex 执行都引用 active task 文件。
- [ ] 在 PR 模板中加入 Task ID 字段。

## 13. Change Log

| Time | Actor | Change |
|---|---|---|
| 2026-05-20 | Chris | Create initial task traceability task |
