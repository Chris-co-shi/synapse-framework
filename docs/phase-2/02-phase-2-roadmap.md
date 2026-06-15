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
- starter 与 examples 后置收口。

## 2. 任务总览

| Task | 名称 | 优先级 | 类型 | 说明 |
| --- | --- | --- | --- | --- |
| TASK-201 | Framework Boundary 固化 | P0 | 文档 | 固化 framework 不可启动、不含业务代码的边界 |
| TASK-202 | WebMVC / WebFlux 拆分 | P0 | 结构 | 解决 gateway 不能引用 MVC 的问题 |
| TASK-203 | Cloud Context Propagation | P1 | 新模块 | Feign / 服务间调用上下文传播 |
| TASK-204 | OperationContext 全场景恢复 | P1 | 核心增强 | HTTP / MQ / Async / Job 上下文一致性 |
| TASK-205 | Time / Config / I18n 基础抽象 | P1 | 新模块 | 补齐平台运行时基础抽象 |
| TASK-206 | MQ / File / Audit / OAuth2 边界复查 | P2 | 收敛 | 防止技术模块滑向平台服务 |
| TASK-207 | Starter / Examples / Docs 收口 | P2 | 收尾 | 在边界稳定后提供组合接入体验 |

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
- `README.md`。
- `AGENTS.md`。
- `docs/modules/README.md`。
- `docs/06-待补充问题.md`。

不做内容：

- 不修改任何 Java 代码。
- 不修改任何 POM。
- 不新增 module。
- 不重命名 module。
- 不新增依赖。
- 不创建 starter。

交付物：

- 二阶段边界文档。
- 模块边界文档。
- 二阶段 roadmap。
- 边界检查清单。
- README 和 AGENTS 入口更新。

验收标准：

- 文档明确 framework 只做技术支持能力。
- 文档明确 platform 才做可启动服务。
- 文档明确 `synapse-config`、`synapse-file`、`synapse-mq`、`synapse-oauth2`、`synapse-audit` 的禁止事项。
- 文档明确 TASK-202 才处理 WebMVC / WebFlux 拆分。
- `git diff --check` 无格式错误。

风险点：

- 文档描述过度承诺未实现模块。
- 把规划能力写成当前事实。
- 忘记收紧 `synapse-oauth2`，导致后续滑向 IAM。

## 4. TASK-202：WebMVC / WebFlux 拆分

目标：

- 解决当前 `synapse-web` 偏 Servlet MVC，Gateway/WebFlux 服务不能引用的问题。
- 建立 `synapse-webmvc` 和 `synapse-webflux` 的清晰边界。

修改范围：

- 根 `pom.xml`。
- `synapse-bom/pom.xml`。
- 当前 `synapse-web` 模块。
- 新增或迁移 `synapse-webmvc`。
- 新增 `synapse-webflux`。
- AutoConfiguration imports。
- 相关测试。
- 模块文档和 Skill。

不做内容：

- 不创建可启动 gateway。
- 不做 Gateway 路由。
- 不做网关鉴权业务。
- 不做平台路由管理。

交付物：

- `synapse-webmvc`。
- `synapse-webflux`。
- 清晰的 Maven 依赖边界。
- WebMVC 和 WebFlux 自动配置。
- 最小测试用例。
- 模块文档。

验收标准：

- WebMVC 服务引用 `synapse-webmvc` 可以正常使用 Result、异常处理、Trace。
- WebFlux 服务引用 `synapse-webflux` 不引入 `spring-webmvc`。
- Gateway 类服务可以只依赖 `synapse-webflux` 的技术能力。
- `synapse-webflux` 不包含 Gateway 路由或启动服务。

风险点：

- 迁移时破坏现有 `synapse-web` 消费方兼容性。
- WebFlux 模块误引入 Servlet / MVC 依赖。
- WebFlux 异常处理写成 gateway 业务能力。

## 5. TASK-203：Cloud Context Propagation

目标：

- 新增 `synapse-cloud`，提供 Spring Cloud 微服务调用链路技术支撑。
- 解决 Feign 调用中的 traceId、requestId、OperationContext 透传。

修改范围：

- 新增 `synapse-cloud` module。
- 根 POM 和 BOM。
- Feign 相关自动配置。
- OperationContext Header codec。
- 模块文档和测试。

不做内容：

- 不做注册中心服务。
- 不做配置中心服务。
- 不做服务治理后台。
- 不做 Gateway 服务。
- 不绑定业务系统注册流程。

交付物：

- `SynapseFeignRequestInterceptor`。
- `SynapseFeignErrorDecoder`。
- `ServiceCallHeaders`。
- `InternalCallProperties`。
- `OperationContextHeaderCodec`。
- 测试用例和模块文档。

验收标准：

- HTTP -> Service A -> Feign -> Service B 链路可透传 traceId / requestId / actor / initiator。
- 消费方可以覆盖默认 Header 编码策略。
- Feign 错误解码不绑定业务错误码。

风险点：

- `synapse-cloud` 过早引入 Nacos、Gateway、配置中心等平台能力。
- Header 契约与 security trusted-header 产生冲突。
- OperationContext 信息泄露敏感内容。

## 6. TASK-204：OperationContext 全场景恢复

目标：

- 统一 HTTP / Feign / MQ / Async / Job 场景下的 OperationContext 创建、快照、恢复和清理。
- 避免异步、线程池、MQ 消费、定时任务场景下默认使用不可追溯的 system actor。

修改范围：

- `synapse-core`。
- `synapse-security`。
- `synapse-mq`。
- `synapse-webmvc` / `synapse-webflux`。
- 可能涉及 `synapse-cloud`。
- 测试和文档。

不做内容：

- 不做任务调度平台。
- 不做 MQ Broker adapter。
- 不做用户中心。
- 不做业务审计查询。

交付物：

- OperationContext codec。
- OperationContext propagator。
- Async TaskDecorator。
- MQ Header codec 对齐。
- Job Actor 策略。
- SystemActor 显式化规则。

验收标准：

- 没有上下文时不能悄悄伪装成 system。
- actor、initiator、source 可以区分。
- 异步执行后能恢复旧上下文并清理 ThreadLocal。
- MQ 消费可以从 Header 恢复 OperationContext。

风险点：

- ThreadLocal 泄漏。
- Reactor Context 与 ThreadLocal 混用不当。
- Actor 语义不清，导致审计不可追溯。

## 7. TASK-205：Time / Config / I18n 基础抽象

目标：

- 新增 `synapse-time`、`synapse-config`、`synapse-i18n` 三个技术抽象模块。
- 解决跨时区查询、运行时配置读取、国际化消息解析的 framework 基础能力。

修改范围：

- 新增 `synapse-time`。
- 新增 `synapse-config`。
- 新增 `synapse-i18n`。
- 根 POM 和 BOM。
- 自动配置、测试、文档。

不做内容：

- 不做 config-service。
- 不做 i18n-resource-center。
- 不做时区配置后台。
- 不做组织 / 工厂 / 用户资料管理。
- 不做配置数据库表。

交付物：

- `TimeZoneResolver`。
- `TimeRangeConverter`。
- `ConfigClient`。
- `ConfigResolver`。
- `ConfigParser`。
- `I18nMessageResolver`。
- `I18nResourceLoader`。
- 默认轻量实现和测试。

验收标准：

- LocalDate + ZoneId 可以转换成 UTC Instant 查询范围。
- Config key 可以解析为配置值。
- I18n key 可以解析为指定语言文案。
- 三个模块均不可启动，且无 Controller / 业务 Entity / 业务 Mapper。

风险点：

- `synapse-config` 滑向配置中心。
- `synapse-i18n` 滑向国际化资源管理后台。
- `synapse-time` 引入组织、工厂、用户等业务模型。

## 8. TASK-206：MQ / File / Audit / OAuth2 边界复查

目标：

- 复查已有风险模块，防止它们从技术抽象滑向平台服务。

修改范围：

- `synapse-mq`。
- `synapse-file`。
- `synapse-audit`。
- `synapse-oauth2`。
- 对应文档、测试和 Skill。

不做内容：

- 不做 message-service。
- 不做 file-service。
- 不做 audit-service。
- 不做 iam-service。
- 不做 Authorization Server 实现。

交付物：

- 模块 README 边界补齐。
- SPI / Port 命名统一。
- 默认实现边界检查。
- 边界测试。

验收标准：

- `synapse-mq` 无站内信、短信、邮件、消息模板管理。
- `synapse-file` 无上传下载 Controller、附件表、文件权限业务。
- `synapse-audit` 无审计查询 API、报表、中心后台。
- `synapse-oauth2` 无登录、客户端管理、Authorization Server 实现。

风险点：

- 为了“闭环”写入平台业务。
- 为了 demo 新增启动类或 Controller。
- 默认实现过重，绑定外部系统。

## 9. TASK-207：Starter / Examples / Docs 收口

目标：

- 在二阶段模块边界稳定后，整理 starter、examples 和文档入口。

修改范围：

- 可选新增 `synapse-starter-*`。
- examples 或 test fixture。
- README。
- docs/modules。
- skills。

不做内容：

- 不做生产启动服务。
- 不做 Admin UI。
- 不做平台业务服务。
- 不把 examples 当成平台应用。

交付物：

- starter 组合模块。
- 最小 examples 或测试 fixture。
- README 和模块文档收口。
- Skill 更新。

验收标准：

- starter 只聚合技术能力。
- examples 不进入生产模块。
- 文档清楚区分当前事实和后续规划。

风险点：

- starter 过早聚合未稳定模块。
- examples 被误用为可启动平台服务。
- 文档和实际代码不一致。

## 10. 推荐执行顺序

推荐顺序：

```text
TASK-201 -> TASK-202 -> TASK-203 -> TASK-204 -> TASK-205 -> TASK-206 -> TASK-207
```

理由：

1. 先固化边界，避免后续模块滑向平台服务。
2. 再拆 WebMVC / WebFlux，因为这是 Gateway 后续引用的前置条件。
3. 再做 Cloud，因为服务间调用需要稳定的 Web 和 Context 基础。
4. 再统一 OperationContext 全场景恢复，支撑 HTTP / Feign / MQ / Async / Job。
5. 再补 Time / Config / I18n，作为 Platform 后续 runtime 基础。
6. 再复查 MQ / File / Audit / OAuth2，防止已有模块膨胀。
7. 最后收口 starter、examples 和文档。

## 11. 当前结论

TASK-201 可以作为二阶段第一个执行任务。

原因：

- 它只做文档和边界固化，不触碰代码结构。
- 它为 TASK-202 之后的所有重构提供约束。
- 它可以防止 `synapse-config`、`synapse-file`、`synapse-mq`、`synapse-oauth2` 等模块滑向平台服务。
