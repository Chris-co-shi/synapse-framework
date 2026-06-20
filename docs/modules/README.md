# 模块使用手册

本目录用于记录 Synapse Framework 各模块的使用方式、扩展点和边界说明。

对应 Agent Skill 索引见：[../../skills/README.md](../../skills/README.md)。

手册面向两类读者：

- 业务系统开发者：判断是否需要引入某个模块，以及如何正确使用。
- 平台系统开发者：判断 framework 提供了哪些底层契约，哪些能力应由平台服务自行实现。

## 当前事实与后续规划

当前正式 modules 以 root `pom.xml` reactor 为准。未进入 root `pom.xml` 的目录或文档规划，不视为已实现模块。

二阶段 TASK-205 已将 `synapse-time`、`synapse-config`、`synapse-i18n` 加入 reactor。
后续若文档出现新的规划模块，未进入 reactor 前不能当成当前可用能力。

固定约定：

- 本项目不提供 `synapse-starter-*` 聚合包。
- 本项目不提供 demo / example / sample application。
- 业务系统按需直接引用具体 module。
- 后续文档不得再把 starter 或 demo 作为正向规划。
- 身份认证只信任经过 Resource Server 验证的 Bearer Token；GatewayProof 只证明可信入口，不恢复身份 Header。

模块文档必须明确区分：

1. **当前事实**：当前代码已经存在、已经进入 reactor、已经可被消费方引用的能力。
2. **后续规划**：phase-2 roadmap 中规划但尚未实现的能力。
3. **禁止事项**：不得进入 framework 的业务能力或平台服务能力。
4. **Platform 边界**：应由 Synapse Platform 可启动服务承载的能力。

禁止把规划模块描述成已经实现的模块，禁止把 platform service 的职责写成 framework 模块能力。

## 当前模块

| 模块 | 手册 | 说明 |
| --- | --- | --- |
| `synapse-core` | [synapse-core.md](synapse-core.md) | 错误码、异常、ID、OperationContext 等核心契约 |
| `synapse-web-core` | [synapse-web-core.md](synapse-web-core.md) | 统一响应、错误映射、traceId 与 Jackson 定制 |
| `synapse-webmvc` | [synapse-webmvc.md](synapse-webmvc.md) | Servlet MVC 响应、异常处理、Filter 异常桥接 |
| `synapse-webflux` | [synapse-webflux.md](synapse-webflux.md) | WebFlux Trace、异常响应、Reactor Context / OperationContext 恢复 |
| `synapse-time` | [synapse-time.md](synapse-time.md) | 时间和时区技术支撑，不做时区后台 |
| `synapse-config` | [synapse-config.md](synapse-config.md) | 配置抽象、运行时读取和类型解析，不是配置中心 |
| `synapse-i18n` | [synapse-i18n.md](synapse-i18n.md) | 国际化消息解析抽象，不是资源中心 |
| `synapse-data` | [synapse-data.md](synapse-data.md) | ORM 无关的数据语义抽象 |
| `synapse-mybatis-plus` | [synapse-mybatis-plus.md](synapse-mybatis-plus.md) | MyBatis-Plus 工程增强 |
| `synapse-datasource` | [synapse-datasource.md](synapse-datasource.md) | 数据源治理、多数据源基础接入和路由治理抽象 |
| `synapse-cache` | [synapse-cache.md](synapse-cache.md) | 缓存、锁、限流、幂等基础设施 |
| `synapse-security` | [synapse-security.md](synapse-security.md) | Web 无关安全主体、权限检查和安全上下文 |
| `synapse-oauth2-core` | [synapse-oauth2-core.md](synapse-oauth2-core.md) | JWT claim、token、validator、denylist 和 BearerTokenProvider 契约 |
| `synapse-oauth2-authorization-server-support` | [synapse-oauth2-authorization-server-support.md](synapse-oauth2-authorization-server-support.md) | JWT 签发与 JWK 技术支持 |
| `synapse-oauth2-client` | [synapse-oauth2-client.md](synapse-oauth2-client.md) | Token Relay、Client Credentials 与 Token 生命周期 |
| `synapse-oauth2-resource-server-core` | [synapse-oauth2-resource-server-core.md](synapse-oauth2-resource-server-core.md) | Resource Server 共享验证与主体映射语义 |
| `synapse-oauth2-resource-server-webmvc` | [synapse-oauth2-resource-server-webmvc.md](synapse-oauth2-resource-server-webmvc.md) | Servlet OAuth2 Resource Server 技术适配 |
| `synapse-oauth2-resource-server-webflux` | [synapse-oauth2-resource-server-webflux.md](synapse-oauth2-resource-server-webflux.md) | Reactive OAuth2 Resource Server 技术适配 |
| `synapse-audit` | [synapse-audit.md](synapse-audit.md) | 审计模型、脱敏、失败策略和 Messaging 发布适配，不是审计中心 |
| `synapse-messaging` | [synapse-messaging.md](synapse-messaging.md) | Broker 中立消息模型、发布/消费编排、Stream 适配和可靠性 SPI，不是消息中心 |
| `synapse-observability` | [synapse-observability.md](synapse-observability.md) | Micrometer Observation 与低基数标签约定 |
| `synapse-resilience` | [synapse-resilience.md](synapse-resilience.md) | Resilience4j 超时、重试、熔断和隔离 |
| `synapse-bom` | [synapse-bom.md](synapse-bom.md) | 依赖版本管理 |

## 二阶段文档入口

二阶段模块边界以以下文档为准：

- [Framework Boundary](../phase-2/00-framework-boundary.md)
- [Module Boundary](../phase-2/01-module-boundary.md)
- [Phase 2 Roadmap](../phase-2/02-phase-2-roadmap.md)
- [Boundary Checklist](../phase-2/03-boundary-checklist.md)
- [Cloud Context Propagation（已删除模块的历史设计）](../phase-2/04-cloud-context-propagation.md)
- [GatewayProof 可信入口证明](../phase-2/05-gateway-proof.md)

## 迁移文档

- [数据模块边界迁移指南](../migration/data-module-boundary-migration.md)

## 手册编写规则

每个模块手册应包含：

1. 模块定位。
2. 当前事实。
3. 后续规划。
4. 适用场景。
5. 不适用场景。
6. Maven 引入方式。
7. 核心能力。
8. 最小使用片段。
9. 扩展方式。
10. 配置项。
11. 边界与注意事项。
12. 常见问题。

手册只描述当前代码事实，不写未实现能力，不把平台服务职责写成 framework 模块能力。

## Configuration Metadata 规则

所有公开 `@ConfigurationProperties` 必须生成 Spring Boot Configuration Metadata。

要求：

- 配置类和公开字段必须有清晰 Javadoc。
- 自动生成 metadata 优先，manual additional metadata 只补充 hints、候选值或自动生成无法表达的信息。
- module 发布前必须验证 jar 中包含 `META-INF/spring-configuration-metadata.json`。
- 新增配置项时必须同步检查 IDEA 补全信息，不得把内部实现参数随意公开成配置项。
