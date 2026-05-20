# AI / Codex 协作规则

## 1. 总原则

AI Agent 是开发协作工具，不是架构最终决策者。

所有 AI 修改必须受以下内容约束：

- AGENTS.md
- docs 设计文档
- skills/SKILL.md
- 测试结果
- Review Checklist

## 2. 工作模式

采用：

```text
人负责方向和取舍
AI 负责分析、实现、测试、Review 辅助
文档负责长期约束
测试负责防回归
```

## 3. 不让 Codex 做什么

- 不让 Codex 做大范围开源项目对标分析。
- 不让 Codex 一次性实现多个大模块。
- 不让 Codex 自行决定架构边界。
- 不让 Codex 未读文档直接写代码。
- 不让 Codex 复制开源项目源码。

## 4. 让 Codex 做什么

适合任务：

- 单个模块实现
- 单个 Repository Adapter
- 单个 Controller
- 单个测试类
- 单个 bug 修复
- 单个 migration
- 单个 codegen template
- 单次 Review

## 5. 标准任务流程

```text
读取文档
  -> 输出自查
  -> 输出实现计划
  -> 修改代码
  -> 补充测试
  -> 运行测试
  -> 输出结果
  -> 输出风险点
```

## 6. 任务拆分原则

错误拆分：

```text
实现完整 IAM 平台
```

正确拆分：

```text
实现 iam_user 的 Entity/Mapper/Repository Adapter
实现 UserApplicationService 的 create/update/disable
实现 UserController 的 CRUD API
补充 UserApplicationServiceTest
补充 UserControllerTest
```

## 7. AI 修改前自查模板

每次修改前必须回答：

```text
1. 本次目标是什么？
2. 本次修改哪些目录？
3. 本次不修改哪些目录？
4. 是否涉及数据库？
5. 是否涉及权限？
6. 是否涉及租户？
7. 是否涉及审计？
8. 是否需要新增测试？
9. 是否存在兼容性风险？
10. 是否可能破坏已有接口？
```

## 8. AI 输出格式

任务完成后输出：

```text
结论：完成/部分完成/未完成

修改文件：
- ...

测试结果：
- 命令：...
- 结果：...

关键实现：
- ...

风险点：
- ...

后续建议：
- ...
```

## 9. Skills 机制

每类任务必须绑定 Skill：

| 任务 | Skill |
|---|---|
| 架构审查 | synapse-architecture-review |
| Java 后端 | synapse-java-backend |
| MyBatis-Plus | synapse-mybatis-plus-persistence |
| 权限认证 | synapse-security-rbac |
| Vue 后台 | synapse-vue-admin |
| 测试 | synapse-test-engineering |

## 10. 禁止 AI 自动行为

- 禁止自动升级核心依赖版本。
- 禁止自动引入新框架。
- 禁止删除设计文档。
- 禁止修改 migration 历史文件。
- 禁止删除测试。
- 禁止大范围格式化无关文件。
- 禁止把异常吞掉。
- 禁止为了通过测试绕过业务逻辑。

## 11. Review 优先级

Review 顺序：

1. 安全问题
2. 数据越权
3. 架构边界破坏
4. 事务一致性
5. 测试缺失
6. 可维护性
7. 性能问题
8. 代码风格

## 12. Token 节省原则

- 对标分析由人或本设计包提供，不让 Codex 重复分析。
- Codex 只读取本次相关文档。
- 大文档拆分，按任务读取。
- 每次任务只修改小范围文件。
- 对重复规则使用 Skills。
