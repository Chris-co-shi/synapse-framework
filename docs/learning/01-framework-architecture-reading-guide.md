# Framework 架构阅读指南

## 1. 先建立三层边界

Synapse 的代码不能只按 Maven module 理解，还要先区分三个产品层级：

```text
Business Application
  -> 拥有业务模型、业务接口、业务数据库和业务权限标识
  -> 可以依赖 Platform SDK / Client 和 Framework

Synapse Platform
  -> 拥有 Gateway、IAM、Message、File、Config、Task 等可启动平台服务
  -> 依赖 Framework

Synapse Framework
  -> 只提供通用技术契约、默认实现、自动配置和适配器
  -> 不拥有业务数据和平台运行时服务
```

阅读任意类时先问：它是技术契约，还是平台业务，还是具体业务代码。Framework 中只能出现第一类。

## 2. 当前模块地图

### 2.1 基础契约层

```text
synapse-core
synapse-time
synapse-config
synapse-i18n
synapse-oauth2-core
synapse-security
```

这一层定义稳定模型、错误、上下文、SPI 和协议无关契约。

重点：

- `synapse-core` 是最底层上下文和错误契约。
- `synapse-security` 定义 Web 无关认证主体和权限检查。
- `synapse-oauth2-core` 定义 JWT claim、token、validator、denylist 等协议契约。
- 基础契约层不应反向依赖具体 Servlet、WebFlux、数据库或业务服务。

### 2.2 Web 与协议适配层

```text
synapse-webmvc
synapse-webflux
synapse-oauth2-resource-server-webmvc
synapse-oauth2-resource-server-webflux
synapse-oauth2-authorization-server-support
synapse-cloud
```

这一层把基础契约接入 Spring MVC、WebFlux、Spring Security、Authorization Server 或 OpenFeign。

典型模式：

```text
外部框架对象
  -> Converter / Resolver / Filter / WebFilter
  -> Synapse 核心模型
  -> Synapse Context
```

身份认证适配只接受经过验证的 Bearer Token，不提供身份 Header 恢复模块。

### 2.3 基础设施能力层

```text
synapse-data
synapse-cache
synapse-audit
synapse-file
synapse-mq
```

这一层提供数据自动填充、缓存并发、审计事件、文件存储和消息传播的技术能力。

它们通常读取 `OperationContext`，但不应该为了获取当前用户而直接依赖 `synapse-security`。

### 2.4 版本管理层

```text
synapse-bom
```

只负责统一依赖版本，不承载运行时代码。

## 3. 最重要的依赖方向

### 3.1 Security 到 Core 是单向适配

```text
AuthenticatedPrincipal
  -> SecurityOperationContextAdapter
  -> OperationActor
  -> OperationContext
```

这样设计的目的，是让 data、audit、mq 只依赖 core 的“当前操作人”语义，而不依赖认证方式。

因此下面的依赖是正确的：

```text
security -> core
data     -> core
audit    -> core
mq       -> core
```

下面的依赖应避免：

```text
data -> security
audit -> security
mq -> security
```

### 3.2 OAuth2 Resource Server 到 Security 是适配关系

```text
Jwt
  -> SynapseJwtPrincipalMapper
  -> AuthenticatedUser / AuthenticatedClient
  -> Synapse SecurityContext
```

`oauth2-resource-server-*` 负责把 OAuth2/JWT 世界中的对象转换成 Synapse 安全模型。

`security` 本身不解析 JWT，也不创建 `SecurityFilterChain`。

Gateway 可以做入口 Token 验证，但下游服务必须继续验证自己的 issuer、audience、有效期和签名；用户、角色与权限 Header 不能替代 Token。

### 3.3 WebMVC 与 WebFlux 必须分离

Servlet 使用线程绑定上下文，Reactive 使用 Reactor Context。二者生命周期和传播方式不同，不能通过一个通用 Filter 强行统一。

```text
Servlet MVC
  -> OncePerRequestFilter
  -> ThreadLocal scope

WebFlux
  -> WebFilter
  -> Reactor Context
```

可以统一模型和语义，但不能假装执行模型相同。

## 4. 四类常见代码角色

### 4.1 Contract / Port

定义消费方可以依赖的稳定接口，例如：

- `OperationContextProvider`
- `PermissionChecker`
- `TokenDenylistPort`
- `FileStorage`
- `MessagePublisher`

阅读重点：输入、输出、失败语义、线程或上下文要求。

### 4.2 Model

表达框架内部稳定语义，例如：

- `OperationContext`
- `OperationActor`
- `AuthenticatedUser`
- `AuthenticatedClient`
- `MessageEnvelope`

阅读重点：字段不变量、是否可空、是否为快照、是否允许跨进程传播。

### 4.3 Adapter

连接外部框架与 Synapse 模型，例如：

- JWT principal mapper
- Servlet Filter
- WebFlux WebFilter
- MyBatis-Plus MetaObjectHandler
- OpenFeign RequestInterceptor

阅读重点：外部输入如何转换、清理发生在哪里、异常由谁接管。

### 4.4 AutoConfiguration

根据 classpath、配置项和用户自定义 Bean 装配默认实现。

阅读重点：

- `@ConditionalOnClass`
- `@ConditionalOnMissingBean`
- `@ConditionalOnProperty`
- 默认 Bean 是否允许消费方覆盖
- 自动配置是否越过模块边界创建了业务对象

## 5. 推荐源码阅读顺序

不要先看自动配置中的所有 Bean。建议按下面顺序：

```text
1. 模块使用手册
2. 核心模型或 Port
3. 默认实现
4. 外部框架 Adapter
5. AutoConfiguration
6. 配置属性
7. 测试
```

原因是自动配置只是“如何把对象组装起来”，它不是核心语义本身。

## 6. 第一条建议追踪的链路

建议从 Servlet OAuth2 Resource Server 开始：

```text
HTTP Bearer Token
  -> Spring Security BearerTokenAuthenticationFilter
  -> JwtDecoder / validators
  -> SynapseJwtAuthenticationConverter
  -> SynapseJwtPrincipalMapper
  -> SynapseJwtAuthenticationToken
  -> SynapseSecurityContextBridgeFilter
  -> SecurityContextBinder.bind
  -> OperationContext
  -> Controller / Service / PermissionChecker
```

详细说明见 [Security 与 OAuth2 请求链路](02-security-oauth2-request-flow.md)。

## 7. 阅读一个类时的记录模板

```text
类名：
所属模块：
角色：Contract / Model / Adapter / Default Implementation / AutoConfiguration
输入：
输出：
上游调用者：
下游依赖：
异常边界：
上下文生命周期：
为什么属于当前模块：
为什么不能放到相邻模块：
```

## 8. 设计是否合理的快速检查

看到新增代码时，至少检查：

- 是否把业务语义放进 Framework。
- 是否让低层模块依赖高层适配器。
- 是否通过服务器默认时区解释业务时间。
- 是否把 Bearer Token 写入日志、MQ Header 或 OperationContext。
- 是否在线程池、异步、MQ 或任务结束后遗留 ThreadLocal。
- 是否把 CLIENT 主体伪装成 USER。
- 是否因自动配置抢占了消费方自定义 Bean。
- 是否把 Servlet 和 Reactive 生命周期混在一起。
- 是否重新引入可伪造的用户、角色或权限身份 Header。

先掌握这些边界，再深入具体实现，代码会从“很多类”变成少数几个稳定模式。
