# Phase 3 Roadmap

本文档定义 Synapse Framework 第三阶段 Core Foundation 的任务顺序、修改范围和验收标准。

## 1. 任务总览

| Task | 名称 | 优先级 | 目标 |
| --- | --- | --- | --- |
| TASK-301 | Core Foundation 基线审计与契约冻结 | P0 | 固化现状、边界、核心契约和后续输入 |
| TASK-302 | WebMVC / WebFlux 契约强化 | P0 | 统一响应、异常、trace 和上下文行为 |
| TASK-303 | Security / OperationContext 生命周期强化 | P0 | 保证认证上下文适配、嵌套和清理正确 |
| TASK-304 | Data 依赖与审计填充强化 | P1 | 收敛生产依赖并固化填充契约 |
| TASK-305 | Cache 并发原语强化 | P1 | 固化缓存、锁、限流和幂等失败语义 |
| TASK-306 | Audit 输出与可追溯性强化 | P1 | 固化上下文补齐和多端口失败策略 |
| TASK-307 | 跨模块验证与阶段收口 | P0 | 全量测试、边界扫描、文档和发布准备 |

## 2. TASK-301：基线审计与契约冻结

### 目标

- 记录七个目标模块的当前事实。
- 固化第三阶段允许和禁止边界。
- 冻结跨模块核心契约。
- 明确 TASK-302 至 TASK-307 的输入和验收标准。

### 修改范围

- `docs/phase-3/*`。

### 不做

- 不修改 Java 公共 API。
- 不修改 POM。
- 不新增依赖或 module。
- 不顺手修复已识别问题。

### 验收

- 七个模块均有现状记录。
- 依赖事实与文档事实的差异已列出。
- 后续任务有明确边界和验收标准。
- Markdown 结构检查通过。

## 3. TASK-302：WebMVC / WebFlux 契约强化

### 目标

- 冻结两个 Web 技术栈的 Result 字段、错误码、HTTP 状态、时间格式和 trace 行为。
- 验证 WebMVC Filter 顺序和上下文逆序清理。
- 验证 WebFlux complete、error、cancel 路径。
- 消除文档中的历史阶段漂移。

### 重点检查

- Result 是否需要提取为无 Web 依赖的公共契约，或通过契约测试维持双实现。
- ObjectMapper 默认规则是否一致。
- 未认证、无权限、参数错误、404、405、415、未知异常响应是否一致。
- traceId header、MDC 或 Reactor Context、Result.traceId 是否一致。
- MvcOperationContextFilter 与 trusted-header filter 的顺序。

### 不做

- 不创建 Gateway 服务。
- 不创建业务 Controller。
- 不让 WebMVC 与 WebFlux 产生相互依赖。

### 验收

- WebMVC 和 WebFlux 契约测试通过。
- WebFlux 无 Servlet 或 MVC 依赖。
- Filter 和 Handler 异常均能返回统一结果。
- 上下文在正常、异常和取消路径无泄漏。

## 4. TASK-303：Security / OperationContext 生命周期强化

### 目标

- 固化 SecurityContext 与 OperationContext 的单向适配。
- 修复或证明嵌套 set/clear、重复 clear、异常路径和外层 scope 恢复行为。
- 冻结 trusted-header 认证与技术 carrier 解码的职责边界。

### 重点检查

- SecurityContext 连续设置不同用户。
- SecurityContext 嵌套在 Job 或 Async OperationContext scope 中。
- Filter chain 抛异常时的清理。
- PermissionChecker 显式入口与注解入口的一致性。
- timestamp tolerance、签名失败和空权限输入。

### 不做

- 不实现 IAM。
- 不引入完整 Spring Security Web 或 Config。
- 不实现 nonce 持久化平台。
- 不实现 ABAC 或 DataScope。

### 验收

- 外层 OperationContext 可被正确恢复。
- 线程复用无用户或权限泄漏。
- 认证失败和权限失败错误码稳定。
- data 和 audit 不依赖 security。

## 5. TASK-304：Data 依赖与审计填充强化

### 目标

- 核对 dynamic-datasource、Flyway 和 PostgreSQL Flyway 生产依赖的实际用途。
- 移除未使用或越界依赖，或将必要依赖调整为正确的可选边界。
- 固化审计字段填充和消费方覆盖行为。

### 重点检查

- createdAt、updatedAt、createdBy、updatedBy、deleted、version、tenantId。
- 显式值不被覆盖。
- 无 OperationContext 时不伪造 system。
- String ID 推荐规范与 MyBatis-Plus IdentifierGenerator 的关系。
- PostgreSQL、H2 和 Testcontainers 测试职责。

### 不做

- 不提供业务 BaseEntity 强制继承。
- 不提供业务 migration。
- 不实现多租户 SQL 拦截或 DataScope。

### 验收

- POM 依赖与模块定位一致。
- 自动配置加载和 Bean 覆盖测试通过。
- insert 和 update 边界测试通过。
- data 仍不依赖 security。

## 6. TASK-305：Cache 并发原语强化

### 目标

- 固化 L1/L2 缓存命中和失败语义。
- 强化 Redis 锁、限流和幂等的参数校验、异常语义和测试。
- 补齐真实 Redis 集成验证或明确测试替代方案。

### 重点检查

- loader null、codec failure、Redis unavailable、L1/L2 部分失败。
- 锁 owner、lease time、重入计数、非 owner 释放。
- 滑动窗口边界、时间回拨和唯一 member。
- IdempotencyGuard 的占位、TTL 和失败后策略说明。

### 不做

- 不实现 Redisson 全功能锁。
- 不实现业务限流规则。
- 不实现业务响应缓存型幂等平台。
- 不实现缓存管理后台。

### 验收

- 单元测试和 Redis 集成测试覆盖核心原子行为。
- 所有非法参数有确定行为。
- 文档不承诺自动续约、阻塞等待或业务结果存储。

## 7. TASK-306：Audit 输出与可追溯性强化

### 目标

- 固化 AuditEventContextEnricher 的优先级和字段保护规则。
- 明确显式 SYSTEM actor 与 UNKNOWN 或缺失主体的差异。
- 为 CompositeAuditLogPort 定义明确的失败策略。
- 避免 Noop 默认端口造成“已记录”的误解。

### 重点检查

- 显式 subject 或 traceId 与上下文补齐冲突。
- attributes 不覆盖和敏感信息边界。
- 单端口、多端口、端口异常和空端口。
- AuditRecorder 返回值或异常是否足以表达记录结果。

### 不做

- 不实现审计表、查询 API、报表、归档或保留周期。
- 不直接依赖 MQ 或数据库。

### 验收

- 不可追溯事件明确失败。
- 多端口失败行为有测试和文档。
- 消费方可以覆盖默认端口。
- audit 依赖方向保持单向。

## 8. TASK-307：跨模块验证与收口

### 目标

- 验证 HTTP 到 Security、OperationContext、Data、Audit 的主链路。
- 验证 Async 和 MQ 兼容链路。
- 执行全量 Maven、边界扫描和文档校准。
- 准备第三阶段发布说明。

### 验证

- 执行根工程 validate 和 clean test。
- 执行 Git diff 格式检查。
- 按 `04-acceptance-checklist.md` 完成启动类、Controller、持久化、模块依赖和历史措辞扫描。

### 验收

- 全量构建和测试通过。
- 无新增生产启动类、业务 Controller、业务持久化、starter 或 demo。
- 模块文档不再混用历史阶段与当前事实。
- 第三阶段风险项有明确关闭结论或后续版本记录。
