# Synapse Framework 重构进度

本文件记录整体架构重构各阶段的状态。Commit SHA 在阶段提交完成后的下一次更新中
回填；最终提交清单以 Git 历史为准。

## Phase 0：建立重构基线

- 状态：已完成
- 修改摘要：新增当前仓库事实基线、10 项架构决策记录和本进度文件。
- 测试结果：`mvn clean test`、`mvn clean verify`、`git diff --check` 均通过。
- Commit SHA：`e8e4077`
- 遗留问题：保留现有 deprecated API 编译告警，后续在对应代码阶段处理。

## Phase 1：调整 Maven 模块结构与 BOM

- 状态：已完成
- 修改摘要：新增 Web/OAuth2 聚合与共享 core 骨架；删除 cloud/file；将 mq 完整更名为 messaging；
  新增 observability/resilience 骨架；根 Parent 接管内部版本，BOM 移除 Alibaba 和已删除模块。
- 测试结果：`mvn -q validate`、`mvn clean verify` 通过，27 个 reactor project 全部成功。
- Commit SHA：`2d11aec`
- 遗留问题：新增骨架模块的运行时能力按后续阶段实现；历史 phase 文档保留旧模块背景并已标注历史状态。

## Phase 2：重构 Security 当前主体上下文

- 状态：已完成
- 修改摘要：将 Synapse 自有 SecurityContext 类型族更名为 CurrentPrincipalContext 类型族；
  Servlet 适配器使用严格可关闭 Scope，Reactive 适配器使用 Reactor Context，并补充并发和线程切换测试。
- 测试结果：相关模块测试、OAuth2 依赖边界检查、`mvn clean verify` 和 `git diff --check` 均通过，
  27 个 reactor project 全部成功。
- Commit SHA：`682bfff`
- 遗留问题：保留 Spring Security 自身的 SecurityContext/Holder 术语；Reactive 主体门面当前位于
  Resource Server WebFlux 适配模块，后续 Web/OAuth2 拆分阶段继续评估共享位置。

## Phase 3：完成 Web 模块拆分和 JSON 修复

- 状态：已完成
- 修改摘要：将 Result、HTTP 状态解析器、traceId 基础规则和 Jackson 定制迁入 web-core；
  MVC/WebFlux 删除重复实现和全局 ObjectMapper Bean，改为复用 Boot Jackson 构建链。
- 测试结果：三个 Web JAR 相关测试、web-core 禁止依赖检查、`mvn clean verify` 和
  `git diff --check` 均通过，27 个 reactor project 全部成功。
- Commit SHA：`d5aa4db`
- 遗留问题：保留现有 WebFlux 测试中 Spring 待删除 API 的编译告警，后续自动配置契约阶段处理。

## Phase 4：重构 OAuth2

- 状态：已完成
- 修改摘要：统一 Resource Server 验证策略、主体/authority 映射和失败模型；实现协议中立的
  Token Relay、Client Credentials、Authorized Client token store 与生命周期编排。
- 测试结果：OAuth2 六个 JAR 相关测试、Resource Server Core 技术栈边界检查、OAuth2 Client
  主体上下文隔离检查、Configuration Metadata 检查、`mvn clean verify` 和 `git diff --check`
  均通过，27 个 reactor project 全部成功。
- Commit SHA：`33eae8d`
- 遗留问题：内存 Authorized Client Store 仅适合单实例或测试，生产集群需由消费方提供安全的
  持久化实现；具体 token endpoint 和 HTTP 客户端适配由应用按技术栈实现。

## Phase 5：修复现有自动配置契约

- 状态：已完成
- 修改摘要：Cache 增加 StringRedisTemplate Bean 条件；Datasource 健康监控限定框架调度器并按
  监控器类型退让；Reactive Resource Server 的 401/403 处理器按 Spring Security 接口退让。
- 测试结果：Cache 缺失 Redis Bean/用户 Bean 覆盖、Datasource 多调度器、Reactive 安全处理器
  接口退让测试通过；全仓条件注解完成扫描；`mvn clean verify` 与 `git diff --check` 通过，
  27 个 reactor project 全部成功。
- Commit SHA：`e110ae4`
- 遗留问题：无。

## Phase 6：完善 Datasource

- 状态：已完成
- 修改摘要：新增不含明文凭据的数据源定义/Provider/Registry、凭据解析端口、显式路由
  Context/Scope、`@UseDatasource`、可排序 Resolver 与事务内切换保护；复用 dynamic-datasource 官方上下文栈。
- 测试结果：Provider 顺序/重复定义、路由四级优先级、Scope 嵌套恢复与清理、事务内切换拒绝、
  注解拦截测试通过；dynamic-datasource/Seata/MyBatis 边界扫描、`mvn clean verify` 和
  `git diff --check` 通过，27 个 reactor project 全部成功。
- Commit SHA：`59480bd`
- 遗留问题：Micrometer Observation 接入按 Phase 7 统一实现，避免 Datasource 单独绑定观测实现。

## Phase 7：实现 Observability 和 Resilience

- 状态：已完成
- 修改摘要：Observability 新增稳定命名、低基数标签、trace context/MDC 和健康扩展约定；
  Resilience 基于 Resilience4j 实现保守 timeout/retry/circuit breaker/bulkhead 与 Observation 编排。
- 测试结果：低基数 tag、异常传播、幂等重试、非重试异常、超时、熔断、bulkhead、自动配置
  退让与多 Executor 测试通过；APM/Sentinel/敏感标签边界扫描、`mvn clean verify` 和
  `git diff --check` 通过，27 个 reactor project 全部成功。
- Commit SHA：待提交
- 遗留问题：Messaging/Audit 的实际链路观测在其对应重构阶段接入统一 Operations。

## Phase 8：重构 Messaging

- 状态：未开始
- 修改摘要：待执行。
- 测试结果：未执行。
- Commit SHA：待提交
- 遗留问题：无。

## Phase 9：重构 Audit

- 状态：未开始
- 修改摘要：待执行。
- 测试结果：未执行。
- Commit SHA：待提交
- 遗留问题：无。

## Phase 10：补充事务和数据库迁移规范

- 状态：未开始
- 修改摘要：待执行。
- 测试结果：未执行。
- Commit SHA：待提交
- 遗留问题：无。

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
