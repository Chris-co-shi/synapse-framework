# 00-Framework Boundary

本文档用于固化 Synapse-Framework 二阶段边界。后续任何 Codex、Agent 或人工开发任务，在修改代码前必须先确认本文件约束。

## 1. 三层边界

Synapse 体系默认分为三层：

```text
Business Application
  -> 具体业务系统，例如 MES、WMS、QMS、EMS、CRM、SCM 等
  -> 拥有业务模型、业务 API、业务流程、业务数据库表和业务权限码

Synapse Platform
  -> 可启动的平台服务集合，例如 gateway、iam、message、file、config、task
  -> 承载平台级业务能力、管理后台、运行时服务和跨系统治理能力

Synapse Framework
  -> Java 技术支持框架
  -> 提供通用技术抽象、契约、默认轻量实现、自动装配和上下文传播能力
```

核心原则：

> Synapse-Framework 只能是技术底座，不能变成可启动平台，也不能承载业务代码。

## 2. Framework 可以提供什么

Framework module 最多只能提供以下内容：

- 技术约束。
- 通用抽象。
- 通用模型。
- SPI / Port。
- 默认轻量实现。
- Spring Boot AutoConfiguration。
- 上下文传播。
- 编码规范。
- 工具能力。
- 测试辅助和技术 fixture。
- 文档和 Skill 最佳实践。

允许示例：

```text
ErrorCode / SynapseException
OperationContext / OperationContextSnapshot
Web MVC exception handler
WebFlux WebFilter
Feign RequestInterceptor
ConfigClient / ConfigResolver
I18nMessageResolver
TimeRangeConverter
FileStorageClient
MessagePublisher / MessageConsumer SPI
AuditEvent / AuditLogPort
```

## 3. Framework 禁止提供什么

Framework 中禁止新增：

- `@SpringBootApplication` 启动类。
- `SpringApplication.run(...)` 生产启动入口。
- 业务 Controller。
- 业务 Application Service。
- 业务 Domain Model。
- 业务 Entity。
- 业务 Mapper。
- 业务 Repository。
- 业务数据库 migration。
- starter 聚合包。
- demo / example / sample application。
- 用户、角色、菜单、组织、部门、字典等平台业务模型。
- 配置中心、文件中心、消息中心、审计中心、任务中心、IAM 等平台服务实现。
- Gateway、IAM、Message、File、Config、Task 等可启动服务。
- 后台管理页面或前端应用。

测试 fixture 可以出现测试专用 Controller、Configuration 或 Application，但必须位于 `src/test`，且不得作为生产能力暴露。

## 4. 固定交付约定

Synapse-Framework 固定采用“按需引用具体 module”的交付方式：

- 不创建 `synapse-starter-*`。
- 不创建 starter 聚合包。
- 不创建 demo / example / sample application。
- 不创建任何可启动示例工程。
- 业务系统按需直接引用具体 module。
- Framework 只交付 module、抽象、自动配置、测试、文档和 Skill。

## 5. Platform 才能提供什么

以下可启动服务统一属于 Synapse-Platform：

| Platform 服务 | 职责 | Framework 对应边界 |
| --- | --- | --- |
| `synapse-gateway` | 统一入口、路由、鉴权前置、Header 注入 | `synapse-webflux` 只提供 WebFlux 技术支撑 |
| `synapse-iam` | 用户、角色、菜单、资源、登录、授权 | `synapse-security` / `synapse-oauth2` 只提供技术抽象 |
| `synapse-message-service` | 站内信、短信、邮件、消息模板、消息记录 | `synapse-mq` 只提供 MQ 技术抽象 |
| `synapse-file-service` | 文件管理、附件表、权限、预览、下载审计 | `synapse-file` 只提供文件存储抽象 |
| `synapse-config-service` | 配置管理、发布、审批、历史版本、后台页面 | `synapse-config` 只提供配置抽象和客户端能力 |
| `synapse-task-service` | 任务编排、调度管理、补偿、可视化运维 | Framework 只提供上下文与执行抽象 |
| `synapse-i18n-resource-center` | 国际化资源维护、翻译流程、停用语言管理 | `synapse-i18n` 只提供运行时解析抽象 |

## 6. 关键模块归属判定

### 6.1 synapse-config

Framework 中的 `synapse-config` 只能提供配置抽象、配置客户端、解析、缓存和刷新扩展点；不能提供配置管理 API、配置发布流程、配置表或可启动 config-service。

### 6.2 synapse-mq

`synapse-mq` 只能提供 MQ 技术抽象，例如消息模型、Header 规范、发布/消费 SPI、上下文传播、幂等 Key、重试分类、死信和顺序消息扩展点；不能提供站内信、短信、邮件、消息模板、消息中心后台或可启动 message-service。

### 6.3 synapse-file

`synapse-file` 只能提供文件存储技术抽象，例如 `FileStorageClient`、文件对象模型、元数据技术模型、上传/下载策略和 URL 签名抽象；不能提供文件管理 API、附件业务表、文件权限业务、文件审批、文件预览业务或可启动 file-service。

### 6.4 synapse-oauth2

二阶段 `synapse-oauth2` 只允许提供 JWT、JWK、Token 校验、Token denylist、Resource Server 辅助能力和 OAuth2 技术契约；不能提供授权服务实现、登录接口、用户认证业务、授权记录管理或 IAM 服务。

### 6.5 synapse-audit

`synapse-audit` 只能提供 `AuditEvent`、`AuditActor`、`AuditTarget`、`AuditAction`、`AuditLogPort` / `AuditRecorder`、OperationContext 对接和审计事件发布扩展点；不能提供审计查询 API、审计报表、审计中心后台、强绑定业务审计表或可启动 audit-service。

### 6.6 synapse-i18n

Framework 中的 `synapse-i18n` 只能提供运行时解析能力，例如 Locale 解析、I18n message resolver、资源 loader/cache、默认语言回退策略和错误码国际化扩展点；不能提供国际化资源管理后台、翻译审批流程、语言维护页面或可启动 i18n-resource-center。

### 6.7 synapse-webflux

`synapse-webflux` 只能提供 WebFlux 技术支撑，例如 WebFilter、TraceId / RequestId、ServerWebExchange Header 解析、Reactor Context、异常响应适配和 OperationContext 恢复；不能提供 Gateway 路由、Gateway 配置管理、网关业务鉴权、限流后台或可启动 gateway 服务。

### 6.8 synapse-task

`task-service` 属于 Platform。Framework 侧如未来需要任务相关能力，只能提供异步执行上下文传播、Job Actor 抽象、调度入口上下文恢复和任务执行技术契约；不能提供任务调度管理后台、可视化任务编排、任务数据库表或可启动 task-service。

## 7. 业务应用边界

业务应用可以依赖 Framework 或 Platform 暴露的 SDK / Client，但业务应用必须自己拥有业务 API、业务模型、业务表结构、业务权限码、业务流程和业务集成规则。

Framework 不应该反向依赖业务应用，也不应该为了某个业务系统定制具体逻辑。

## 8. 边界判断规则

当一个能力无法判断归属时，按以下问题判断：

1. 是否需要启动进程对外提供服务？如果是，归 Platform。
2. 是否需要 Controller 暴露 API？如果是，优先归 Platform 或 Business Application。
3. 是否需要业务表结构？如果是，归 Platform 或 Business Application。
4. 是否包含用户、角色、菜单、组织、消息模板、文件记录等业务语义？如果是，不能放入 Framework。
5. 是否只是技术契约、上下文传播、轻量默认实现或 adapter？如果是，可以考虑放入 Framework。
6. 是否只是为了降低引入成本而聚合依赖？如果是，不在本项目创建 starter。
7. 是否只是为了演示而创建可启动工程？如果是，不在本项目创建 demo / example / sample application。

## 9. 二阶段执行纪律

二阶段所有任务必须遵守：

- 先更新边界文档，再改代码。
- 每次只执行一个 TASK。
- 新增模块前必须先明确模块定位和禁止事项。
- 不为完整性过度设计。
- 不把 Platform 能力提前塞进 Framework。
- 不把示例应用、启动应用或后台管理能力放进 Framework。
- 不规划、不创建 starter。
- 不规划、不创建 demo / example / sample application。
