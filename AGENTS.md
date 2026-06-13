# Synapse Framework AI 协作总规范

本文件是 Synapse Framework 仓库的 AI Agent 总入口。所有 Codex、代码 Agent、Review Agent、测试 Agent 在修改本仓库前必须读取本文件。

## 1. 项目定位

Synapse Framework 是面向 Java 企业应用的通用技术底座。

它只提供可复用的架构抽象、基础模块、自动配置、SPI、Adapter 和测试规范，供其他业务项目集成使用。本仓库不是后台管理系统、不是业务系统、不是可直接启动的应用。

核心目标：

1. 提供 Core、Web、Data、Cache、Security、OAuth2、Audit、File、Message 等一阶段可复用技术能力。
2. 提供统一异常、响应、追踪、数据访问、缓存、安全上下文、权限抽象、OAuth2 技术抽象、审计事件、幂等、限流、消息与文件抽象等基础设施。
3. 提供标准化 Java 后端开发包结构、接口规范、数据库规范和测试规范，作为消费方项目的工程约束。
4. 提供可被 AI Agent 长期协作的文档和 Skill 约束，避免不同 Agent 生成风格冲突的代码。
5. 为未来业务项目、Admin 项目、IAM 项目或行业应用提供技术支撑，但这些业务实现不进入本仓库。

## 2. 当前阶段

当前处于框架 v0.1 阶段：

- 优先完成纯技术底座模块。
- 一阶段 Maven reactor 固定为：`synapse-bom`、`synapse-core`、`synapse-web`、`synapse-data`、`synapse-cache`、`synapse-security`、`synapse-oauth2`、`synapse-audit`、`synapse-file`、`synapse-message`。
- `synapse-task` 当前移除，不得恢复到一阶段 reactor。
- `synapse-tenant`、`synapse-data-permission`、`synapse-cloud` 属于二阶段预留，当前不得实现，也不得加入 reactor。
- 当前不做 starter，业务项目按 module 引入。
- 不实现业务模块。
- 不提供启动应用。
- 不提供后台管理前端。
- 不把 IAM/Auth/RBAC、用户、角色、菜单、字典、组织等业务模型作为本仓库交付物。
- 优先保证模块边界、测试闭环和可维护性。
- 每次只执行一个 Task，禁止一次性大范围重构。

## 3. 技术基线

| 组件 | 推荐版本 | 说明 |
|---|---:|---|
| Java | 21 | 当前主线运行时基线。 |
| Maven | 3.9.0 | 当前工作站使用 `/Users/sxc/Documents/tool/apache-maven-3.9.0`。 |
| Spring Boot | 3.5.15 | 3.5.x 稳定线；本框架暂不切到 Spring Boot 4.x。 |
| Spring Cloud | 2025.0.2 | 二阶段 cloud 预留版本；当前不实现 `synapse-cloud`。 |
| Spring Cloud Alibaba | 2025.0.0.0 | 二阶段 cloud 预留版本；当前不实现 `synapse-cloud`。 |
| Spring Security | Boot 管理，6.5.x | 不单独覆盖 Boot 管理版本。 |
| OAuth2 | Authorization Server + Resource Server | 目标归属 `synapse-oauth2`，不提供业务登录系统；当前仅有模块骨架，迁移尚未完成。 |
| Token | JWT + JWK | 目标归属 `synapse-oauth2`；当前迁移尚未完成，后续 Task 从 `synapse-security` 拆分。 |
| MyBatis-Plus | 3.5.16 | 使用 `mybatis-plus-spring-boot3-starter`。 |
| dynamic-datasource | Spring Boot 3 starter | 配置级多数据源切换。 |
| Redis Server | 7.2.7 可用 | 服务端可保留；客户端版本跟随 Spring Boot 管理。 |
| Redis Client | Boot 管理 | 默认 Spring Data Redis + Lettuce，不手动指定 Lettuce/Jedis 版本。 |
| Seata | 2.5.0 优先 / 2.6.0 可选 | 使用 SCA BOM 时优先 2.5.0；独立接入时可评估 2.6.0。 |
| RocketMQ Server | 5.3.1 优先 | 跟随 SCA 2025.0.0.0 组件关系。 |
| RocketMQ Spring Boot Starter | 2.3.4 | 不走 SCA starter 管理时的直接依赖选择；升级需单独验证兼容性。 |
| OpenFeign | Spring Cloud 2025.0.2 管理 | 二阶段 cloud 预留；当前不实现。 |
| Spring Cloud LoadBalancer | Spring Cloud 2025.0.2 管理 | 二阶段 cloud 预留；当前不实现。 |
| Resilience4j | Spring Cloud 2025.0.2 管理 | 二阶段 cloud 预留；当前不实现。 |
| Flyway | Boot 管理 | 只提供迁移规范和测试约束。 |
| H2 / Testcontainers | Boot 管理 | 用于模块测试和兼容性验证。 |
| springdoc OpenAPI | 2.8.x | 由 BOM 统一管理。 |
| Lombok / MapStruct | BOM 管理 | 只用于减少样板代码和模型转换。 |

## 4. 强制读取文档

任何后端开发任务必须先读取：

- `docs/01-项目定位与边界.md`
- `docs/02-总体架构设计.md`
- `docs/03-核心链路设计.md`
- `docs/04-技术复杂点.md`
- `docs/05-面试表达.md`
- `docs/06-待补充问题.md`
- `docs/22-基座与业务域边界设计.md`
- `docs/23-工程结构与模块边界设计.md`
- `docs/25-开发前技术决策记录.md`
- `docs/26-工程初始化实施清单.md`

涉及 MyBatis-Plus 或动态数据源必须读取：

- `skills/synapse-data/SKILL.md`

涉及 Redis、缓存、分布式锁、幂等或限流必须读取：

- `skills/synapse-cache/SKILL.md`

涉及权限基础设施必须读取：

- `skills/synapse-security/SKILL.md`

涉及 OAuth2、JWT、JWK、Resource Server 或 Authorization Server 时，必须遵守本文件中 `synapse-oauth2` 的归属边界。当前 `synapse-oauth2` 只是模块骨架，OAuth2/JWT/JWK 代码迁移尚未完成，后续 Task 才允许从 `synapse-security` 拆分。

涉及测试必须读取对应模块 Skill；若存在测试工程 Skill，也必须读取。

## 5. 架构硬约束

### 5.1 禁止把业务代码放进框架仓库

禁止在本仓库新增：

- 业务 Controller。
- 业务 Application Service。
- 业务 Domain Model。
- 业务 Entity / Mapper / migration。
- 业务启动类，如 `XxxApplication`。
- Admin UI、业务页面或前端应用。
- 示例应用模块。

允许：

- 技术模块的自动配置。
- 技术模块的属性类。
- 技术模块的 SPI / Port / Adapter。
- 技术模块测试用的 test fixture、test application、test configuration。

### 5.2 框架模块只能提供技术能力

框架模块可以提供：

```text
Web response / exception / trace / validation
Data dialect / MyBatis-Plus configuration / datasource abstraction
Cache / lock / rate limit / idempotency
Security context / permission abstraction / invalid request protection / header contract
OAuth2 / JWT / JWK / Resource Server / Authorization Server technical abstraction
Audit event infrastructure / audit publisher / audit repository port
File storage abstraction / storage port / lightweight default implementation
Message infrastructure contract / producer-consumer SPI / lightweight default implementation
```

框架模块不得沉淀具体业务语义，如用户、角色、菜单、组织、字典、订单、工单、客户、库存。

当前模块边界：

- `synapse-security` 只负责轻量安全上下文、权限抽象、非法请求拦截、Header 契约；后续不得继续新增 OAuth2/JWT/JWK 能力。
- `synapse-oauth2` 是 OAuth2/JWT/JWK/Resource Server/Authorization Server 技术抽象的目标归属模块；当前仅有模块骨架，代码迁移尚未完成。
- `synapse-audit` 是审计事件基础设施，不是审计中心。
- `synapse-file` 是文件存储抽象，不是文件中心。
- `synapse-message` 是消息基础设施契约，不是消息中心。
- `synapse-tenant`、`synapse-data-permission`、`synapse-cloud` 是二阶段预留，当前不实现。

### 5.3 分层规则只约束消费方和可选 adapter

当本仓库提供 adapter 或生成规则时，必须保持：

```text
Controller -> Application Service -> Domain/Repository Port -> Repository Adapter -> Mapper
```

边界要求：

- Controller 不直接依赖 Mapper。
- Entity、Mapper、ServiceImpl、ActiveRecord 模型属于持久化实现，不允许直接暴露给 Controller 或前端。
- Domain Model 如独立存在，不承载 MyBatis-Plus 注解和持久化行为。
- API 返回对象必须是 response/result DTO，不直接返回 Entity。

### 5.4 Entity 只允许存在于 persistence entity 包

MyBatis-Plus Entity 是持久化模型，不是领域模型。

推荐包：

- Port：`domain.repository` 或模块暴露的 `port`
- Adapter：`infrastructure.persistence.repository`
- Mapper：`infrastructure.persistence.mapper`
- Entity：`infrastructure.persistence.entity`

### 5.5 禁止随意引入依赖

新增三方依赖必须说明：

1. 解决什么问题？
2. 为什么不用已有依赖？
3. 是否有许可证风险？
4. 是否会影响启动速度、包体积、安全性？
5. 是否需要封装在 adapter 里？当前不得新增 starter。

## 6. 代码修改前必须输出自查

每次实现前，Agent 必须先输出：

1. 本次目标是什么？
2. 本次涉及哪些模块？
3. 会修改哪些文件或目录？
4. 明确不会修改哪些内容？
5. 是否新增生产依赖？
6. 是否新增启动类或示例应用？
7. 是否引入业务概念、业务表或业务接口？
8. 是否新增数据库 migration？
9. Entity 放在哪个包？
10. Mapper 放在哪个包？
11. Repository Port 放在哪个包？
12. Repository Adapter 放在哪个包？
13. 是否会使用 JdbcTemplate？
14. 是否会使用 JdbcClient？
15. 是否会使用 java.sql？
16. 是否会使用 IService / ServiceImpl？
17. 是否会使用 ActiveRecord Model<T>，边界如何控制？
18. 是否会让 MyBatis-Plus 模型直接暴露到 Controller 或前端？
19. 是否会让 Controller 直接依赖 Mapper？
20. 是否触碰 `synapse-task`、`synapse-tenant`、`synapse-data-permission`、`synapse-cloud` 或将其加入 reactor？
21. 是否新增 starter？
22. 是否在 `synapse-security` 中继续新增 OAuth2/JWT/JWK 能力？
23. 是否把 `synapse-audit`、`synapse-file`、`synapse-message` 做成中心化平台服务？
24. 是否一次性处理多个 Task 或扩大任务范围？
25. 需要执行哪些验证命令？

## 7. 模块完成后的 Skill 交付规则

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

## 8. 测试要求

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

涉及安全、OAuth2、审计、缓存、消息、文件时，必须补充对应边界测试。租户、数据权限、Cloud 属于二阶段预留，当前不实现；Task 当前移除。

## 9. 输出要求

任务完成后必须输出：

1. 修改文件列表。
2. 新增文件列表。
3. 删除文件列表。
4. 核心实现说明。
5. 执行过的验证命令。
6. 验证结果。
7. 未完成事项。
8. 风险点。

## 10. 禁止行为

- 禁止大范围重构无关代码。
- 禁止为了测试通过降低测试标准。
- 禁止吞异常。
- 禁止返回 `null` 表示错误。
- 禁止绕过安全校验。
- 禁止直接复制开源框架源码。
- 禁止把业务模块代码放进框架基础模块。
- 禁止把临时实验代码提交到主模块。
- 禁止新增启动应用、示例应用或 Admin UI。
- 禁止使用宽泛的 `catch (Exception e)` 后只打印日志不处理。
- 禁止当前阶段新增 starter。
- 禁止恢复 `synapse-task` 或将其加入 reactor。
- 禁止将 `synapse-tenant`、`synapse-data-permission`、`synapse-cloud` 加入当前 reactor。
- 禁止在 `synapse-security` 中继续新增 OAuth2/JWT/JWK 能力。
- 禁止把 `synapse-audit` 实现为审计中心。
- 禁止把 `synapse-file` 实现为文件中心。
- 禁止把 `synapse-message` 实现为消息中心。

## 11. 代码注释要求

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

## 12. 默认开发原则

- 正确性优先于速度。
- 可维护性优先于炫技。
- 框架边界优先于功能堆叠。
- 技术底座优先于业务闭环。
- 先有规则，再写代码。
- 先做最小技术闭环，再做扩展能力。
