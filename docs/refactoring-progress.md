# Synapse Framework 重构进度

本文件记录整体架构重构各阶段的状态。最终提交清单以 Git 历史为准。

## Phase 0：建立重构基线

- 状态：已完成
- 修改摘要：新增当前仓库事实基线、10 项架构决策记录和本进度文件。
- 测试结果：`mvn clean test`、`mvn clean verify`、`git diff --check` 均通过。
- Commit SHA：`e8e4077`
- 遗留问题：保留现有 deprecated API 编译告警，后续在对应代码阶段处理。

## Phase 1：调整 Maven 模块结构与 BOM

- 状态：已完成
- 修改摘要：新增 Web/OAuth2 聚合与共享 core 骨架；删除 cloud/file；将 mq 完整更名为 messaging；新增 observability/resilience 骨架；根 Parent 接管内部版本，BOM 移除 Alibaba 和已删除模块。
- 测试结果：`mvn -q validate`、`mvn clean verify` 通过，27 个 reactor project 全部成功。
- Commit SHA：`2d11aec`
- 遗留问题：新增骨架模块的运行时能力按后续阶段实现。

## Phase 2：重构 Security 当前主体上下文

- 状态：已完成
- 修改摘要：将 Synapse 自有 SecurityContext 类型族更名为 CurrentPrincipalContext 类型族；Servlet 使用可关闭 Scope，Reactive 使用 Reactor Context。
- 测试结果：相关模块测试、OAuth2 依赖边界检查、`mvn clean verify` 和 `git diff --check` 均通过。
- Commit SHA：`682bfff`
- 遗留问题：Reactive 主体门面位于 Resource Server WebFlux 适配模块。

## Phase 3：完成 Web 模块拆分和 JSON 修复

- 状态：已完成
- 修改摘要：将 Result、HTTP 状态解析器、traceId 基础规则和 Jackson 定制迁入 web-core；MVC/WebFlux 删除重复实现和全局 ObjectMapper Bean。
- 测试结果：三个 Web JAR 相关测试、web-core 禁止依赖检查、`mvn clean verify` 和 `git diff --check` 均通过。
- Commit SHA：`d5aa4db`
- 遗留问题：保留现有 WebFlux 测试中 Spring 待删除 API 的编译告警。

## Phase 4：重构 OAuth2

- 状态：已完成
- 修改摘要：统一 Resource Server 验证策略、主体/authority 映射和失败模型；实现协议中立的 Token Relay、Client Credentials、Authorized Client token store 与生命周期编排。
- 测试结果：OAuth2 六个 JAR 相关测试、Resource Server Core 边界检查、Configuration Metadata 检查、`mvn clean verify` 和 `git diff --check` 均通过。
- Commit SHA：`33eae8d`
- 遗留问题：内存 Authorized Client Store 仅适合单实例或测试。

## Phase 5：修复现有自动配置契约

- 状态：已完成
- 修改摘要：Cache 增加 Redis Bean 条件；Datasource 健康监控限定框架调度器；Reactive Resource Server 401/403 处理器按接口退让。
- 测试结果：相关契约测试、`mvn clean verify` 与 `git diff --check` 通过。
- Commit SHA：`e110ae4`
- 遗留问题：无。

## Phase 6：Datasource 边界校正

- 状态：已完成，已在 Phase 7.1 校正
- 原提交：`59480bd`
- 最终决策：底层直接使用 dynamic-datasource 官方配置、官方注解和上下文机制。Framework 只提供发现、描述、检测、健康、候选选择和治理决策。
- 已移除：`@UseDatasource`、自定义 RouteContext/Scope/Selector/Resolver、运行时 DatasourceDefinition Registry、自建 Advisor 与 AutoProxyCreator。
- 遗留问题：需要在可执行构建环境重新运行全量 Maven 验证。

## Phase 7：实现 Observability 和 Resilience

- 状态：已完成
- 修改摘要：Observability 新增稳定命名、低基数标签、trace context/MDC 和健康扩展约定；Resilience 基于 Resilience4j 实现 timeout/retry/circuit breaker/bulkhead 与 Observation 编排。
- 测试结果：原阶段低基数 tag、异常传播、幂等重试、超时、熔断、bulkhead 和自动配置测试通过。
- Commit SHA：`5c236b57c6d25b0ae5a187cee00ff3c2d844dac2`
- 遗留问题：Messaging/Audit 的实际链路观测在对应重构阶段接入。

## Phase 7.1：Phase 0～7 评审修复

- 状态：进行中
- 分支：`fix/phase-7-1-review-findings`
- 修改摘要：Access Token 字符串脱敏；OAuth2 Client 按 registration 隔离刷新；MVC/WebFlux clock-skew 空值语义统一并移除无效 resource-server fail-fast 属性；Observation 记录 Error；Resilience 拒绝同名不同配置；Datasource 删除自定义路由层并恢复官方 dynamic-datasource 边界。
- 测试结果：当前执行环境无法解析 GitHub/Maven 网络，等待远程 CI 或本地执行 `mvn clean verify`。
- Commit SHA：以分支最新提交为准。
- 遗留问题：完成远程 CI 后才能关闭本阶段。

## Phase 8：重构 Messaging

- 状态：已完成
- 修改摘要：保持单一 JAR，重建 Broker 中立 Envelope/Metadata/Destination、best-effort 与事务 Outbox 可靠发布、Handler Registry/Dispatcher、可靠性 SPI，以及可选 Spring Cloud Stream Transport；明确 At-least-once 和 eventId/messageId 幂等边界。
- 测试结果：Messaging 31 项测试通过；`mvn clean verify` 通过，27 个 reactor project 全部成功；`git diff --check` 和生产边界扫描通过。
- Commit SHA：本阶段独立提交（见 Git 历史）
- 遗留问题：Framework 不提供 Outbox、幂等和失败存储实现；应用需提供本地持久化实现及 Binder 配置。

## Phase 9：重构 Audit

- 状态：已完成
- 修改摘要：Audit 依赖 Messaging，新增 AuditPublisher、AuditFailurePolicy、AuditSanitizer、@Audited/AuditAspect；自动补齐 eventId、主体、租户、traceId 和 source service；普通审计 best-effort 失败继续，关键审计 reliable 失败传播。
- 测试结果：Audit 36 项测试通过；`mvn clean verify` 通过，27 个 reactor project 全部成功；依赖方向、敏感信息和生产边界扫描通过。
- Commit SHA：本阶段独立提交（见 Git 历史）
- 遗留问题：可靠审计依赖应用提供本地 OutboxStore 和活动本地事务；旧 AuditRecorder/AuditLogPort 仅作为兼容入口保留。

## Phase 10：补充事务和数据库迁移规范

- 状态：已完成
- 修改摘要：新增事务和数据库迁移规范，明确应用服务事务边界、本地事务 Outbox/Audit 约束、分布式一致性选择、Schema 所有权和服务级 Flyway 规则。
- 测试结果：`mvn clean verify` 和 `git diff --check` 通过。
- Commit SHA：本阶段独立提交（见 Git 历史）
- 遗留问题：规范由具体 Platform/Application 落地，Framework 不提供 Migration 或事务模块。

## Phase 11：建立自动配置契约测试标准

- 状态：未开始
- 修改摘要：待执行。
- 测试结果：未执行。
- Commit SHA：待提交
- 遗留问题：无。

## Phase 12：建立文档和架构一致性校验

- 状态：未开始
- 修改摘要：待执行。
- 测试结果：未执行。
- Commit SHA：待提交
- 遗留问题：无。

## Phase 13：发布工程与质量门禁

- 状态：未开始
- 修改摘要：待执行。
- 测试结果：未执行。
- Commit SHA：待提交
- 遗留问题：无。
