# AGENTS.md Snippet: Task Traceability Rules

将以下内容追加到仓库根目录的 `AGENTS.md` 中。

```markdown
## Task Traceability Rules

1. Do not overwrite existing task files.
2. Every implementation task must have a unique task file under `docs/tasks/`.
3. Use `TASK-YYYYMMDD-NNN-short-name.md` as the task naming format.
4. `docs/tasks/CURRENT.md` may be overwritten, but it must only contain pointers to the active task, not the full task content.
5. Before implementation, read the active task file and all documents listed in its Required Reading section.
6. After every Agent / Codex execution, create a run log under `docs/tasks/runs/{TASK-ID}/`.
7. Do not overwrite existing run logs. Use incremental names such as `001-backend-implementation.md`, `002-test-review.md`.
8. Every commit should reference the related task ID.
9. If scope changes during implementation, update the task file Change Log instead of silently changing behavior.
10. Do not mark a task as done unless tests and review results are recorded.
11. If a task is cancelled or superseded, move it to `docs/tasks/cancelled/` and record the reason.
12. Do not store secrets, API keys, passwords, tokens, or private credentials in task files or run logs.
```
