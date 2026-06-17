# 02-Phase 2 Roadmap

本文档固化 Synapse-Framework 二阶段任务拆分。二阶段目标不是建设平台服务，而是在 Framework 边界内补齐微服务技术基座能力。

## 1. 二阶段总体目标

二阶段目标：

> 在不引入业务代码、不提供可启动服务的前提下，把 Synapse-Framework 从一阶段基础模块升级为 Synapse-Platform 可复用的微服务技术基座。

重点方向：

- Framework / Platform / Business Application 边界固化。
- WebMVC / WebFlux 技术栈隔离。
- HTTP / Feign / MQ / Async / Job 的 OperationContext 传播和恢复。
- Spring Cloud 调用链路适配。
- UTC 时间存储和用户时区查询转换。
- 配置抽象、国际化抽象、MQ/File/Audit/OAuth2 边界收紧。
- Docs / Skills / Boundary 收口。

固定约定：

- 不创建 `synapse-starter-*`。
- 不创建 starter 聚合包。
- 不创建 demo / example / sample application。
- 不创建任何可启动示例工程。
- 业务系统按需直接引用具体 module。

## 2. 任务总览

| Task | 名称 | 优先级 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| TASK-201 | Framework Boundary 固化 | P0 | 文档 | 固化 framework 不可启动、不含业务代码的边界 |
| TASK-202 | WebMVC / WebFlux 拆分 | P0 | 结构 | 已拆分为 `synapse-webmvc` / `synapse-webflux` |
| TASK-203 | Cloud Context Propagation | P1 | 新模块 | 已新增 `synapse-cloud`，提供 Feign / 服务间调用上下文传播 |
| TASK-204 | OperationContext 全场景恢复 | P1 | 核心增强 | 已统一 HTTP / MQ / Async 基础上下文传播规则，Job 入口显式 actor 策略已固化 |
| TASK-205 | Time / Config / I18n 基础抽象 | P1 | 新模块 | 已新增 `synapse-time`、`synapse-config`、`synapse-i18n` 基础抽象 |
| TASK-206 | MQ / File / Audit / OAuth2 边界复查 | P2 | 收敛 | 已复查风险模块边界并补齐 Skill |
| TASK-207 | Docs / Skills / Boundary 收口 | P2 | 收尾 | 已完成文档、Skill、边界检查和模块状态校准 |
| TASK-208 | Spring Boot Configuration Metadata 收口 | P2 | 封板补丁 | 已完成公开配置项 metadata 生成、测试和文档收口 |

## 3. TASK-201：Framework Boundary 固化

目标：

- 固化 Framework / Platform / Business Application 三层边界。
- 明确 framework 不可启动、不含业务代码。
- 明确 platform 才能承载 gateway、iam、message、file、config、task 等可启动服务。
- 为后续 Codex 和 Agent 提供边界检查入口。

修改范围：

- `docs/phase-2/00-framework-boundary.md`。
- `docs/phase-2/01-module-boundary.md`。
- `docs/phase-2/02-phase-2-roadmap.md`。
- `docs/phase-2/03-boundary-checklist.md`。
- README、AGENTS、docs/modules、待补充问题文档。

不做内容：

- 不修改任何 Java 代码。
- 不修改任何 POM。
- 不新增 module。
- 不重命名 module。
- 不新增依赖。
- 不创建 starter。
- 不创建 demo / example / sample application。

交付物：二阶段边界文档、模块边界文档、roadmap、边界检查清单、README 和 AGENTS 入口更新。

验收标准：文档明确 framework 只做技术支持能力，platform 才做可启动服务，并且明确 `synapse-config`、`synapse-file`、`synapse-mq`、`synapse-oauth2`、`synapse-audit` 的禁止事项。

## 4. TASK-202：WebMVC / WebFlux 拆分

目标：

- 解决原 `synapse-web` 偏 Servlet MVC，Gateway/WebFlux 服务不能引用的问题。
- 建立 `synapse-webmvc` 和 `synapse-webflux` 的清晰边界。

完成说明：

- `synapse-web` 不保留兼容层。
- `synapse-webmvc` 承接 Servlet MVC 响应、异常、Trace、Filter 异常桥接。
- `synapse-webflux` 只提供 WebFlux Trace、异常响应、Reactor Context / OperationContext 恢复，不做 Gateway。

不做内容：

- 不创建可启动 gateway。
- 不做 Gateway 路由。
- 不做网关鉴权业务。
- 不做平台路由管理。

风险点：WebFlux 模块误引入 Servlet / MVC 依赖，或 WebFlux 异常处理写成 gateway 业务能力。

## 5. TASK-203：Cloud Context Propagation

目标：

- 新增 `synapse-cloud`，提供 Spring Cloud / OpenFeign 微服务调用链路技术支撑。
- 解决 Feign 调用中的 traceId、requestId、OperationContext 透传。
- 冻结服务间调用 Header 契约，避免 WebMVC / WebFlux / MQ / Security 各自扩散。

完成说明：

- TASK-203-A 已通过文档冻结 Cloud 方案和 Header 契约。
- TASK-203-B 已新增 `synapse-cloud` module，修改根 POM 和 BOM。
- TASK-203-C 已实现 OperationContext HTTP Header codec。
- TASK-203-D 已实现 Feign RequestInterceptor。
- TASK-203-E 已实现 Feign ErrorDecoder。
- TASK-203-F 已完成自动配置和测试。
- TASK-203-G 已补齐模块文档和 Skill。

不做内容：

- 不做注册中心服务。
- 不做配置中心服务。
- 不做服务治理后台。
- 不做 Gateway 服务。
- 不绑定业务系统注册流程。
- 不做 IAM。
- 不做登录认证。
- 不做业务鉴权。
- 不绑定 Nacos / Seata / RocketMQ。
- 不依赖 `synapse-webmvc` 或 `synapse-webflux` 复用 Result / trace / error response。
- 不传播 roles / permissions / menu / raw token 等敏感或业务字段。

交付物：`docs/phase-2/04-cloud-context-propagation.md`、`SynapseFeignRequestInterceptor`、`SynapseFeignErrorDecoder`、`SynapseCloudHeaders`、`InternalCallSigner` / `InternalCallVerifier` 扩展点、OperationContext HTTP Header codec、测试用例、模块文档和 Skill。

验收标准：HTTP -> Service A -> Feign -> Service B 链路可透传 traceId / requestId / actor / initiator；Feign 错误解码不绑定业务错误码；服务间签名只作为扩展点；`synapse-cloud` 不依赖 `synapse-webmvc`、`synapse-webflux`、`synapse-security`、`synapse-mq`。

## 6. TASK-204：OperationContext 全场景恢复

目标：

- 统一 HTTP / Feign / MQ / Async / Job 场景下的 OperationContext 创建、快照、恢复和清理。
- 避免异步、线程池、MQ 消费、定时任务场景下默认使用不可追溯的 system actor。

修改范围：`synapse-core`、`synapse-security`、`synapse-mq`、`synapse-webmvc`、`synapse-webflux`、`synapse-cloud`、测试和文档。

完成说明：

- `synapse-core` 已新增纯 Java `OperationContextSnapshotCarrier`、`OperationContextSnapshotCodec`、`OperationContextPropagator`。
- `synapse-core` 已新增 `ContextAwareRunnable`、`ContextAwareCallable`、`OperationContextExecutor`，用于同步包装异步任务。
- `synapse-core` 已新增 `SystemOperationActorFactory`，system actor 必须显式创建。
- `synapse-webmvc` 已新增 `MvcOperationContextFilter`，从标准 Header 恢复 OperationContext。
- `synapse-webflux` 已对齐 core codec 规则，通过 Reactor Context 保存 `OperationContextSnapshot`。
- `synapse-cloud` HTTP Header codec 已复用 core codec，并保留 Feign 覆盖策略。
- `synapse-mq` 消息 header codec 已复用 core codec，并保持 MQ 小写 header 契约。

不做内容：

- 不做任务调度平台。
- 不做 MQ Broker adapter。
- 不做用户中心。
- 不做业务审计查询。
- 不做 demo / example / sample application。

交付物：OperationContext codec / propagator、Async 上下文传播包装器、MQ Header codec 对齐、WebMVC / WebFlux 入站恢复、Job Actor 显式化规则、SystemActor 显式化规则。

验收标准：没有上下文时不能悄悄伪装成 system；actor、initiator、source 可以区分；异步执行后能恢复旧上下文并清理 ThreadLocal；MQ 消费可以从 Header 恢复 OperationContext。

## 7. TASK-205：Time / Config / I18n 基础抽象

目标：

- 新增 `synapse-time`、`synapse-config`、`synapse-i18n` 三个技术抽象模块。
- 解决跨时区查询、运行时配置读取、国际化消息解析的 framework 基础能力。

修改范围：新增三个 module、根 POM、BOM、自动配置、测试、文档、Skill。

完成说明：

- `synapse-time` 已提供 `TimeZoneResolver`、`TimeRangeConverter`、UTC 查询范围转换和自动配置。
- `synapse-config` 已提供 `ConfigClient`、`ConfigParser`、`ConfigResolver`、轻量内存配置客户端和自动配置。
- `synapse-i18n` 已提供 `LocaleResolver`、`I18nResourceLoader`、`I18nMessageResolver`、轻量内存资源加载和自动配置。
- 三个模块均已进入 root reactor 和 BOM。

不做内容：

- 不做 config-service。
- 不做 i18n-resource-center。
- 不做时区配置后台。
- 不做组织 / 工厂 / 用户资料管理。
- 不做配置数据库表。
- 不做配置发布 / 审批。
- 不做 demo / example / sample application。

交付物：`TimeZoneResolver`、`TimeRangeConverter`、`ConfigClient`、`ConfigResolver`、`ConfigParser`、`I18nMessageResolver`、`I18nResourceLoader`、默认轻量实现、自动配置、测试、模块文档和 Skill。

验收标准：LocalDate + ZoneId 可以转换成 UTC Instant 查询范围；Config key 可以解析为配置值；I18n key 可以解析为指定语言文案；三个模块均不可启动，且无 Controller / 业务 Entity / 业务 Mapper。

## 8. TASK-206：MQ / File / Audit / OAuth2 边界复查

目标：复查已有风险模块，防止它们从技术抽象滑向平台服务。

修改范围：`synapse-mq`、`synapse-file`、`synapse-audit`、`synapse-oauth2`、对应文档、测试和 Skill。

完成说明：

- `synapse-mq` 边界复查通过：当前只提供消息外壳、SPI、模板、上下文传播和异常分类，不做 message-service。
- `synapse-file` 边界复查通过：当前只提供文件存储抽象和本地轻量实现，不做 file-service。
- `synapse-audit` 边界复查通过：当前只提供审计事件、上下文补齐和输出端口，不做 audit-service。
- `synapse-oauth2` 边界复查通过：当前只提供 JWT / JWK / Token denylist 技术辅助，不做 IAM 或 Authorization Server。
- 已新增 `skills/synapse-mq`、`skills/synapse-file`、`skills/synapse-audit`、`skills/synapse-oauth2`。

不做内容：

- 不做 message-service。
- 不做 file-service。
- 不做 audit-service。
- 不做 iam-service。
- 不做 Authorization Server 实现。
- 不做登录认证。
- 不做 demo / example / sample application。

交付物：模块 README 边界复查、Skill 补齐、默认实现边界检查、边界扫描命令记录。

验收标准：`synapse-mq` 无站内信、短信、邮件、消息模板管理；`synapse-file` 无上传下载 Controller、附件表、文件权限业务；`synapse-audit` 无审计查询 API、报表、中心后台；`synapse-oauth2` 无登录、客户端管理、Authorization Server 实现。

## 9. TASK-207：Docs / Skills / Boundary 收口

目标：

- README 收口。
- docs/modules 收口。
- phase-2 roadmap 状态收口。
- AGENTS 边界补强。
- Skill 索引补齐。
- 边界检查命令补齐。
- 模块事实与规划状态校准。

修改范围：README、AGENTS、docs/modules、docs/phase-2、skills。

完成说明：

- README 已加入当前模块事实和 Skill 索引入口。
- AGENTS 已校准当前 reactor modules 和 TASK-205 后的模块状态。
- docs/modules 已校准当前模块事实。
- skills 已补齐当前已实现模块索引和缺失模块 Skill。
- no starter / no demo / no example / no sample application 约定已作为长期约束保留。

不做内容：

- 不新增 starter module。
- 不新增 demo。
- 不新增 example application。
- 不新增 sample application。
- 不新增生产启动类。
- 不新增可启动平台服务。
- 不新增业务 Controller / Entity / Mapper / Service。

交付物：文档状态校准、Skill 索引补齐、边界检查命令补齐、规划能力与当前事实校准。

验收标准：

- 文档清楚区分当前事实和后续规划。
- 所有已实现模块都有模块手册和 Skill。
- starter / demo / example / sample 仅作为禁止项或历史说明出现。
- 不存在“后续新增 starter”或“后续新增 demo”的正向规划。

风险点：文档和实际代码不一致，或把平台服务写成 framework 能力。

## 10. TASK-208：Spring Boot Configuration Metadata 收口

目标：

- 为所有公开 `@ConfigurationProperties` 生成可发布的 Spring Boot Configuration Metadata。
- 让消费方在 `application.yml` / `application.properties` 中获得 `synapse.*` 配置补全。
- 补齐配置项说明、默认值、类型和必要 hints。

完成说明：

- 已通过根 POM 统一配置 Spring Boot configuration processor。
- 已覆盖 `synapse.cache`、`synapse.cloud`、`synapse.cloud.feign`、`synapse.config`、`synapse.file`、`synapse.i18n`、`synapse.oauth2`、`synapse.security`、`synapse.time`。
- 已为 `synapse.time.default-zone` 和 `synapse.i18n.default-locale` 增加 additional metadata hints。
- 已增加 metadata 单元测试和 jar 产物检查。

不做内容：

- 不新增 starter。
- 不新增 demo / example / sample application。
- 不新增业务配置模型。
- 不修改现有配置 key 或默认行为。

验收标准：`mvn package` 后相关 jar 包含 `META-INF/spring-configuration-metadata.json`，metadata JSON 可解析，且包含真实公开配置项。

## 11. 推荐执行顺序

推荐顺序：

```text
TASK-201 -> TASK-202 -> TASK-203 -> TASK-204 -> TASK-205 -> TASK-206 -> TASK-207 -> TASK-208
```

理由：

1. 先固化边界，避免后续模块滑向平台服务。
2. 再拆 WebMVC / WebFlux，因为这是 Gateway 后续引用的前置条件。
3. 再做 Cloud，因为服务间调用需要稳定的 Web 和 Context 基础。
4. 再统一 OperationContext 全场景恢复，支撑 HTTP / Feign / MQ / Async / Job。
5. 再补 Time / Config / I18n，作为 Platform 后续 runtime 基础。
6. 再复查 MQ / File / Audit / OAuth2，防止已有模块膨胀。
7. 再做 Docs / Skills / Boundary 收口，不做 starter 或 demo。
8. 最后执行 TASK-208 metadata 封板补丁，保证发布产物具备 IDE 配置索引条件。

## 12. 当前结论

二阶段后续继续推进时，必须遵守：

- Framework 只交付 module、抽象、自动配置、测试、文档和 Skill。
- 业务系统按需直接引用具体 module。
- 不创建 starter。
- 不创建 demo / example / sample application。
