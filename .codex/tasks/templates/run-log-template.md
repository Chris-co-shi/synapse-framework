# Run Log NNN - agent-name

## 1. Metadata

- Task ID:
- Run ID:
- Agent:
- Skills:
- Started At:
- Finished At:
- Branch:
- Commit:
- Status: success | failed | partial

## 2. Original Prompt

粘贴本次给 Codex / Agent 的核心任务。

注意：

- 不要粘贴密钥、Token、密码、私有凭证。
- 不要粘贴无关大段日志。
- 保留任务目标、范围、约束和验收标准。

```text

```

## 3. Required Reading Confirmed

本次已读取：

- [ ] AGENTS.md
- [ ] docs/00-positioning.md
- [ ] docs/01-architecture.md
- [ ] docs/02-module-boundary.md
- [ ] docs/03-package-rules.md
- [ ] docs/04-database-rules.md
- [ ] docs/05-api-rules.md
- [ ] docs/06-security-rules.md
- [ ] docs/07-test-rules.md
- [ ] skills/synapse-architecture-review/SKILL.md
- [ ] skills/synapse-java-backend/SKILL.md
- [ ] skills/synapse-mybatis-plus-persistence/SKILL.md
- [ ] skills/synapse-security-rbac/SKILL.md
- [ ] skills/synapse-vue-admin/SKILL.md
- [ ] skills/synapse-test-engineering/SKILL.md

## 4. Codex / Agent Plan

执行前计划：

1. 
2. 
3. 

## 5. Files Changed

新增：

- 

修改：

- 

删除：

- 

## 6. Key Decisions

记录本次实现中的关键取舍：

1. 
2. 
3. 

## 7. Implementation Notes

记录关键实现说明：

- 

## 8. Tests

### 8.1 Commands

```bash

```

### 8.2 Results

```text
Tests run:
Failures:
Errors:
Skipped:
```

### 8.3 Failed Tests

如果有失败测试，记录失败原因：

- 

## 9. Architecture Self-Check

- [ ] Controller 未直接依赖 Mapper
- [ ] Controller 未直接返回 Entity
- [ ] Domain 未依赖 MyBatis-Plus
- [ ] 未使用 IService / ServiceImpl 作为业务分层
- [ ] Repository Port / Adapter 边界清晰
- [ ] 未修改无关模块
- [ ] 未直接复制开源项目代码

说明：

- 

## 10. Security Self-Check

- [ ] 权限判断不只依赖前端菜单
- [ ] Token 操作校验归属关系
- [ ] Refresh Token rotation 具备原子性
- [ ] 数据权限未只放在 Controller 层
- [ ] 租户 ID 未由前端传入后直接信任
- [ ] 安全失败返回明确错误码

说明：

- 

## 11. Review Notes

### 11.1 Blocking

- 

### 11.2 High

- 

### 11.3 Medium

- 

### 11.4 Low

- 

## 12. Known Risks

1. 
2. 

## 13. Follow-up Tasks

- [ ] 
- [ ]
