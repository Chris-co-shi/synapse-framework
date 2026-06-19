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
├── synapse-mybatis-plus
├── synapse-datasource
├── synapse-cache
├── synapse-security
├── synapse-oauth2-core
├── synapse-oauth2-authorization-server-support
├── synapse-oauth2-resource-server-webmvc
├── synapse-oauth2-resource-server-webflux
├── synapse-audit
├── synapse-file
└── synapse-mq
```

说明：

- 未进入根 `pom.xml` reactor 的目录，不视为当前已实现模块。
- `synapse-web` 已在 TASK-202 中拆分为 `synapse-webmvc` 和 `synapse-webflux`，不再作为正式 reactor module。
- `synapse-oauth2` 已拆分为 `synapse-oauth2-core`、`synapse-oauth2-authorization-server-support`、`synapse-oauth2-resource-server-webmvc`、`synapse-oauth2-resource-server-webflux`，不再作为正式 reactor module。
- 身份 Header 恢复协议已经移除，不再作为正式模块或后续扩展方向。
- 身份认证只信任经过 Resource Server 验证的 Bearer Token；Gateway 不向下游传播可直接信任的用户、角色或权限 Header。
- 新规划模块在真正加入 reactor 前，只能作为路线图，不代表已实现能力。
- 本项目固定不创建 `synapse-starter-*`，不创建 demo / example / sample application。

## 2. 二阶段目标模块形态

二阶段目标形态如下：

```text
synapse-framework
├── synapse-bom
├── synapse-core
├── synapse-webmvc
├── synapse-webflux
├── synapse-cloud
├── synapse-data
├── synapse-mybatis-plus
├── synapse-datasource
├── synapse-cache
├── synapse-security
├── synapse-oauth2-core
├── synapse-oauth2-authorization-server-support
├── synapse-oauth2-resource-server-webmvc
├── synapse-oauth2-resource-server-webflux
├── synapse-audit
├── synapse-file
├── synapse-mq
├── synapse-config
├── synapse-i18n
└── synapse-time
```

固定排除项：

```text
身份 Header 恢复协议     不提供
synapse-starter-*        不创建
examples / demos         不创建
sample applications      不创建
可启动示例工程           不创建
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
| 收紧 | `synapse-data` | ORM 无关的数据语义抽象 |
| 新增 | `synapse-mybatis-plus` | MyBatis-Plus 工程增强 |
| 新增 | `synapse-datasource` | 数据源治理模块 |
| 保持 | `synapse-cache` | 缓存、锁、限流、幂等技术支撑 |
| 收紧 | `synapse-security` | Web 无关安全主体、上下文、权限检查和密码编码 |
| 已拆分 | `synapse-oauth2` | 不再作为正式模块 |
| 新增 | `synapse-oauth2-core` | JWT claim、token、validator、denylist 和 BearerTokenProvider 契约 |
| 新增 | `synapse-oauth2-authorization-server-support` | JWT 签发、RSAKey、JWKSource、JwtEncoder 技术支持 |
| 新增 | `synapse-oauth2-resource-server-webmvc` | Servlet OAuth2 Resource Server 技术适配 |
| 新增 | `synapse-oauth2-resource-server-webflux` | Reactive OAuth2 Resource Server 技术适配 |
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

禁止内容：WebFlux WebFilter、Gateway 路由、Gateway 鉴权业务、可启动 gateway、业务 Controller、OAuth2 Resource Server 认证能力。

Platform 边界：`synapse-gateway` 属于 Platform；Gateway 不能依赖 `synapse-webmvc`。

### 3.4 synapse-webflux

当前状态：正式 reactor module，提供 WebFlux 技术支撑，不是 Gateway 服务。

允许内容：WebFlux WebFilter、ServerWebExchange Header 解析、Reactor Context 适配、WebFlux 异常响应适配、OperationContext 恢复。

禁止内容：Gateway 路由服务、Gateway 配置管理、网关业务鉴权、可启动 gateway、OAuth2 Resource Server 认证能力。

### 3.5 synapse-cloud

当前状态：正式 reactor module，提供 Spring Cloud / OpenFeign / 服务间调用技术支撑。

允许内容：Feign RequestInterceptor、Feign ErrorDecoder、服务间调用 Header 规范、OperationContext 到 HTTP Header 的编码、HTTP Header 到 OperationContext 的轻量恢复辅助、TraceId / RequestId 透传、internal-call marker、内部调用签名扩展点、Cloud properties、条件自动配置。

当前未实现内容：LoadBalancer 扩展点、Resilience4j 扩展点、完整内部调用签名认证体系。

禁止内容：注册中心服务、配置中心服务、服务治理后台、Gateway 服务、Gateway RouteLocator、Gateway Filter 业务逻辑、IAM、登录认证、用户/角色/菜单、业务权限判断、Nacos 配置管理、Seata 事务协调、RocketMQ adapter、业务服务 SDK、传播 roles / permissions / menu codes / organization tree / raw token / password / credential / business data。

依赖边界：可以依赖 `synapse-core`、`spring-boot-autoconfigure`、`spring-cloud-openfeign-core` 或 `feign-core`，可以使用 Jackson 解析远程错误响应；禁止依赖 `synapse-webmvc`、`synapse-webflux`、`synapse-security`、`synapse-mq`、`spring-cloud-starter-gateway`、Nacos、Seata、RocketMQ 或业务模块。

Bearer Token 的服务间转发由 OAuth2 的 `BearerTokenProvider` 与调用方适配负责；不得把 Token 拆成用户、角色或权限 Header。

Header 契约：详见 `docs/phase-2/04-cloud-context-propagation.md`。

### 3.6 synapse-data

当前状态：正式 reactor module，提供 ORM 无关的数据语义抽象。

允许内容：分页模型、排序模型、审计字段名、通用数据字段名、ORM 无关数据语义接口。

禁止内容：MyBatis-Plus、dynamic-datasource、Flyway、Spring Boot AutoConfiguration、BaseEntity、Mapper、Repository、DataSource 配置、MetaObjectHandler、IdentifierGenerator、租户字段、数据权限、SQL 路由、业务 Entity、业务 Mapper、业务 Repository、业务 Service、业务数据库 migration。

### 3.7 synapse-mybatis-plus

当前状态：正式 reactor module，提供 MyBatis-Plus 工程增强。

允许内容：MyBatis-Plus starter 接入、JSqlParser 接入、MyBatis-Plus 自动配置、`MybatisPlusInterceptor`、分页插件、乐观锁插件、防全表 update/delete 插件、非法 SQL 插件开关、自动字段填充、MyBatis-Plus ID 生成适配、分页模型适配。

禁止内容：业务 Entity、业务 Mapper、业务 Repository、业务 Service、业务数据库 migration、DataSource 治理、Seata、SQL 自动读写路由、应用层主库晋升。

### 3.8 synapse-datasource

当前状态：正式 reactor module，提供数据源治理能力。

允许内容：dynamic-datasource 基础接入、数据源命名和分组规范、数据源元信息识别、数据库类型识别、数据源角色识别、数据库连接安全检测、健康检查、健康状态注册表、故障数据源摘除、故障恢复检测、读库 Load Balance、Router 抽象、Failover / Failback 抽象、启动诊断、运行时状态查询基础模型。

禁止内容：`@DS` 封装、`@MasterDS`、`@ReadOnlyDS`、业务显式切换数据源 API、Seata 集成、MyBatis SQL 自动读写路由拦截器、应用层主库晋升、业务 Entity / Mapper / Repository / Service。

### 3.9 synapse-cache

当前状态：正式 reactor module，提供缓存、锁、限流、幂等基础设施。

允许内容：CacheKey 规范、CacheNamespace、本地缓存抽象、Redis 缓存抽象、分布式锁抽象、限流抽象、幂等抽象、缓存刷新事件。

禁止内容：业务缓存 key、用户缓存、菜单缓存、字典缓存业务实现、缓存管理后台。

### 3.10 synapse-security

当前状态：正式 reactor module，提供 Web 无关安全主体、SecurityContext、密码编码器、PermissionChecker 和权限注解适配。

允许内容：`AuthenticatedPrincipal`、`AuthenticatedUser`、`AuthenticatedClient` 技术模型、`SecurityContext`、PermissionChecker 抽象、`@RequirePermission`、OperationContext 与安全主体的单向适配、PasswordEncoder 默认实现。

禁止内容：身份 Header 恢复协议、用户/角色/权限身份 Header 解析、Servlet Filter、WebFilter、Spring Security FilterChain、登录接口、用户表、角色表、菜单表、权限管理后台、IAM 服务、OAuth2 Authorization Server 或 Resource Server 实现。

认证主体只能由 OAuth2 Resource Server 等专用适配模块在完成 Token 验证后建立。

### 3.11 synapse-oauth2-core

当前状态：正式 reactor module，作为 OAuth2 / JWT 协议无关基础契约模块。

允许内容：JWT claim 常量、token 类型、claim validator、Token denylist 端口、BearerTokenProvider、OAuth2 技术错误码。

禁止内容：Spring Security、WebMVC、WebFlux、私钥生成、JwtEncoder、Resource Server FilterChain、Authorization Server、登录认证、IAM。

### 3.12 synapse-oauth2-authorization-server-support

当前状态：正式 reactor module，只提供授权服务器侧可复用的 JWT 签发技术支撑。

允许内容：RSAKey/JWKSource 技术封装、JwtEncoder、JWT claim 写出、开发密钥保护策略、签发属性和自动配置。

禁止内容：Authorization Server 业务实现、RegisteredClient 管理、授权码流程、登录页、用户认证、客户端管理后台、IAM。

### 3.13 synapse-oauth2-resource-server-webmvc

当前状态：正式 reactor module，提供 Servlet OAuth2 Resource Server 技术适配。

允许内容：JwtDecoder 条件装配、JWT 到 `AuthenticatedPrincipal` 映射、Servlet SecurityContext Bridge、统一 401/403 响应适配、denylist validator。

禁止内容：JWT 签发私钥、JwtEncoder、Authorization Server、登录认证、IAM、用户/角色/菜单管理、WebFlux/Gateway。

### 3.14 synapse-oauth2-resource-server-webflux

当前状态：正式 reactor module，提供 Reactive OAuth2 Resource Server 技术适配。

允许内容：ReactiveJwtDecoder 条件装配、JWT 到 `AuthenticatedPrincipal` 映射、Reactor Context 安全/操作上下文读取、统一 401/403 响应适配。

禁止内容：JWT 签发私钥、JwtEncoder、Authorization Server、登录认证、IAM、Gateway 路由服务、网关业务鉴权。

### 3.15 synapse-audit

当前状态：正式 reactor module，提供审计事件契约和基础设施。

允许内容：`AuditEvent`、`AuditActor`、`AuditTarget`、`AuditAction`、`AuditLogPort`、`AuditRecorder`、OperationContext 对接、审计事件发布扩展点。

禁止内容：审计查询 API、审计报表、审计中心后台、强绑定业务审计表、可启动 audit-service。

### 3.16 synapse-file

当前状态：正式 reactor module，提供文件存储抽象和本地轻量实现。

允许内容：`FileStorageClient`、`FileObject`、`FileMetadata`、`FileUploadPolicy`、`FileDownloadPolicy`、`FileUrlSigner`、本地 / MinIO / S3 adapter 扩展点。

禁止内容：上传下载 Controller、文件管理后台、附件业务表、文件权限业务、文件审批流程、可启动 file-service。

### 3.17 synapse-mq

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

- 身份 Header 恢复模块。
- 用户、角色或权限身份 Header 协议。
- `synapse-starter-*`。
- demo application。
- example application。
- sample application。
- 任何可启动示例工程。

原因：

- 身份权威统一来自经过密码学验证的 Bearer Token。
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
