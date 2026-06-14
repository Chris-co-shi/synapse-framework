# 模块使用手册

本目录用于记录 Synapse Framework 各模块的使用方式、扩展点和边界说明。

手册面向两类读者：

- 业务系统开发者：判断是否需要引入某个模块，以及如何正确使用。
- 平台系统开发者：判断 framework 提供了哪些底层契约，哪些能力应由平台服务自行实现。

## 一阶段模块

| 模块 | 手册 | 说明 |
| --- | --- | --- |
| `synapse-core` | [synapse-core.md](synapse-core.md) | 错误码、异常、ID、OperationContext 等核心契约 |
| `synapse-web` | [synapse-web.md](synapse-web.md) | Servlet MVC 响应、异常处理、Filter 异常桥接 |
| `synapse-data` | [synapse-data.md](synapse-data.md) | 数据层基础能力，当前聚焦 OperationContext 自动填充 |
| `synapse-cache` | [synapse-cache.md](synapse-cache.md) | 缓存、锁、限流、幂等基础设施 |
| `synapse-security` | [synapse-security.md](synapse-security.md) | trusted-header、AuthenticatedUser、PermissionChecker、权限注解适配 |
| `synapse-oauth2` | [synapse-oauth2.md](synapse-oauth2.md) | token、JWT、JWK 基础能力 |
| `synapse-audit` | [synapse-audit.md](synapse-audit.md) | 审计事件契约 |
| `synapse-file` | 待补充 | 文件存储抽象与本地轻量实现 |
| `synapse-message` | 待补充 | 消息头、上下文传播、发送 SPI、交互追踪契约 |
| `synapse-bom` | 待补充 | 依赖版本管理 |

## 手册编写规则

每个模块手册应包含：

1. 模块定位。
2. 适用场景。
3. 不适用场景。
4. Maven 引入方式。
5. 核心能力。
6. 快速使用示例。
7. 扩展方式。
8. 配置项。
9. 边界与注意事项。
10. 常见问题。

手册只描述当前代码事实，不写未实现能力，不把平台服务职责写成 framework 模块能力。
