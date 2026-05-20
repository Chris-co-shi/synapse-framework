# Task Traceability

本目录用于沉淀 Synapse Framework 的任务定义、执行记录、Review 结果和任务状态流转。

核心目标：

1. 防止每次覆盖 `task.md` 导致历史丢失。
2. 让每一次 Codex / Agent 执行都有明确任务锚点。
3. 让任务、分支、提交、PR、测试结果和 Review 结果可以追溯。
4. 让框架长期演进时保留设计依据和变更上下文。

## 目录结构

```text
docs/tasks/
├── README.md
├── CURRENT.md
├── backlog/
├── active/
├── done/
├── cancelled/
├── runs/
│   └── TASK-YYYYMMDD-NNN-short-name/
│       ├── 001-architect-plan.md
│       ├── 002-backend-implementation.md
│       ├── 003-test-review.md
│       └── 004-final-summary.md
├── templates/
│   ├── task-template.md
│   └── run-log-template.md
└── snippets/
    └── agents-task-traceability-rules.md
```

## 状态说明

| 状态 | 目录 | 说明 |
|---|---|---|
| backlog | `docs/tasks/backlog/` | 已记录但暂未执行 |
| active | `docs/tasks/active/` | 当前正在执行 |
| done | `docs/tasks/done/` | 已完成并记录结果 |
| cancelled | `docs/tasks/cancelled/` | 已取消或废弃 |
| runs | `docs/tasks/runs/` | 每次 Agent / Codex 执行日志 |

## 命名规范

Task 文件命名：

```text
TASK-YYYYMMDD-NNN-short-name.md
```

示例：

```text
TASK-20260520-001-init-maven-multimodule.md
TASK-20260520-002-common-web-response.md
TASK-20260520-003-auth-login-minimal-loop.md
TASK-20260520-004-iam-user-persistence.md
TASK-20260520-005-dict-management-crud.md
```

Run Log 文件命名：

```text
NNN-agent-or-stage-name.md
```

示例：

```text
001-architect-plan.md
002-backend-implementation.md
003-test-review.md
004-final-summary.md
```

## 推荐工作流

### 1. 创建任务

从模板复制：

```bash
cp docs/tasks/templates/task-template.md docs/tasks/backlog/TASK-20260520-001-init-maven-multimodule.md
```

填写任务背景、目标、范围、验收标准和 Required Reading。

### 2. 激活任务

将任务移动到 `active/`：

```bash
mv docs/tasks/backlog/TASK-20260520-001-init-maven-multimodule.md docs/tasks/active/
```

同时更新 `docs/tasks/CURRENT.md`，只保留当前任务指针，不放完整任务内容。

### 3. 执行任务

给 Codex / Agent 的提示词必须包含任务文件路径，例如：

```text
你现在使用 backend_agent。

本次任务文件：
docs/tasks/active/TASK-20260520-001-init-maven-multimodule.md

请先读取该任务文件以及其中 Required Reading 列出的文档。
严格按 Scope 修改。
修改完成后，在 docs/tasks/runs/TASK-20260520-001-init-maven-multimodule/ 下创建本次 Run Log。
```

### 4. 记录 Run Log

每一次 Codex / Agent 执行后，都要在 `docs/tasks/runs/{TASK-ID}/` 下新增 Run Log。

不要覆盖之前的 Run Log。

### 5. 完成任务

任务完成后：

1. 更新 Task 文件的 `Result Summary`。
2. 更新 Task 文件的 `Change Log`。
3. 将 Task 文件从 `active/` 移动到 `done/`。
4. 更新 `CURRENT.md`。
5. Commit message 引用 Task ID。

示例：

```text
feat(auth): implement login minimal loop

Refs: TASK-20260520-003
```

## 分支命名建议

```text
feature/TASK-20260520-001-init-maven-multimodule
fix/TASK-20260520-006-refresh-token-rotation
docs/TASK-20260520-007-agent-rules
test/TASK-20260520-008-auth-regression-tests
```

## 基本原则

1. 不覆盖历史任务。
2. 一个任务一个 Task Spec。
3. 一次 Agent 执行一个 Run Log。
4. 一个功能一个 Branch。
5. 每个 Commit 引用 Task ID。
6. 任务范围变化必须记录到 Task Change Log。
7. 未记录测试结果和 Review 结果，不允许标记为 done。
