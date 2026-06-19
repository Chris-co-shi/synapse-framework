# synapse-cloud 使用手册

## 1. 模块定位

`synapse-cloud` 是 Synapse Framework 的 Spring Cloud / OpenFeign 服务间调用技术支撑模块。

它只提供：

- 服务间调用 Header 契约。
- `OperationContext` 到 HTTP Header 的编码。
- Feign `RequestInterceptor` 出站上下文传播。
- Feign `ErrorDecoder` 远程错误解码。
- 内部调用签名扩展点。
- Spring Boot 条件自动配置。

`synapse-cloud` 不是 Gateway，不是注册中心，不是配置中心，不是服务治理后台，也不是 IAM。

## 2. 适用场景

业务系统或平台系统在以下场景可以引入 `synapse-cloud`：

- 使用 Spring Cloud OpenFeign 调用内部服务。
- 需要在 Feign 出站请求中透传 `traceId` / `requestId`。
- 需要将当前 `OperationContext` 中的 actor、initiator、source、tenantId 写入服务间 Header。
- 需要统一把远程错误响应转换为 `RemoteCallException`。
- 需要为内部调用签名预留扩展点。

## 3. 不适用场景

`synapse-cloud` 不承担以下职责：

- Gateway 启动服务。
- Gateway 路由。
- Gateway Filter 业务逻辑。
- 注册中心。
- 配置中心。
- 服务治理后台。
- IAM。
- 登录认证。
- 用户、角色、菜单、组织等业务模型。
- 业务权限判断。
- Nacos 配置管理。
- Seata 事务协调。
- RocketMQ adapter。
- 业务服务 SDK。

## 4. Maven 引入方式

推荐先引入 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.indigo.synapse</groupId>
            <artifactId>synapse-bom</artifactId>
            <version>${synapse.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

再引入 cloud 模块：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-cloud</artifactId>
</dependency>
```

该模块依赖 `synapse-core`、Spring Boot AutoConfigure、Spring Cloud OpenFeign Core 和 Jackson。它不依赖 `synapse-webmvc`、`synapse-webflux`、`synapse-security`、`synapse-mq`、Gateway、Nacos、Seata 或 RocketMQ。

## 5. Header 契约

基础追踪：

```text
X-Trace-Id
X-Request-Id
```

Synapse 上下文：

```text
X-Synapse-Context-Version
X-Synapse-Tenant-Id
```

Actor：

```text
X-Synapse-Actor-Type
X-Synapse-Actor-Id
X-Synapse-Actor-Name
```

Initiator：

```text
X-Synapse-Initiator-Type
X-Synapse-Initiator-Id
X-Synapse-Initiator-Name
```

Source：

```text
X-Synapse-Source-Type
X-Synapse-Source-Name
X-Synapse-Source-Instance-Id
X-Synapse-Source-Entrypoint
```

Locale / Timezone：

```text
X-Synapse-Locale
X-Synapse-Time-Zone
```

Internal Call 扩展：

```text
X-Synapse-Internal-Call
X-Synapse-Internal-Caller
X-Synapse-Timestamp
X-Synapse-Nonce
X-Synapse-Signature
```

禁止通过服务间 Header 传播：

- roles。
- permissions。
- menu codes。
- organization tree。
- raw token。
- password。
- credential。
- business data。

## 6. 核心能力

### 6.1 OperationContext HTTP Header Codec

核心类型：

```java
OperationContextHttpHeaderCodec
SynapseCloudHeaders
HttpHeaderReader
HttpHeaderWriter
```

编码规则：

- HTTP Header codec 内部复用 `synapse-core` 的 `OperationContextSnapshotCodec`。
- 有值才写 Header。
- null / blank 不写。
- 缺少 actor 时不伪造 actor。
- 不默认创建 system actor。
- 已存在 Header 默认不覆盖。
- 配置允许时可以覆盖。
- 不写 roles、permissions、raw token、password、credential 或业务数据。

### 6.2 Feign RequestInterceptor

核心类型：

```java
SynapseFeignRequestInterceptor
SynapseFeignProperties
```

处理流程：

```text
OperationContextProvider
  -> OperationContextHttpHeaderCodec
  -> Feign RequestTemplate Header
```

默认行为：

- 有 `OperationContext` 时写入 traceId、requestId、tenantId、actor、initiator、source。
- 无 `OperationContext` 时不写身份字段。
- 已有 Header 默认不覆盖。
- `synapse.cloud.feign.override-existing-headers=true` 时允许覆盖。
- `synapse.cloud.feign.internal-signature-enabled=true` 时调用 `InternalCallSigner`。

### 6.3 Feign ErrorDecoder

核心类型：

```java
SynapseFeignErrorDecoder
RemoteCallException
RemoteErrorResponse
RemoteErrorBodyParser
CloudErrorCode
```

默认解析：

- 标准 JSON 中的 `code`。
- 标准 JSON 中的 `message`。
- 标准 JSON 中的 `traceId`。
- remote HTTP status。
- methodKey。
- response body 摘要。

非标准 JSON 或空 body 会降级为通用 `RemoteCallException`。

该能力不依赖 `synapse-webmvc.Result` 或 `synapse-webflux.Result`。

### 6.4 内部调用签名扩展点

核心类型：

```java
InternalCallSigner
InternalCallVerifier
InternalCallSignRequest
NoopInternalCallSigner
```

默认 `NoopInternalCallSigner` 不写任何 Header，避免 framework 默认建立伪认证体系。

签名扩展点只用于出站服务间调用技术签名，不代表 IAM、登录认证、业务鉴权或 Gateway 鉴权。GatewayProof 是入站可信入口证明，定义在 `synapse-security` 并由 OAuth2 Resource Server 适配模块前置校验；它不属于 `synapse-cloud` 的 Feign 出站签名能力。

## 7. 自动配置

自动配置类：

```text
com.indigo.synapse.cloud.autoconfigure.SynapseCloudAutoConfiguration
com.indigo.synapse.cloud.autoconfigure.SynapseFeignAutoConfiguration
```

AutoConfiguration imports：

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

默认装配：

- `OperationContextProvider`。
- `OperationContextHttpHeaderCodec`。
- `RemoteErrorBodyParser`。
- `InternalCallSigner`。
- Feign `RequestInterceptor`。
- Feign `ErrorDecoder`。

条件：

- 缺少 Feign 类时不装配 Feign 相关 Bean。
- 用户自定义同类型 Bean 时默认 Bean 不覆盖。
- `synapse.cloud.enabled=false` 时关闭 cloud 自动配置。
- `synapse.cloud.feign.enabled=false` 时关闭 Feign 自动配置。
- `synapse.cloud.feign.context-propagation-enabled=false` 时不装配 RequestInterceptor。
- `synapse.cloud.feign.error-decoder-enabled=false` 时不装配 ErrorDecoder。

## 8. 配置项

```yaml
synapse:
  cloud:
    enabled: true
    feign:
      enabled: true
      context-propagation-enabled: true
      error-decoder-enabled: true
      override-existing-headers: false
      internal-signature-enabled: false
```

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `synapse.cloud.enabled` | `true` | 是否启用 cloud 基础自动配置 |
| `synapse.cloud.feign.enabled` | `true` | 是否启用 Feign 自动配置 |
| `synapse.cloud.feign.context-propagation-enabled` | `true` | 是否启用 Feign 出站上下文传播 |
| `synapse.cloud.feign.error-decoder-enabled` | `true` | 是否启用 Feign 错误解码 |
| `synapse.cloud.feign.override-existing-headers` | `false` | 是否覆盖调用方已有 Header |
| `synapse.cloud.feign.internal-signature-enabled` | `false` | 是否调用内部调用签名扩展点 |

## 9. 扩展方式

### 9.1 替换 OperationContextProvider

消费方可以提供自己的 `OperationContextProvider`，例如从自定义上下文容器中读取当前上下文。

### 9.2 替换 InternalCallSigner

消费方可以提供 `InternalCallSigner` 写入 `X-Synapse-Timestamp`、`X-Synapse-Nonce`、`X-Synapse-Signature` 等签名 Header。

注意：这只是服务间调用签名扩展，不是 IAM。

### 9.3 替换 ErrorDecoder

消费方可以提供自定义 Feign `ErrorDecoder`，默认 `SynapseFeignErrorDecoder` 不会覆盖。

## 10. 边界与注意事项

- 不要把 `synapse-cloud` 做成 Gateway。
- 不要引入 `spring-cloud-starter-gateway`。
- 不要引入 Nacos、Seata、RocketMQ。
- 不要依赖 `synapse-webmvc` 或 `synapse-webflux` 复用 Result。
- 不要依赖 `synapse-security` 读取 roles / permissions。
- 不要依赖 `synapse-mq` 复用 MQ codec。
- 不要把 raw token、password、credential 或业务数据写入 Header。
- 无上下文时不要伪造 actor 或默认 system actor。

## 11. 常见问题

### Q1：为什么不依赖 WebMVC / WebFlux 的 Result？

`synapse-cloud` 需要被不同服务类型复用。依赖 WebMVC / WebFlux 会把 Cloud 和 Web 技术栈绑定，破坏模块边界。

### Q2：为什么不复用 MQ codec？

MQ codec 面向消息 Header，命名、语义和 HTTP 服务间调用不同。Cloud 先使用自己的 HTTP Header codec，后续稳定后再评估是否抽纯 Java codec 到 core。

### Q3：`synapse-cloud` 是否会自动认证服务间请求？

不会。当前只提供签名扩展点。完整可信边界、登录认证、IAM 和业务鉴权属于 Platform 或业务系统。

## 12. Configuration Metadata

`synapse-cloud` 发布 jar 必须包含 `META-INF/spring-configuration-metadata.json`，覆盖 `synapse.cloud.*` 和 `synapse.cloud.feign.*`。新增配置项时必须补充字段 Javadoc，并运行 metadata 测试。
