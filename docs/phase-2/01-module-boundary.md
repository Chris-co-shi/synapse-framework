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
- 二阶段规划模块在真正加入 reactor 前，只能作为路线图，不代表已实现能力。

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
├── synapse-time
└── synapse-starter-*   后置规划
```

模块变化：

| 类型 | 模块 | 说明 |
| --- | --- | --- |
| 保持 | `synapse-bom` | 继续做版本管理 |
| 保持 | `synapse-core` | 继续做核心契约 |
| 已拆分 | `synapse-web` | TASK-202 后不再作为正式模块 |
| 新增 | `synapse-webmvc` | Servlet MVC 技术支撑 |
| 新增 | `synapse-webflux` | WebFlux 技术支撑，不是 gateway |
| 新增规划 | `synapse-cloud` | Spring Cloud / Feign / 服务调用上下文传播 |
| 保持 | `synapse-data` | 数据层技术支撑 |
| 保持 | `synapse-cache` | 缓存、锁、限流、幂等技术支撑 |
| 保持 | `synapse-security` | 安全上下文、权限注解、trusted-header 技术支撑 |
| 保持并收紧 | `synapse-oauth2` | JWT / JWK / Token / Resource Server 技术辅助，不做 IAM |
| 保持 | `synapse-audit` | 审计事件和记录端口 |
| 保持 | `synapse-file` | 文件存储抽象，不做 file-service |
| 保持 | `synapse-mq` | MQ 技术抽象，不做 message-service |
| 新增规划 | `synapse-config` | 配置抽象，不做 config-service |
| 新增规划 | `synapse-i18n` | 国际化运行时抽象，不做资源中心 |
| 新增规划 | `synapse-time` | 时间和时区技术支撑，独立模块，不并入 core |
| 后置规划 | `synapse-starter-*` | 在模块边界稳定后再考虑 |

## 3. 当前模块边界

### 3.1 synapse-bom

当前状态：

- 正式 reactor module。
- 只负责 dependencyManagement 版本管理。

二阶段定位：

- 继续统一管理 Framework 内部模块和必要三方依赖版本。

允许内容：

- Maven dependencyManagement。
- 版本属性。
- 内部模块版本声明。

禁止内容：

- Java 代码。
- AutoConfiguration。
- 业务依赖聚合。
- starter 能力。

Platform 边界：

- Platform 可以 import framework BOM，但 Platform 自己的服务版本管理不应反向进入 framework。

### 3.2 synapse-core

当前状态：

- 正式 reactor module。
- 承载错误码、异常、ID、OperationContext 等核心契约。

二阶段定位：

- Framework 最底层通用技术契约。
- 继续作为其他技术模块的最小依赖。

允许内容：

- `ErrorCode`。
- `SynapseException`。
- `OperationContext`。
- `OperationContextHolder`。
- `OperationActor`。
- 通用上下文 key。
- 通用 ID 工具。
- 不依赖具体技术栈的工具类。

禁止内容：

- Spring Web 依赖。
- Spring Security 依赖。
- MyBatis 依赖。
- Redis / MQ / File SDK 依赖。
- 业务用户模型。
- 业务权限模型。
- 时间时区完整实现。

Platform 边界：

- Platform 可以复用 core 的通用上下文，但用户、角色、菜单、组织等平台模型不进入 core。

### 3.3 synapse-webmvc

当前状态：

- 正式 reactor module。
- 由原 `synapse-web` 的 Servlet MVC 能力迁移而来。
- 不包含 WebFlux / Gateway。

二阶段定位：

- Servlet MVC 技术支撑模块。

允许内容：

- MVC Result。
- MVC 全局异常处理。
- Servlet Filter 异常桥接。
- MVC Trace / RequestId。
- MVC Jackson 配置。
- MVC OpenAPI 可见性策略。

禁止内容：

- WebFlux WebFilter。
- Gateway 路由。
- Gateway 鉴权业务。
- 可启动 gateway。
- 业务 Controller。

Platform 边界：

- `synapse-gateway` 属于 Platform。
- Gateway 不能依赖 `synapse-webmvc`。
- Gateway 如需 Framework WebFlux 技术能力，应引用 `synapse-webflux`。

### 3.4 synapse-data

当前状态：

- 正式 reactor module。
- 提供 MyBatis-Plus 基础配置、ID 生成器、自动填充和 OperationContext 读取能力。

二阶段定位：

- 数据访问技术支撑模块。

允许内容：

- MyBatis-Plus 插件默认配置。
- ID 生成器。
- MetaObjectHandler。
- OperationContext 自动填充。
- 技术型 `BaseEntity` / `AuditableEntity` / `VersionedEntity`。
- 数据权限 SPI / Port 预留。
- SQL 拦截器扩展点。

禁止内容：

- 业务 Entity。
- 业务 Mapper。
- 业务 Repository。
- 业务 Service。
- 业务数据库 migration。
- 用户、角色、菜单、组织等业务表模型。
- 具体 ABAC / DataScope 业务规则。

Platform 边界：

- Platform 或业务应用实现具体数据模型和数据权限规则。
- Framework 只提供技术型填充和拦截扩展点。

### 3.5 synapse-cache

当前状态：

- 正式 reactor module。
- 提供缓存、锁、限流、幂等基础设施。

二阶段定位：

- 缓存和分布式基础能力模块。

允许内容：

- CacheKey 规范。
- CacheNamespace。
- 本地缓存抽象。
- Redis 缓存抽象。
- 分布式锁抽象。
- 限流抽象。
- 幂等抽象。
- 缓存刷新事件。

禁止内容：

- 业务缓存 key。
- 用户缓存。
- 菜单缓存。
- 字典缓存业务实现。
- 缓存管理后台。

Platform 边界：

- Platform 可以基于 cache 实现平台资源缓存，但具体缓存内容和刷新策略归 Platform。

### 3.6 synapse-security

当前状态：

- 正式 reactor module。
- 提供轻量安全上下文、trusted-header、密码编码器、PermissionChecker、权限注解适配。

二阶段定位：

- 安全上下文和声明式权限技术支撑，不是 IAM。

允许内容：

- `AuthenticatedUser` 技术模型。
- `SecurityContext`。
- trusted-header 解析。
- Header 签名校验。
- PermissionChecker 抽象。
- `@RequirePermission`。
- OperationContext 与 AuthenticatedUser 互转。
- WebMVC / WebFlux 安全上下文适配。

禁止内容：

- 登录接口。
- 用户表。
- 角色表。
- 菜单表。
- 权限管理后台。
- IAM 服务。
- OAuth2 Authorization Server 实现。

Platform 边界：

- `synapse-iam` 才负责用户、角色、菜单、资源、登录认证、授权管理。
- `synapse-security` 只负责运行时安全上下文和权限判断扩展点。

### 3.7 synapse-oauth2

当前状态：

- 正式 reactor module。
- 作为 OAuth2 / JWT / JWK 技术能力模块。

二阶段定位：

- Token / JWT / JWK / Resource Server 辅助能力。
- 不做 IAM，不做 Authorization Server 实现。

允许内容：

- JWT 解析。
- JWK 解析。
- Token 校验辅助。
- Token denylist 抽象。
- Resource Server 辅助配置。
- OAuth2 技术契约。

禁止内容：

- Authorization Server 实现。
- 登录接口。
- 登录页。
- OAuth2 Client 管理。
- 用户认证业务。
- 授权码流程持久化。
- 授权记录后台。
- IAM 服务。

Platform 边界：

- `synapse-iam` 才是可启动认证授权中心。
- Framework 只沉淀 OAuth2 相关底层技术能力。

### 3.8 synapse-audit

当前状态：

- 正式 reactor module。
- 提供审计事件契约和基础设施。

二阶段定位：

- 审计事件抽象和记录端口。

允许内容：

- `AuditEvent`。
- `AuditActor`。
- `AuditTarget`。
- `AuditAction`。
- `AuditLogPort`。
- `AuditRecorder`。
- OperationContext 对接。
- 审计事件发布扩展点。

禁止内容：

- 审计查询 API。
- 审计报表。
- 审计中心后台。
- 强绑定业务审计表。
- 可启动 audit-service。

Platform 边界：

- Platform 可以实现审计中心和审计查询服务。
- Framework 只定义审计事件和记录端口。

### 3.9 synapse-file

当前状态：

- 正式 reactor module。
- 提供文件存储抽象和本地轻量实现。

二阶段定位：

- 文件存储技术抽象，不是文件中心。

允许内容：

- `FileStorageClient`。
- `FileObject`。
- `FileMetadata`。
- `FileUploadPolicy`。
- `FileDownloadPolicy`。
- `FileUrlSigner`。
- 本地 / MinIO / S3 adapter 扩展点。

禁止内容：

- 上传下载 Controller。
- 文件管理后台。
- 附件业务表。
- 文件权限业务。
- 文件审批流程。
- 可启动 file-service。

Platform 边界：

- `synapse-file-service` 才负责文件管理、文件权限、附件业务和可启动服务。

### 3.10 synapse-mq

当前状态：

- 正式 reactor module。
- `synapse-message` 已改为 `synapse-mq`。
- 提供消息外壳、发布 / 消费模板、SPI、上下文传播契约。

二阶段定位：

- MQ 技术抽象，不是业务消息中心。

允许内容：

- 消息模型。
- Message Header 规范。
- MessagePublisher / MessageConsumer SPI。
- OperationContext 透传。
- TraceId 透传。
- 幂等 Key 规范。
- 消费重试分类。
- 死信扩展点。
- 顺序消息扩展点。

禁止内容：

- 站内信。
- 短信。
- 邮件。
- 消息模板管理。
- 消息记录查询 API。
- 消息中心后台。
- 可启动 message-service。

Platform 边界：

- `synapse-message-service` 才负责业务消息中心。
- Framework 只提供 MQ 基础设施契约。

## 4. 二阶段规划模块边界

### 4.1 synapse-webmvc

当前定位：

- Servlet MVC 技术支撑模块。
- 已由原 `synapse-web` 的 MVC 能力迁移而来。

允许内容：

- MVC Result。
- MVC ExceptionHandler。
- Servlet Filter。
- MVC Trace / RequestId。
- MVC OperationContext 恢复。
- MVC Jackson 配置。

禁止内容：

- WebFlux。
- Gateway。
- 业务 Controller。

### 4.2 synapse-webflux

当前定位：

- WebFlux 技术支撑模块，不是 Gateway 服务。

允许内容：

- WebFlux WebFilter。
- ServerWebExchange Header 解析。
- Reactor Context 适配。
- WebFlux 异常响应适配。
- OperationContext 恢复。

禁止内容：

- Gateway 路由服务。
- Gateway 配置管理。
- 网关业务鉴权。
- 可启动 gateway。

### 4.3 synapse-cloud

规划定位：

- Spring Cloud 微服务调用技术支撑模块。

允许内容：

- Feign RequestInterceptor。
- Feign ErrorDecoder。
- 服务间调用 Header 规范。
- OperationContext 透传。
- 内部调用签名扩展点。
- LoadBalancer 扩展点。

禁止内容：

- 注册中心服务。
- 配置中心服务。
- 服务治理后台。
- Gateway 服务。

### 4.4 synapse-config

规划定位：

- 统一配置抽象和运行时客户端能力。

允许内容：

- ConfigClient。
- ConfigResolver。
- ConfigParser。
- ConfigCache。
- ConfigChangeEvent。
- ConfigRefreshListener。
- JSON 配置解析。

禁止内容：

- Config Controller。
- 配置数据库表。
- 配置发布流程。
- 配置审批。
- 配置中心后台。
- 可启动 config-service。

### 4.5 synapse-i18n

规划定位：

- 国际化运行时解析抽象。

允许内容：

- LocaleResolver。
- I18nMessageResolver。
- I18nResourceLoader。
- I18nResourceCache。
- FallbackPolicy。
- 错误码国际化扩展点。

禁止内容：

- 国际化资源中心。
- 翻译审批。
- 语言维护后台。
- 可启动 i18n-resource-center。

### 4.6 synapse-time

规划定位：

- 时间和时区技术支撑模块。
- 独立模块，不并入 `synapse-core`。

允许内容：

- TimeZoneResolver。
- UserTimeZoneProvider。
- TimeRangeConverter。
- DateRangeQuery。
- UTC 存储规范。
- Jackson 时间序列化扩展。

禁止内容：

- 组织架构。
- 工厂管理。
- 用户资料管理。
- 时区配置后台。

### 4.7 synapse-starter-*

规划定位：

- 后置聚合能力。
- 只在模块边界稳定后再考虑。

允许内容：

- 按服务类型组合自动配置。
- 依赖聚合。
- 启用条件。

禁止内容：

- 启动应用。
- 示例应用。
- 业务代码。
- 平台服务代码。

## 5. 文档编写规则

所有模块文档必须区分：

- 当前事实：已经进入 reactor 并存在代码的能力。
- 二阶段规划：路线图中的目标能力。
- 禁止事项：不得进入 Framework 的内容。
- Platform 边界：应由 Platform 可启动服务承载的内容。

禁止把规划模块描述成已经实现的模块。
