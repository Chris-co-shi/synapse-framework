---
name: synapse-starter
description: Synapse Starter 自动配置最佳实践。Use when Codex implements or reviews synapse-starter code involving Spring Boot auto-configuration, conditional beans, configuration properties, starter dependencies, module aggregation, or example app integration.
---

# Synapse Starter

## 必读

- `AGENTS.md`
- `docs/01-architecture.md`
- `docs/02-module-boundary.md`
- `docs/08-ai-development-rules.md`
- `docs/10-technical-foundation-baseline.md`

## 职责和边界

- 作为技术基座统一接入入口。
- 聚合 common、web、data、cache、security、audit。
- 提供自动配置、条件装配、默认配置、模块能力计划和统一禁用开关。
- 不放业务 Controller、业务表或 IAM 业务逻辑。
- 不直接创建数据库、Redis、OAuth2 服务等外部基础设施连接。

## 推荐包结构

```text
com.indigo.synapse.starter
├── autoconfigure
├── properties
└── condition
```

资源文件固定放在：

```text
src/main/resources/META-INF/spring/
└── org.springframework.boot.autoconfigure.AutoConfiguration.imports
src/main/resources/META-INF/
└── spring.factories
```

## 标准实现模式

- 第一层 Starter Foundation 必须先定义纯 Java 配置契约，再接 Spring Boot `AutoConfiguration`。
- Starter 自身自动配置类使用 `@AutoConfiguration`，并通过 `AutoConfiguration.imports` 注册。
- Starter 开关绑定到 `SynapseBootProperties`，统一前缀是 `synapse`。
- 自动配置必须可通过 `synapse.<feature>.enabled=false` 关闭。
- `SynapseFeature` 定义 starter 可控能力：WEB、DATA、CACHE、SECURITY、AUDIT。
- `SynapseFeature` 必须维护模块名、默认开启状态、是否依赖外部基础设施、受同一 feature switch 控制的有序自动配置类名列表。
- `SynapseStarterProperties.defaults()` 默认启用第一阶段基座能力；显式关闭必须返回新配置对象，不修改原对象。
- `SynapseBootProperties.toStarterProperties()` 负责把 Spring Boot 配置绑定结果转换为纯 Java 配置契约。
- `SynapseAutoConfigurationPlan` 只描述应启用哪些模块，不直接创建外部连接。
- `SynapseAutoConfigurationImportFilter` 通过 `spring.factories` 注册，按自动配置类名识别 Synapse 模块和相关外部自动配置并应用开关。
- `synapse.web.enabled=false` 必须同时关闭 Web Base、MVC、WebFlux 自动配置。
- `synapse.data.enabled=false` 必须同时关闭 Synapse Data、dynamic-datasource、Spring Boot DataSource、JdbcTemplate、DataSourceTransactionManager、Flyway 自动配置。
- `synapse.cache.enabled=false` 必须同时关闭 Synapse Cache、Redis、Reactive Redis、Redis Repositories 自动配置。
- Example 或业务应用没有配置真实数据库、Redis 时，优先通过 feature switch 关闭对应能力，不应在测试里手写大量三方 auto-configuration exclude。
- 各基础模块必须各自提供 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，Starter 只负责统一过滤，不替模块隐藏注册缺失问题。
- 条件装配避免强制启用未配置基础设施。
- DATA、CACHE 标记为需要外部基础设施，但 plan 层永远不主动连接数据库或 Redis。
- starter 不直接实现业务能力，只组合模块能力。
- 后续新增基础模块时，必须同步更新 `SynapseFeature`、Boot properties、ImportFilter 测试和本 Skill。

## 允许使用的技术和禁止事项

- 允许使用 Spring Boot `@AutoConfiguration`、`@EnableConfigurationProperties`、`AutoConfigurationImportFilter`、`EnvironmentAware`。
- 允许使用纯 Java plan/properties 对 Starter 行为建模，便于无 Spring 环境单元测试。
- 禁止在 Starter 中定义业务 Bean、业务 Controller、业务表 Entity、IAM 业务逻辑。
- 禁止在 Starter 自动配置中无条件访问数据库、Redis、JWK 文件、远程服务。
- 禁止用 Starter 绕过各模块自身的条件装配和测试要求。
- 禁止新增生产依赖而不说明模块边界和替代方案。

## 测试要求

- 纯 Java 契约必须覆盖默认开启、显式关闭、不修改原配置、外部基础设施标记、plan 层不创建外部连接。
- 使用 Spring Boot context runner 验证 Starter 自动配置和属性绑定。
- 验证 `SynapseAutoConfigurationImportFilter` 对已知模块自动配置类默认放行、显式关闭后拦截、未知自动配置类始终放行。
- 验证 Data feature switch 能拦截 dynamic-datasource、DataSource、JdbcTemplate、Flyway 等外部自动配置。
- 验证 Cache feature switch 能拦截 Redis、Reactive Redis、Redis Repositories 等外部自动配置。
- 验证默认开启、显式关闭、缺少外部基础设施时 plan 层不触发连接。
- 示例应用必须验证 starter 接入路径。
- 模块完成后先运行 `/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn -pl synapse-starter -am test`，关键变更再运行根目录 `clean test`。
- TASK-007 完成后必须生成或更新 `skills/synapse-starter/SKILL.md`。

## 常见错误

- 自动配置无条件创建外部连接。
- starter 引入业务模块导致基础项目被迫依赖后台系统。
- 配置项没有默认值或说明。
- 在 plan/properties 层直接依赖 Spring Boot，导致简单规则无法单元测试。
- DATA/CACHE 默认开启时立即连接数据库或 Redis。
- 把 IAM、Admin Controller、业务表放进 starter。
- 只在 `SynapseFeature` 中声明模块，却忘记给模块添加 `AutoConfiguration.imports`。
- 模块自动配置类改名后没有同步更新 Starter 映射和 ImportFilter 测试。
- feature switch 只关闭 Synapse 自身自动配置，却遗漏会主动创建连接的三方自动配置。
- 用无序集合保存自动配置类名后再对外返回“主类名”，导致兼容方法结果不稳定。

## 示例任务拆分

- 定义 starter feature 开关和默认配置。
- 定义自动配置计划层。
- 注册 Starter 自身自动配置。
- 注册模块自动配置导入文件。
- 增加模块自动配置统一开关过滤器。
- 增加 Spring Boot context runner 测试。
- 增加示例应用 starter 集成测试。
- 扩展 feature switch 到外部自动配置并更新 Example 上下文测试。
