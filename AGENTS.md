# Synapse Framework AI 协作总规范

本文件是 Synapse Framework 仓库的 AI Agent 总入口。所有 Codex、代码 Agent、Review Agent、测试 Agent 在修改本仓库前必须读取本文件。

## 1. 项目定位

Synapse Framework 是面向 Java 企业应用的通用技术底座。

它只提供可复用的架构抽象、基础模块、自动配置、SPI、Adapter 和测试规范，供其他业务项目或 Synapse Platform 集成使用。本仓库不是后台管理系统、不是业务系统、不是可直接启动的应用。

核心目标：

1. 提供 Core、WebMVC、WebFlux、Cloud、Data、Cache、Security、OAuth2、Audit、File、MQ 等可复用技术能力。
2. 提供统一异常、响应、追踪、数据访问、缓存、安全上下文、权限抽象、OAuth2 技术抽象、审计事件、幂等、限流、消息与文件抽象等基础设施。
3. 提供标准化 Java 后端开发包结构、接口规范、数据库规范和测试规范，作为消费方项目的工程约束。
4. 提供可被 AI Agent 长期协作的文档和 Skill 约束，避免不同 Agent 生成风格冲突的代码。
5. 为未来业务项目、Synapse Platform、IAM 项目或行业应用提供技术支撑，但这些业务实现不进入本仓库。

## 2. 当前阶段

当前 Maven reactor 以根 `pom.xml` 为准：

- `synapse-bom`
- `synapse-core`
- `synapse-webmvc`
- `synapse-webflux`
- `synapse-cloud`
- `synapse-data`
- `synapse-cache`
- `synapse-security`
- `synapse-oauth2`
- `synapse-audit`
- `synapse-file`
- `synapse-mq`

当前阶段约束：

- `synapse-message` 已更名为 `synapse-mq`，后续不得继续使用旧名称描述正式模块。
- `synapse-web` 已在 TASK-202 中拆分为 `synapse-webmvc` 和 `synapse-webflux`，不得继续作为正式模块使用。
- `synapse-task` 当前不属于正式 reactor，不得恢复到当前阶段 reactor。
- `synapse-tenant`、`synapse-data-permission` 若目录存在，也只视为暂存目录或历史残留，不得当作当前已实现模块。
- `synapse-config`、`synapse-i18n`、`synapse-time` 属于二阶段规划模块，未进入 reactor 前不得描述成已实现能力。
- 本项目不创建 `synapse-starter-*`。
- 本项目不创建 starter 聚合包。
- 本项目不创建 demo / example / sample application。
- 业务项目按需直接引用具体 module。
- 不实现业务模块。
- 不提供启动应用。
- 不提供后台管理前端。
- 不把 IAM/Auth/RBAC、用户、角色、菜单、字典、组织等业务模型作为本仓库交付物。
- 每次只执行一个 Task，禁止一次性大范围重构。

## 3. 二阶段边界原则

二阶段必须先遵守以下文档：

- `docs/phase-2/00-framework-boundary.md`
- `docs/phase-2/01-module-boundary.md`
- `docs/phase-2/02-phase-2-roadmap.md`
- `docs/phase-2/03-boundary-checklist.md`
- `docs/phase-2/04-cloud-context-propagation.md`

任何后续 Agent 执行前，必须至少先阅读：

1. `docs/phase-2/00-framework-boundary.md`
2. `docs/phase-2/03-boundary-checklist.md`

二阶段核心原则：

- Framework 不可启动。
- Framework 不含业务代码。
- Framework 只提供技术支持能力。
- Platform 才承载可启动服务。
- Business Application 才承载具体业务模型、业务 API 和业务流程。

Framework 可以提供：

- 技术约束。
- 通用抽象。
- 通用模型。
- SPI / Port。
- 默认轻量实现。
- 自动装配。
- 上下文传播。
- 编码规范。
- 工具能力。
- 测试与文档沉淀。
- Skill 最佳实践。

Framework 禁止提供：

- `@SpringBootApplication` 生产启动类。
- 业务 Controller。
- 业务 Service / Entity / Mapper / Repository。
- 业务数据库 migration。
- starter 聚合包。
- demo / example / sample application。
- 配置中心、文件中心、消息中心、审计中心、任务中心、IAM 等平台业务实现。
- Gateway / IAM / Message / File / Config / Task 等可启动服务。

涉及 `synapse-cloud`、Spring Cloud、OpenFeign、Feign `RequestInterceptor` 或 Feign `ErrorDecoder` 的任务，必须先阅读：

- `docs/phase-2/04-cloud-context-propagation.md`

Cloud 边界原则：

- `synapse-cloud` 只能做 Spring Cloud / OpenFeign / 服务间调用技术适配。
- `synapse-cloud` 不得做 Gateway、注册中心、配置中心、服务治理后台或 IAM。
- `synapse-cloud` 不得依赖 `synapse-webmvc` 或 `synapse-webflux` 来复用 `Result`、trace 或 error response。
- 服务间 Header 禁止传播 roles、permissions、menu codes、organization tree、raw token、password、credential 和 business data。
- 服务间签名只允许作为技术扩展点，不能实现登录认证、业务鉴权或 IAM。

## 4. 技术基线

| 组件 | 推荐版本 | 说明 |
|---|---:|---|
| Java | 21 | 当前主线运行时基线。 |
| Maven | 3.9.0 | 当前工作站使用 `/Users/sxc/Documents/tool/apache-maven-3.9.0`。 |
| Spring Boot | 3.5.15 | 3.5.x 稳定线；本框架暂不切到 Spring Boot 4.x。 |
| Spring Cloud | 2025.0.2 | 当前用于 `synapse-cloud` OpenFeign 技术适配。 |
| Spring Cloud Alibaba | 2025.0.0.0 | 后续如使用，必须保持在 framework 技术边界内。 |
| Spring Security | Boot 管理，6.5.x | 不单独覆盖 Boot 管理版本。 |
| OAuth2 | JWT / JWK / Token / Resource Server 辅助能力 | 二阶段 framework 不做 Authorization Server 实现。 |
| MyBatis-Plus | 3.5.16 | 使用 `mybatis-plus-spring-boot3-starter`。 |
| dynamic-datasource | Spring Boot 3 starter | 配置级多数据源切换。 |
| Redis Client | Boot 管理 | 默认 Spring Data Redis + Lettuce，不手动指定 Lettuce/Jedis 版本。 |
| Flyway | Boot 管理 | 只提供迁移规范和测试约束，不提供业务 migration。 |
| H2 / Testcontainers | Boot 管理 | 用于模块测试和兼容性验证。 |
| Lombok / MapStruct | BOM 管理 | 只用于减少样板代码和模型转换。 |

## 5. 强制读取文档

任何后端开发任务必须先读取：

- `docs/phase-2/00-framework-boundary.md`
- `docs/phase-2/03-boundary-checklist.md`
- `docs/01-项目定位与边界.md`
- `docs/02-总体架构设计.md`
- `docs/03-核心链路设计.md`
- `docs/04-技术复杂点.md`
- `docs/06-待补充问题.md`
- `docs/modules/README.md`

历史文档编号曾出现过调整。如果旧提示词中出现 `docs/22-*`、`docs/23-*`、`docs/25-*`、`docs/26-*` 等路径，应优先以当前仓库实际路径和 `README.md` 文档导航为准，不得自行猜测或创建重复文档。

涉及 MyBatis-Plus 或动态数据源必须读取：

- `skills/synapse-data/SKILL.md`

涉及 Redis、缓存、分布式锁、幂等或限流必须读取：

- `skills/synapse-cache/SKILL.md`

涉及权限基础设施必须读取：

- `skills/synapse-security/SKILL.md`

涉及 OAuth2、JWT、JWK、Resource Server 时，必须遵守 `docs/phase-2/00-framework-boundary.md` 中 `synapse-oauth2` 的归属边界。二阶段 `synapse-oauth2` 不允许实现 Authorization Server、登录、客户端管理、用户认证或 IAM 后台。

涉及 `synapse-cloud`、OpenFeign 或服务间上下文传播时，必须遵守 `docs/phase-2/04-cloud-context-propagation.md`。不得把 `synapse-cloud` 做成 Gateway、注册中心、配置中心、服务治理后台、IAM、登录认证或业务鉴权模块。

涉及测试必须读取对应模块 Skill；若存在测试工程 Skill，也必须读取。

## 6. 架构硬约束

### 6.1 禁止把业务代码放进框架仓库

禁止在本仓库新增：

- 业务 Controller。
- 业务 Application Service。
- 业务 Domain Model。
- 业务 Entity / Mapper / Repository / migration。
- 业务启动类，如 `XxxApplication`。
- starter 聚合包。
- demo / example / sample application。
- Admin UI、业务页面或前端应用。
- 可启动平台服务。

允许：

- 技术模块的自动配置。
- 技术模块的属性类。
- 技术模块的 SPI / Port / Adapter。
- 技术型 BaseEntity / AuditableEntity / VersionedEntity。
- 技术模块测试用的 test fixture、test application、test configuration。

### 6.2 框架模块只能提供技术能力

框架模块可以提供：

```text
Web response / exception / trace / validation
Data dialect / MyBatis-Plus configuration / datasource abstraction
Cache / lock / rate limit / idempotency
Security context / permission abstraction / invalid request protection / header contract
OAuth2 / JWT / JWK / Token / Resource Server auxiliary capability
Audit event infrastructure / audit publisher / audit repository port
File storage abstraction / storage port / lightweight default implementation
MQ infrastructure contract / producer-consumer SPI / lightweight default implementation
Config / I18n / Time runtime abstraction after corresponding phase-2 tasks
```

框架模块不得沉淀具体业务语义，如用户、角色、菜单、组织、字典、订单、工单、客户、库存。

当前模块边界：

- `synapse-security` 只负责轻量安全上下文、权限抽象、非法请求拦截、Header 契约；不得实现 IAM。
- `synapse-oauth2` 只允许 JWT / JWK / Token / Resource Server 辅助能力；二阶段不得实现 Authorization Server。
- `synapse-audit` 是审计事件基础设施，不是审计中心。
- `synapse-file` 是文件存储抽象，不是文件中心。
- `synapse-mq` 是 MQ 基础设施契约，不是消息中心。
- `synapse-config` 未来只做配置抽象，不是配置中心服务。
- `synapse-webmvc` 只做 Servlet MVC 技术支撑，不包含 WebFlux / Gateway。
- `synapse-webflux` 只做 WebFlux 技术支撑，不是 gateway 服务。
- `synapse-cloud` 只做服务间调用上下文传播和 Feign 技术适配，不是 Gateway、注册中心、配置中心、服务治理后台或 IAM。
- 本项目不提供 starter，也不提供 demo / example / sample application。

### 6.3 分层规则只约束消费方和可选 adapter

当本仓库提供 adapter 或生成规则时，必须保持：

```text
Controller -> Application Service -> Domain/Repository Port -> Repository Adapter -> Mapper
```

边界要求：

- Controller 不直接依赖 Mapper。
- Entity、Mapper、ServiceImpl、ActiveRecord 模型属于持久化实现，不允许直接暴露给 Controller 或前端。
- Domain Model 如独立存在，不承载 MyBatis-Plus 注解和持久化行为。
- API 返回对象必须是 response/result DTO，不直接返回 Entity。

注意：上述分层主要约束消费方和可选 adapter。Framework 生产代码本身不应新增业务 Controller 或业务 Application Service。

### 6.4 Entity 边界

MyBatis-Plus Entity 是持久化模型，不是领域模型。

Framework 允许技术型基类，例如：

- `BaseEntity`
- `AuditableEntity`
- `VersionedEntity`

Framework 禁止业务 Entity，例如：

- User。
- Role。
- Menu。
- Organization。
- ConfigItem。
- FileRecord。
- MessageTemplate。

### 6.5 禁止随意引入依赖

新增三方依赖必须说明：

1. 解决什么问题？
2. 为什么不用已有依赖？
3. 是否有许可证风险？
4. 是否会影响启动速度、包体积、安全性？
5. 是否需要封装在 adapter 里？
6. 是否会把 Platform 能力提前带入 Framework？

## 7. 代码修改前必须输出自查

每次实现前，Agent 必须先输出：

1. 本次目标是什么？
2. 本次涉及哪些模块？
3. 会修改哪些文件或目录？
4. 明确不会修改哪些内容？
5. 是否新增生产依赖？
6. 是否新增启动类或示例应用？
7. 是否新增 starter？
8. 是否新增 demo / example / sample application？
9. 是否引入业务概念、业务表或业务接口？
10. 是否新增数据库 migration？
11. 是否会新增 Controller？
12. 是否会新增业务 Entity / Mapper / Repository / Service？
13. 是否触碰 `synapse-task`、`synapse-tenant`、`synapse-data-permission`，或将其加入 reactor？
14. 是否在 `synapse-oauth2` 中实现 Authorization Server？
15. 是否把 `synapse-audit`、`synapse-file`、`synapse-mq`、`synapse-config` 做成中心化平台服务？
16. 是否把 `synapse-webflux` 做成 gateway 服务？
17. 是否一次性处理多个 Task 或扩大任务范围？
18. 需要执行哪些验证命令？

## 8. 模块完成后的 Skill 交付规则

每完成一个技术模块并通过测试后，必须新增或更新：

```text
skills/<module-name>/SKILL.md
```

要求：

- `SKILL.md` 是最佳实践，不是过程日志。
- 模块测试未通过，不允许把实现沉淀为最终 Skill。
- `SKILL.md` 必须覆盖模块职责和边界、推荐包结构、允许技术和禁止事项、标准实现模式、测试要求、常见错误、执行前必读文档、示例任务拆分方式。
- 后续同类模块开发前，Agent 必须先读取对应 `SKILL.md`。
- 如果实现过程中发现原 Skill 规则不适用，必须先说明原因，再更新 Skill。

## 9. 测试要求

每个技术模块至少覆盖：

- 正常流程。
- 参数为空。
- 参数非法。
- 自动配置启用。
- 自动配置关闭。
- 缺少依赖时不误装配。
- 多实例或并发场景。
- 异常传播和错误码。
- 与 Spring Boot 条件装配兼容。
- 与消费方可覆盖配置兼容。

涉及安全、OAuth2、审计、缓存、MQ、文件、Config、I18n、Time 时，必须补充对应边界测试。

## 10. 输出要求

任务完成后必须输出：

1. 修改文件列表。
2. 新增文件列表。
3. 删除文件列表。
4. 核心实现说明。
5. 执行过的验证命令。
6. 验证结果。
7. 未完成事项。
8. 风险点。

## 11. 禁止行为

- 禁止大范围重构无关代码。
- 禁止为了测试通过降低测试标准。
- 禁止吞异常。
- 禁止返回 `null` 表示错误。
- 禁止绕过安全校验。
- 禁止直接复制开源框架源码。
- 禁止把业务模块代码放进框架基础模块。
- 禁止把临时实验代码提交到主模块。
- 禁止新增生产启动应用、示例应用或 Admin UI。
- 禁止新增 starter。
- 禁止新增 demo / example / sample application。
- 禁止使用宽泛的 `catch (Exception e)` 后只打印日志不处理。
- 禁止恢复 `synapse-task` 或将其加入 reactor。
- 禁止将 `synapse-tenant`、`synapse-data-permission` 加入当前 reactor。
- 禁止在 `synapse-oauth2` 中实现 Authorization Server。
- 禁止把 `synapse-audit` 实现为审计中心。
- 禁止把 `synapse-file` 实现为文件中心。
- 禁止把 `synapse-mq` 实现为消息中心。
- 禁止把 `synapse-config` 实现为配置中心。
- 禁止把 `synapse-webflux` 实现为 gateway 服务。

## 12. 代码注释要求

框架代码必须补充必要注释，但禁止为了显得完整而机械注释。

必须注释：

- 对外公开的 SPI、Port、Annotation、AutoConfiguration、Properties。
- 会被消费方直接依赖的公共类和公共方法。
- 复杂算法、并发控制、Lua 脚本、缓存一致性、幂等、限流、重试、熔断、事务边界。
- 安全、租户、数据权限、审计、Trace 等容易误用的边界。
- 非显而易见的设计取舍、兼容性处理和降级策略。

注释要求：

- 优先使用中文，除非文件已有英文注释约定。
- 公共 API 优先使用 Javadoc。
- 普通实现只在必要处写短注释。
- 注释说明“为什么”和“边界”，不要重复代码在做什么。

禁止：

- 给 getter/setter、简单赋值、显而易见分支写噪音注释。
- 注释与代码行为不一致。
- 用注释掩盖临时实现或未完成逻辑。

## 13. 默认开发原则

- 正确性优先于速度。
- 可维护性优先于炫技。
- 框架边界优先于功能堆叠。
- 技术底座优先于业务闭环。
- 先有规则，再写代码。
- 先做最小技术闭环，再做扩展能力。
