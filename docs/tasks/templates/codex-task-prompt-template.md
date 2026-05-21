# Codex Task Prompt Template

你可以复制下面的模板给 Codex 使用。

```text
你现在使用 {agent_name}。

本次任务文件：
docs/tasks/active/{TASK_FILE_NAME}

请先读取该任务文件，以及其中 Required Reading 列出的文档和 Skills。

执行要求：
1. 严格按任务文件 Scope 修改。
2. 实现前先输出自查。
3. 不允许修改 Forbidden Changes 中列出的内容。
4. 不允许扩大任务范围。
5. 修改完成后运行任务文件中要求的测试命令。
6. 在 docs/tasks/runs/{TASK_ID_SHORT_NAME}/ 下创建本次 Run Log。
7. Run Log 文件名使用递增序号，例如 001-backend-implementation.md。
8. 输出修改文件列表、测试结果、架构自查、安全自查、已知风险和后续建议。
```
