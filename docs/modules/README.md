# 模块使用手册

本目录用于记录 Synapse Framework 各模块的使用方式、扩展点和边界说明。

手册面向两类读者：

- 业务系统开发者：判断是否需要引入某个模块，以及如何正确使用。
- 平台系统开发者：判断 framework 提供了哪些底层契约，哪些能力应由平台服务自行实现。

## 当前事实与后续规划

当前正式 modules 以 root `pom.xml` reactor 为准。未进入 root `pom.xml` 的目录或文档规划，不视为已实现模块。

二阶段文档中的以下模块属于规划能力，未进入 reactor 前不能当成当前可用能力：

- `synapse-config`
- `synapse-i18n`
- `synapse-time`

固定约定：

- 本项目不提供 `synapse-starter-*` 聚合包。
- 本项目不提供 demo / example / sample application。
- 业务系统按需直接引用具体 module。
- 后续文档不得再把 starter 或 demo 作为正向规划。

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
| `synapse-webmvc` | [synapse-webmvc.md](synapse-webmvc.md) | Servlet MVC 响应、异常处理、Filter 异常桥接 |
| `synapse-webflux` | [synapse-webflux.md](synapse-webflux.md) | WebFlux Trace、异常响应、Reactor Context / OperationContext 恢复 |
| `synapse-cloud` | [synapse-cloud.md](synapse-cloud.md) | Spring Cloud / OpenFeign 服务间调用上下文传播，不是 Gateway |
| `synapse-data` | [synapse-data.md](synapse-data.md) | 数据层基础能力，当前聚焦 OperationContext 自动填充 |
| `synapse-cache` | [synapse-cache.md](synapse-cache.md) | 缓存、锁、限流、幂等基础设施 |
| `synapse-security` | [synapse-security.md](synapse-security.md) | trusted-header、AuthenticatedUser、PermissionChecker、权限注解适配 |
| `synapse-oauth2` | [synapse-oauth2.md](synapse-oauth2.md) | token、JWT、JWK、Resource Server 辅助能力，不是 IAM |
| `synapse-audit` | [synapse-audit.md](synapse-audit.md) | 审计事件契约，不是审计中心 |
| `synapse-file` | [synapse-file.md](synapse-file.md) | 文件存储抽象与本地轻量实现，不是文件中心 |
| `synapse-mq` | [synapse-mq.md](synapse-mq.md) | MQ 消息外壳、发布/消费模板、异常分类、上下文传播契约，不是消息中心 |
| `synapse-bom` | [synapse-bom.md](synapse-bom.md) | 依赖版本管理 |

## 二阶段文档入口

二阶段模块边界以以下文档为准：

- [Framework Boundary](../phase-2/00-framework-boundary.md)
- [Module Boundary](../phase-2/01-module-boundary.md)
- [Phase 2 Roadmap](../phase-2/02-phase-2-roadmap.md)
- [Boundary Checklist](../phase-2/03-boundary-checklist.md)
- [Cloud Context Propagation](../phase-2/04-cloud-context-propagation.md)

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
