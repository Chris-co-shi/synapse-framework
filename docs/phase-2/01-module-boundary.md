# 01-Module Boundary

本文档用于说明 Synapse-Framework 当前 reactor module 事实、二阶段目标模块形态，以及每个模块的允许内容与禁止内容。

## 1. 当前 reactor modules

当前正式模块以根 `pom.xml` 的 `<modules>` 为准。

```text
synapse-framework
├── synapse-bom
├── synapse-core
├── synapse-webmvc
├── synapse-webflux
├── synapse-cloud
├── synapse-time
├── synapse-config
├── synapse-i18n
├── synapse-data
├── synapse-cache
├── synapse-security
├── synapse-oauth2
├── synapse-audit
├── synapse-file
└── synapse-mq
```

说明：

- 未进入根 `pom.xml` reactor 的目录，不视为当前已实现模块。
- `synapse-task`、`synapse-tenant`、`synapse-data-permission` 若目录存在，也只能视为暂存目录或历史残留，不能按正式 module 使用。
- `synapse-web` 已在 TASK-202 中拆分为 `synapse-webmvc` 和 `synapse-webflux`，不再作为正式 reactor module。
- 新规划模块在真正加入 reactor 前，只能作为路线图，不代表已实现能力。
- 本项目固定不创建 `synapse-starter-*`，不创建 demo / example / sample application。

## 2. 二阶段目标模块形态

二阶段建议目标形态如下：

```text
synapse-framework
├── synapse-bom
├── synapse-core
├── synapse-webmvc
├── synapse-webflux
├── synapse-cloud
├── synapse-data
├── synapse-cache
├── synapse-security
├── synapse-oauth2
├── synapse-audit
├── synapse-file
├── synapse-mq
├── synapse-config
├── synapse-i18n
└── synapse-time
```

固定排除项：

```text
synapse-starter-*      不创建
examples / demos       不创建
sample applications    不创建
可启动示例工程         不创建
```

模块变化：

| 类型 | 模块 | 说明 |
| --- | --- | --- |
| 保持 | `synapse-bom` | 继续做版本管理 |
| 保持 | `synapse-core` | 继续做核心契约 |
| 已拆分 | `synapse-web` | TASK-202 后不再作为正式模块 |
| 新增 | `synapse-webmvc` | Servlet MVC 技术支撑 |
| 新增 | `synapse-webflux` | WebFlux 技术支撑，不是 gateway |
| 新增 | `synapse-cloud` | Spring Cloud / Feign / 服务调用上下文传播 |
| 保持 | `synapse-data` | 数据层技术支撑 |
| 保持 | `synapse-cache` | 缓存、锁、限流、幂等技术支撑 |
| 保持 | `synapse-security` | 安全上下文、权限注解、trusted-header 技术支撑 |
| 保持并收紧 | `synapse-oauth2` | JWT / JWK / Token / Resource Server 技术辅助，不做 IAM |
| 保持 | `synapse-audit` | 审计事件和记录端口 |
| 保持 | `synapse-file` | 文件存储抽象，不做 file-service |
| 保持 | `synapse-mq` | MQ 技术抽象，不做 message-service |
| 已新增 | `synapse-config` | 配置抽象，不做 config-service |
| 已新增 | `synapse-i18n` | 国际化运行时抽象，不做资源中心 |
| 已新增 | `synapse-time` | 时间和时区技术支撑，独立模块，不并入 core |

## 3. 当前模块边界

### 3.1 synapse-bom

当前状态：正式 reactor module，只负责 dependencyManagement 版本管理。

允许内容：Maven dependencyManagement、版本属性、内部模块版本声明。

禁止内容：Java 代码、AutoConfiguration、业务依赖聚合、starter 能力。

Platform 边界：Platform 可以 import framework BOM，但 Platform 自己的服务版本管理不应反向进入 framework。

### 3.2 synapse-core

当前状态：正式 reactor module，承载错误码、异常、ID、OperationContext 等核心契约。

允许内容：`ErrorCode`、`SynapseException`、`OperationContext`、`OperationContextHolder`、`OperationActor`、通用上下文 key、通用 ID 工具、不依赖具体技术栈的工具类。

禁止内容：Spring Web / Spring Security / MyBatis / Redis / MQ / File SDK 依赖，业务用户模型，业务权限模型，时间时区完整实现。

Platform 边界：Platform 可以复用 core 的通用上下文，但用户、角色、菜单、组织等平台模型不进入 core。

### 3.3 synapse-webmvc

当前状态：正式 reactor module，由原 `synapse-web` 的 Servlet MVC 能力迁移而来，不包含 WebFlux / Gateway。

允许内容：MVC Result、MVC 全局异常处理、Servlet Filter 异常桥接、MVC Trace / RequestId、MVC Jackson 配置、MVC OpenAPI 可见性策略。

禁止内容：WebFlux WebFilter、Gateway 路由、Gateway 鉴权业务、可启动 gateway、业务 Controller。

Platform 边界：`synapse-gateway` 属于 Platform；Gateway 不能依赖 `synapse-webmvc`。

### 3.4 synapse-webflux

当前状态：正式 reactor module，提供 WebFlux 技术支撑，不是 Gateway 服务。

允许内容：WebFlux WebFilter、ServerWebExchange Header 解析、Reactor Context 适配、WebFlux 异常响应适配、OperationContext 恢复。

禁止内容：Gateway 路由服务、Gateway 配置管理、网关业务鉴权、可启动 gateway。

### 3.5 synapse-cloud

当前状态：正式 reactor module，提供 Spring Cloud / OpenFeign / 服务间调用技术支撑。

允许内容：Feign RequestInterceptor、Feign ErrorDecoder、服务间调用 Header 规范、OperationContext 到 HTTP Header 的编码、HTTP Header 到 OperationContext 的轻量恢复辅助、TraceId / RequestId 透传、internal-call marker、内部调用签名扩展点、Cloud properties、条件自动配置。

当前未实现内容：LoadBalancer 扩展点、Resilience4j 扩展点、完整内部调用签名认证体系。

禁止内容：注册中心服务、配置中心服务、服务治理后台、Gateway 服务、Gateway RouteLocator、Gateway Filter 业务逻辑、IAM、登录认证、用户/角色/菜单、业务权限判断、Nacos 配置管理、Seata 事务协调、RocketMQ adapter、业务服务 SDK、传播 roles / permissions / menu codes / organization tree / raw token / password / credential / business data。

依赖边界：可以依赖 `synapse-core`、`spring-boot-autoconfigure`、`spring-cloud-openfeign-core` 或 `feign-core`，可以使用 Jackson 解析远程错误响应；禁止依赖 `synapse-webmvc`、`synapse-webflux`、`synapse-security`、`synapse-mq`、`spring-cloud-starter-gateway`、Nacos、Seata、RocketMQ 或业务模块。

Header 契约：详见 `docs/phase-2/04-cloud-context-propagation.md`。

### 3.6 synapse-data

当前状态：正式 reactor module，提供 MyBatis-Plus 基础配置、ID 生成器、自动填充和 OperationContext 读取能力。

允许内容：MyBatis-Plus 插件默认配置、ID 生成器、MetaObjectHandler、OperationContext 自动填充、技术型 `BaseEntity` / `AuditableEntity` / `VersionedEntity`、数据权限 SPI / Port 预留、SQL 拦截器扩展点。

禁止内容：业务 Entity、业务 Mapper、业务 Repository、业务 Service、业务数据库 migration、用户/角色/菜单/组织等业务表模型、具体 ABAC / DataScope 业务规则。

### 3.7 synapse-cache

当前状态：正式 reactor module，提供缓存、锁、限流、幂等基础设施。

允许内容：CacheKey 规范、CacheNamespace、本地缓存抽象、Redis 缓存抽象、分布式锁抽象、限流抽象、幂等抽象、缓存刷新事件。

禁止内容：业务缓存 key、用户缓存、菜单缓存、字典缓存业务实现、缓存管理后台。

### 3.8 synapse-security

当前状态：正式 reactor module，提供轻量安全上下文、trusted-header、密码编码器、PermissionChecker、权限注解适配。

允许内容：`AuthenticatedUser` 技术模型、`SecurityContext`、trusted-header 解析、Header 签名校验、PermissionChecker 抽象、`@RequirePermission`、OperationContext 与 AuthenticatedUser 互转、WebMVC / WebFlux 安全上下文适配。

禁止内容：登录接口、用户表、角色表、菜单表、权限管理后台、IAM 服务、OAuth2 Authorization Server 实现。

### 3.9 synapse-oauth2

当前状态：正式 reactor module，作为 OAuth2 / JWT / JWK 技术能力模块。

允许内容：JWT 解析、JWK 解析、Token 校验辅助、Token denylist 抽象、Resource Server 辅助配置、OAuth2 技术契约。

禁止内容：Authorization Server 实现、登录接口、登录页、OAuth2 Client 管理、用户认证业务、授权码流程持久化、授权记录后台、IAM 服务。

### 3.10 synapse-audit

当前状态：正式 reactor module，提供审计事件契约和基础设施。

允许内容：`AuditEvent`、`AuditActor`、`AuditTarget`、`AuditAction`、`AuditLogPort`、`AuditRecorder`、OperationContext 对接、审计事件发布扩展点。

禁止内容：审计查询 API、审计报表、审计中心后台、强绑定业务审计表、可启动 audit-service。

### 3.11 synapse-file

当前状态：正式 reactor module，提供文件存储抽象和本地轻量实现。

允许内容：`FileStorageClient`、`FileObject`、`FileMetadata`、`FileUploadPolicy`、`FileDownloadPolicy`、`FileUrlSigner`、本地 / MinIO / S3 adapter 扩展点。

禁止内容：上传下载 Controller、文件管理后台、附件业务表、文件权限业务、文件审批流程、可启动 file-service。

### 3.12 synapse-mq

当前状态：正式 reactor module，`synapse-message` 已改为 `synapse-mq`，提供消息外壳、发布 / 消费模板、SPI、上下文传播契约。

允许内容：消息模型、Message Header 规范、MessagePublisher / MessageConsumer SPI、OperationContext 透传、TraceId 透传、幂等 Key 规范、消费重试分类、死信扩展点、顺序消息扩展点。

禁止内容：站内信、短信、邮件、消息模板管理、消息记录查询 API、消息中心后台、可启动 message-service。

## 4. TASK-205 已新增模块边界

### 4.1 synapse-config

当前状态：正式 reactor module，提供统一配置抽象、运行时读取和类型解析能力。

允许内容：ConfigClient、ConfigResolver、ConfigParser、轻量本地配置客户端、运行时配置读取和解析扩展点。

禁止内容：Config Controller、配置数据库表、配置发布流程、配置审批、配置中心后台、可启动 config-service。

### 4.2 synapse-i18n

当前状态：正式 reactor module，提供国际化运行时解析抽象。

允许内容：LocaleResolver、I18nMessageResolver、I18nResourceLoader、轻量本地资源加载、错误码国际化扩展点。

禁止内容：国际化资源中心、翻译审批、语言维护后台、可启动 i18n-resource-center。

### 4.3 synapse-time

当前状态：正式 reactor module，提供时间和时区技术支撑，独立模块，不并入 `synapse-core`。

允许内容：TimeZoneResolver、TimeRangeConverter、UTC 查询范围转换、时间存储和查询规范。

禁止内容：组织架构、工厂管理、用户资料管理、时区配置后台。

## 5. 明确不做的模块形态

以下能力不进入 Synapse-Framework：

- `synapse-starter-*`。
- demo application。
- example application。
- sample application。
- 任何可启动示例工程。

原因：

- 业务系统应按需直接引用具体 module。
- starter 容易掩盖依赖边界。
- demo / example / sample application 容易被误解为可启动平台服务。
- Framework 只交付 module、抽象、自动配置、测试、文档和 Skill。

## 6. 文档编写规则

所有模块文档必须区分：

- 当前事实：已经进入 reactor 并存在代码的能力。
- 二阶段规划：路线图中的目标能力。
- 禁止事项：不得进入 Framework 的内容。
- Platform 边界：应由 Platform 可启动服务承载的内容。

禁止把规划模块描述成已经实现的模块。
禁止把 starter / demo / example / sample application 作为后续正向规划。
