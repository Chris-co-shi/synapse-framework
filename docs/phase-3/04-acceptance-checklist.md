# Phase 3 Acceptance Checklist

> 历史说明：本文是 Phase 3 验收清单，当前架构验收由整体重构进度与 ADR 接管。

本清单用于第三阶段每个任务开始前、提交前和阶段收口时检查。

## 1. 任务边界

- [ ] 已说明本次目标和修改范围。
- [ ] 已说明明确不做内容。
- [ ] 已确认是否修改 POM、新增依赖或新增 module。
- [ ] 未新增可启动平台服务。
- [ ] 未新增业务 Controller、Service、Entity、Mapper、Repository。
- [ ] 未新增业务 migration。
- [ ] 未新增 starter、demo、example、sample application。

## 2. 模块依赖

- [ ] `synapse-core` 不依赖其他 `synapse-*` 模块。
- [ ] `synapse-webmvc` 与 `synapse-webflux` 不互相依赖。
- [ ] `synapse-webflux` 不依赖 `spring-webmvc` 或 `jakarta.servlet`。
- [ ] `synapse-data` 不依赖 `synapse-security`。
- [ ] `synapse-audit` 不依赖 `synapse-data`、`synapse-cache` 或 `synapse-messaging`。
- [ ] 新增三方依赖有必要性和替代方案说明。
- [ ] POM 依赖与模块文档定位一致。

## 3. OperationContext

- [ ] 缺少 actor 时未自动伪造 system。
- [ ] actor 与 initiator 语义未混淆。
- [ ] source 能识别当前技术入口。
- [ ] traceId 和 requestId 传播规则稳定。
- [ ] scope 正常结束后恢复旧上下文。
- [ ] scope 异常结束后恢复旧上下文。
- [ ] worker thread 复用后无上下文泄漏。
- [ ] carrier 缺少关键字段时不恢复半完整上下文。

## 4. WebMVC

- [ ] Filter 阶段异常返回统一 JSON。
- [ ] MVC 阶段异常返回统一 JSON。
- [ ] 400、401、403、404、405、415、500 映射有测试。
- [ ] traceId header、MDC、Result.traceId 一致。
- [ ] TraceContext、RequestContext、CurrentPrincipalContext、OperationContext 按正确顺序清理。
- [ ] 消费方自定义 Bean 不被默认自动配置覆盖。

## 5. WebFlux

- [ ] Reactor Context 是主传播通道。
- [ ] complete 路径无泄漏。
- [ ] error 路径无泄漏。
- [ ] cancel 路径无泄漏。
- [ ] Result 和错误语义与 WebMVC 兼容。
- [ ] 未引入 Servlet 或 MVC 类型。

## 6. Security

- [ ] OAuth2 适配模块负责 Token 验证，`synapse-security` 不恢复身份 Header 认证协议。
- [ ] 未把 roles 或 permissions 快照当作权威数据源。
- [ ] CurrentPrincipalContext set 和 clear 能恢复外层 OperationContext。
- [ ] 连续设置不同用户不会遗留旧 scope。
- [ ] 认证失败和权限失败错误码稳定。
- [ ] `@RequirePermission` 未被描述为唯一安全边界。

## 7. Data

- [ ] insert 只填充未显式赋值字段。
- [ ] update 刷新 updatedAt。
- [ ] 有 actor 时填充 updatedBy。
- [ ] 无 actor 时不填充固定 system。
- [ ] tenantId 仅作为承载位，不暗示多租户隔离已实现。
- [ ] dynamic-datasource 和 Flyway 依赖用途已核实。
- [ ] ID 示例和项目 String ID 规范不冲突。

## 8. Cache

- [ ] CacheClient 的 null、miss、codec failure 行为明确。
- [ ] L1 和 L2 部分失败行为明确。
- [ ] 锁 owner 和 lease time 参数校验完整。
- [ ] 非 owner 释放行为有测试。
- [ ] 当前未承诺自动续约或阻塞等待。
- [ ] 限流窗口边界有测试。
- [ ] IdempotencyGuard 未被描述为业务执行结果存储。

## 9. Audit

- [ ] 显式事件字段优先于上下文补齐。
- [ ] attributes 不覆盖调用方已有值。
- [ ] 缺少 subject 或 traceId 时明确失败。
- [ ] 未把 UNKNOWN 或缺失主体自动写成 system。
- [ ] 显式 System actor 仍包含可追溯身份和来源。
- [ ] CompositeAuditLogPort 失败策略有测试和文档。
- [ ] Noop 默认端口的语义清晰。

## 10. 自动配置

- [ ] 自动配置 imports 正确。
- [ ] 缺少可选依赖时不会错误装配。
- [ ] 消费方提供 Bean 时默认 Bean back off。
- [ ] Properties 默认值和非法值有测试。
- [ ] 自动配置不创建业务组件。

## 11. 验证要求

代码任务至少执行：

```bash
mvn -q validate
mvn -q test
git diff --check
```

影响多个模块或公共契约时执行：

```bash
mvn -q clean test
```

还必须完成以下边界扫描：

- 生产启动类和启动入口。
- 生产 Controller 和请求映射。
- 业务 Entity、Mapper、Service 与建表语句。
- starter、demo、example、sample application。
- WebFlux 对 MVC 或 Servlet 的错误依赖。
- Data 或 Audit 对 Security 的错误依赖。

命中不一定违规，但必须分类为禁止项说明、历史说明、检查命令、测试 fixture 或真实生产代码问题。

## 12. TASK-301 特殊验收

- [ ] 只新增 `docs/phase-3` 文档。
- [ ] 未修改 Java 或 POM。
- [ ] 七个目标模块均有现状记录。
- [ ] 已记录 Data 生产依赖与文档边界不一致问题。
- [ ] 已记录 WebMVC 和 WebFlux 双实现一致性问题。
- [ ] 已记录 CurrentPrincipalContext 生命周期风险。
- [ ] 已记录 Cache 和 Audit 失败语义风险。
- [ ] TASK-302 至 TASK-307 均有明确验收标准。
