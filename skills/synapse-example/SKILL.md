---
name: synapse-example
description: Synapse Example 示例应用最佳实践。Use when Codex implements or reviews synapse-example code that demonstrates starter integration, multi-datasource usage, Redis lock/rate-limit usage, OAuth2 flows, audit events, or foundation module examples.
---

# Synapse Example

## 必读

- `AGENTS.md`
- `docs/10-technical-foundation-baseline.md`
- `skills/synapse-starter/SKILL.md`
- 相关能力模块的 `SKILL.md`

## 职责和边界

- 验证技术基座可以被业务应用接入。
- 展示 starter、Web、Data、Cache、Security、Audit 的最小用法。
- 不承载真实业务系统。
- 不绕过基础模块直接实现框架能力。
- 不连接开发者本机数据库、Redis、OAuth2 Server 或远程服务。
- 不为了示例启动而补齐完整外部基础设施配置。

## 推荐包结构

```text
com.indigo.synapse.example
├── interfaces
├── application
├── infrastructure
├── foundation
└── config
```

## 标准实现模式

- 示例必须足够小，只证明基座能力可用。
- 第一层 Example Foundation 只依赖 `synapse-starter`，通过 starter 的传递能力演示基础契约。
- 示例可以演示 `ApiResponse`、`DataSourceContext`、`DatabaseDialectResolver`、`CacheKey`、`RedisReentrantLock`、`SlidingWindowRateLimiter`、`OAuth2PublicEndpointPolicy`、`JwtClaims`、`JwkKeyDescriptor`、`LoginUser`、`AuditRecorder`、`SynapseAutoConfigurationPlan` 等公开契约。
- 示例不得在 plan 层或单元测试中连接数据库、Redis、OAuth2 Server 或本机外部服务。
- Redis 锁/限流示例可以使用最小 `RedisScriptExecutor` 测试替身演示调用语义，但不得复制 Lua 实现。
- Data 示例优先演示数据源上下文切换和方言解析，不在 Example 第一层创建表、Mapper 或 Repository。
- Security 示例优先演示 OAuth2 公开端点策略、JWT claims、JWK 描述和当前用户权限摘要，不做 IAM 登录、refresh token rotation 或 RBAC 业务。
- Web 示例优先演示统一响应和 trace 契约，不要求启动 Servlet 容器。
- Audit 示例必须走 `AuditRecorder`，并验证敏感属性脱敏。
- 审计示例属性名如果包含 `token`、`secret`、`key` 等敏感关键字会被脱敏；需要断言明文示例值时，必须选择非敏感属性名。
- 每个示例能力必须有对应测试。
- 示例配置不得包含生产密钥、真实账号或本机私有路径。
- 示例应用上下文测试应验证 starter 可接入和 feature switch 可生效。
- 当示例应用没有配置真实外部基础设施时，测试上下文必须关闭对应 Synapse feature。
- Starter feature switch 必须负责同步关闭相关三方外部自动配置，例如 dynamic-datasource、Flyway、DataSource、Redis；Example 测试不应长期手写这些 exclude。

## 允许使用的技术和禁止事项

- 允许使用 Spring Boot `@SpringBootConfiguration`、`@EnableAutoConfiguration` 和 `ApplicationContextRunner` 验证接入。
- 允许新增测试依赖 `spring-boot-test`、`assertj-core` 支撑上下文测试。
- 允许通过 starter 传递访问基础模块公开 API。
- 禁止在 `synapse-example` 直接依赖 common/web/data/cache/security/audit 模块，避免绕过 starter。
- 禁止在示例中实现业务 Controller、业务 Entity、Mapper、Repository Adapter。
- 禁止在示例测试中访问真实数据库、Redis、OAuth2 Server 或本机路径。
- 禁止把示例写成完整后台管理系统或 IAM 模块。
- 禁止把基础模块内部实现复制到 Example；只能调用公开 API。

## 测试要求

- 第一层示例必须验证模块依赖 `synapse-starter`。
- 必须验证 starter 默认计划、cache key、安全公开端点策略、权限摘要、审计脱敏可被示例接入。
- 验证应用上下文可启动。
- 验证 starter 自动配置生效。
- 验证 starter feature switch 能关闭 Web/Data/Cache/Security 等需要环境配合的能力，并且不需要额外手写三方 auto-configuration exclude。
- 验证 Data 数据源上下文切换和数据库方言解析。
- 验证 Redis 锁和限流的最小调用链路，测试替身只能返回脚本语义结果，不复制 Lua。
- 验证 OAuth2 公开端点、JWT claims、JWK 描述、权限摘要。
- 验证审计事件写入和敏感字段脱敏。
- 模块完成后先运行 `/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn -pl synapse-example -am test`，关键变更再运行根目录 `clean test`。
- TASK-008 完成后必须生成或更新 `skills/synapse-example/SKILL.md`。

## 常见错误

- 把示例应用做成完整后台管理系统。
- 在示例里复制框架模块内部实现。
- 示例配置依赖开发者本机环境。
- 示例直接依赖 common/web/data/cache/security/audit，绕过 starter 接入路径。
- 示例测试连接真实数据库、Redis 或远程 OAuth2 服务。
- 使用 `cacheKey`、`secretKey` 这类属性名断言审计明文值，导致被安全脱敏。
- 示例应用上下文只关闭 Synapse feature，却因 Starter 未同步过滤外部自动配置导致没有真实数据源或 Redis 时启动失败。
- 为了让上下文启动而把 Data/Cache/Security 的默认规则放宽。
- 在 Example 中复制 Lua 脚本或 JWT 签发实现。

## 示例任务拆分

- 创建最小 Maven 示例模块。
- 增加 starter 纯 Java 契约示例。
- 创建最小 Spring Boot 示例应用。
- 增加 Redis 锁和限流示例接口。
- 增加 OAuth2 资源服务器保护接口示例。
- 增加审计事件写入示例。
- 增加 feature switch 与外部自动配置过滤测试。
- 更新 `skills/synapse-example/SKILL.md`。
