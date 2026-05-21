---
name: synapse-audit
description: Synapse Audit 基础能力最佳实践。Use when Codex implements or reviews synapse-audit code involving audit events, operation log annotations, login/security event recording, AuditLogPort, audit persistence adapters, or audit tests.
---

# Synapse Audit

## 必读

- `AGENTS.md`
- `docs/01-architecture.md`
- `docs/02-module-boundary.md`
- `docs/07-test-rules.md`
- `docs/10-technical-foundation-baseline.md`
- `skills/synapse-common/SKILL.md`
- `skills/synapse-audit/SKILL.md`

## 职责和边界

- 提供审计事件模型。
- 提供操作日志注解。
- 提供登录日志、安全事件、审计持久化 Port。
- 提供审计上下文。
- 提供 AuditRecorder 自动配置。
- 不直接依赖具体 IAM 业务模型。
- 审计失败不能吞掉关键安全事件。

## 推荐包结构

```text
com.indigo.synapse.audit
├── event
├── context
├── annotation
├── port
├── recorder
└── autoconfigure
```

## 标准实现模式

- 审计事件必须包含行为、主体、目标、时间、结果、traceId。
- `AuditEvent` 必须包含 action、subject、target、occurredAt、outcome、traceId、message、attributes。
- `AuditEvent.builder()` 可用于标准构建，但最终仍必须走 `AuditEvent` 构造校验和敏感字段脱敏。
- `AuditSubject` 必须包含 subjectType、subjectId，可携带 tenantId 作为租户预留。
- `AuditTarget` 必须包含 targetType、targetId，禁止用模糊文本替代目标身份。
- `AuditOutcome` 只表达 SUCCESS / FAILURE，复杂失败原因放 message 或 attributes。
- `AuditContext` 使用 ThreadLocal 保存当前审计 subject 和 traceId，请求结束或测试结束必须 clear。
- 临时切换审计上下文必须使用 `AuditContext.scope(...)` 和 try-with-resources，保证恢复上一层上下文。
- 持久化通过 Port 扩展，具体落库实现放 adapter。
- `AuditLogPort` 是唯一持久化扩展点，audit 基座不直接依赖 data/admin/security 业务模型。
- 无持久化实现时，自动配置使用 `NoopAuditLogPort`，用于保证接入应用可启动；生产系统必须提供真实 Port。
- 多个 `AuditLogPort` 同时存在时，`AuditRecorder` 使用 `CompositeAuditLogPort` 按顺序全部写入。
- `AuditRecorder` 不吞掉 `AuditLogPort` 异常；关键安全事件持久化失败必须暴露给调用方或上层策略处理。
- 敏感字段必须脱敏。
- `SensitiveAuditValueMasker` 必须遮蔽 password、token、secret、salt、key 等属性值。
- `AuditOperation` 注解只声明 action 和 targetType，不直接执行业务逻辑或访问 Mapper。
- `SynapseAuditAutoConfiguration` 负责注册默认 `AuditLogPort` 和 `AuditRecorder`。
- 自动配置必须写入 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。

## 允许技术和禁止事项

允许：

- Spring Boot AutoConfiguration。
- Spring `ObjectProvider` 收集多个审计 Port。
- ThreadLocal 审计上下文。
- Noop Port 作为开发期默认兜底。
- Composite Port 用于多落点审计。

禁止：

- 在 `synapse-audit` 直接依赖 IAM Entity、Mapper 或业务 Service。
- 在基座中直接落库。
- 在 `AuditRecorder` 中吞掉 Port 异常。
- 审计 attributes 记录明文密码、token、secret、salt、key。
- 用接口路径或中文描述替代 `targetType/targetId`。
- 忘记清理 `AuditContext`。

## 测试要求

- 覆盖审计事件构建、成功记录、失败记录、脱敏。
- 覆盖 `AuditContext` 设置、清理、scope 恢复。
- 覆盖 `NoopAuditLogPort` 和 `CompositeAuditLogPort`。
- 覆盖自动配置默认 Noop Port、Recorder、多 Port 组合。
- 涉及登录、授权、权限变更的任务必须验证审计写入。
- 注解必须覆盖运行时可见性。
- Recorder 必须覆盖正常记录、Port 异常透传、空事件拒绝。
- 模块完成后先运行 `/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn -pl synapse-audit -am test`，关键变更再运行根目录 `clean test`。

模块完成标准：

- 生产代码完成。
- 模块级测试通过。
- 根目录 `/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn clean test` 通过。
- `skills/synapse-audit/SKILL.md` 已更新为最佳实践。
- `SKILL.md` 不写过程日志。

## 常见错误

- 只打印日志不持久化关键审计事件。
- 审计记录包含明文 token、密码或密钥。
- 审计模块直接依赖 admin-api。
- 在 `AuditRecorder` 中 catch 后只打印日志，导致关键审计失败不可见。
- 审计目标只写中文描述或接口路径，缺少 targetType/targetId。
- 把 IAM 用户、角色、菜单 Entity 直接放进审计事件。
- 自动配置没有 `AutoConfiguration.imports`，导致接入应用不生效。
- 生产环境依赖 `NoopAuditLogPort` 导致关键安全事件没有持久化。
- 多 Port 场景只写入第一个 Port，导致审计落点缺失。

## 示例任务拆分

- 定义 AuditEvent。
- 定义 AuditLogPort。
- 实现操作日志注解和单元测试。
- 实现敏感字段脱敏规则。
- 实现 AuditRecorder 并验证异常策略。
- 实现 AuditContext 和 scope 恢复。
- 实现 Noop/Composite AuditLogPort。
- 实现 Audit 自动配置。
- 后续实现基于 AOP 的 `AuditOperation` 拦截器。
- 后续实现基于 data 模块的审计落库 adapter。
