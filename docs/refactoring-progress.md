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
- Commit SHA：待提交
- 遗留问题：保留现有 WebFlux 测试中 Spring 待删除 API 的编译告警，后续自动配置契约阶段处理。

## Phase 4：重构 OAuth2

- 状态：未开始
- 修改摘要：待执行。
- 测试结果：未执行。
- Commit SHA：待提交
- 遗留问题：无。

## Phase 5：修复现有自动配置契约

- 状态：未开始
- 修改摘要：待执行。
- 测试结果：未执行。
- Commit SHA：待提交
- 遗留问题：无。

## Phase 6：完善 Datasource

- 状态：未开始
- 修改摘要：待执行。
- 测试结果：未执行。
- Commit SHA：待提交
- 遗留问题：无。

## Phase 7：实现 Observability 和 Resilience

- 状态：未开始
- 修改摘要：待执行。
- 测试结果：未执行。
- Commit SHA：待提交
- 遗留问题：无。

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
