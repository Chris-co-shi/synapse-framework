# synapse-cloud Skill

## 模块定位

`synapse-cloud` 是 Synapse Framework 的 Spring Cloud / OpenFeign 服务间调用技术支撑模块。

允许提供：

- 服务间调用 Header 契约。
- `OperationContext` 到 HTTP Header 的编码。
- Feign `RequestInterceptor`。
- Feign `ErrorDecoder`。
- 内部调用签名扩展点。
- Spring Boot 条件自动配置。

禁止提供：

- Gateway。
- Gateway RouteLocator。
- Gateway Filter 业务逻辑。
- 注册中心。
- 配置中心。
- 服务治理后台。
- IAM。
- 登录认证。
- 业务鉴权。
- 用户、角色、菜单、组织等业务模型。
- Nacos / Seata / RocketMQ adapter。
- 业务服务 SDK。

## 开发前必读

- `AGENTS.md`
- `docs/phase-2/00-framework-boundary.md`
- `docs/phase-2/03-boundary-checklist.md`
- `docs/phase-2/04-cloud-context-propagation.md`
- `docs/modules/synapse-cloud.md`

## 推荐包结构

```text
com.indigo.synapse.cloud
com.indigo.synapse.cloud.autoconfigure
com.indigo.synapse.cloud.context
com.indigo.synapse.cloud.feign
com.indigo.synapse.cloud.remote
com.indigo.synapse.cloud.security
```

## 标准实现模式

- `synapse-cloud` 只能依赖 `synapse-core`、Spring Boot AutoConfigure、Spring Cloud OpenFeign Core 和 Jackson。
- 不依赖 `synapse-webmvc`、`synapse-webflux`、`synapse-security`、`synapse-mq`。
- Header codec 只传播技术上下文。
- 无 `OperationContext` 时不伪造 actor，不默认创建 system actor。
- 已有 Header 默认不覆盖，必须通过配置显式允许覆盖。
- Feign `RequestInterceptor` 从 `OperationContextProvider` 读取当前上下文。
- Feign `ErrorDecoder` 不依赖 Web Result，只解析 `code`、`message`、`traceId`。
- 自动配置必须使用 `@ConditionalOnClass`、`@ConditionalOnProperty`、`@ConditionalOnMissingBean`。
- 缺少 Feign 类时不得误装配 Feign Bean。

## Header 禁止项

服务间 Header 禁止传播：

- roles。
- permissions。
- menu codes。
- organization tree。
- raw token。
- password。
- credential。
- business data。

## 测试要求

- Header codec 正常编码测试。
- null / blank Header 跳过测试。
- 缺少 actor 不伪造 actor 测试。
- Header 默认不覆盖和允许覆盖测试。
- Feign RequestInterceptor 有上下文 / 无上下文测试。
- InternalCallSigner no-op 和自定义 signer 测试。
- Feign ErrorDecoder 标准 JSON、非标准 JSON、空 body 测试。
- 自动配置启用 / 关闭 / 用户 Bean 不覆盖 / 缺少 Feign 类不装配测试。
- Configuration Metadata 测试，覆盖 `synapse.cloud.*` 和 `synapse.cloud.feign.*`。
- 依赖边界检查：`synapse-cloud` 不得命中 `synapse-webmvc`、`synapse-webflux`、Gateway、Nacos、Seata、RocketMQ 依赖。

## 常见错误

- 为复用 Result 引入 `synapse-webmvc` 或 `synapse-webflux`。
- 复用 `synapse-mq` 的 message header codec。
- 在无上下文时默认创建 system actor。
- 把服务间签名实现成 IAM 或业务鉴权。
- 把 Gateway 路由、注册中心、配置中心能力放入 `synapse-cloud`。
