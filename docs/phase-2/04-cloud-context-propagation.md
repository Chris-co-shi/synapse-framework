# 04-Cloud Context Propagation

本文档用于冻结 TASK-203 的 `synapse-cloud` 方案、服务间调用 Header 契约和执行边界。TASK-203-A 已完成文档确认，TASK-203 后续实现必须继续遵守本文档。

## 1. 目标

TASK-203 的目标是新增 `synapse-cloud` 作为 Spring Cloud / OpenFeign 技术适配模块，支撑以下服务间调用链路：

```text
Http Request -> Service A -> Feign -> Service B
```

该链路需要传播以下技术上下文：

- `traceId`。
- `requestId`。
- `actor`。
- `initiator`。
- `source`。
- `tenantId`。
- `locale`。
- `timezone`。

服务间签名只作为扩展点规划，不做 IAM，不做登录认证，不做业务鉴权。

TASK-203 不做：

- Gateway。
- 注册中心。
- 配置中心。
- 服务治理后台。
- 可启动服务。
- 平台业务实现。

## 2. 模块边界

`synapse-cloud` 允许提供：

- Feign `RequestInterceptor`。
- Feign `ErrorDecoder`。
- 服务间调用 Header 规范。
- `OperationContext` 到 HTTP Header 的编码。
- HTTP Header 到 `OperationContext` 的恢复辅助。
- `traceId` / `requestId` 透传。
- internal-call marker。
- `InternalCallSigner` / `InternalCallVerifier` 扩展点。
- Cloud properties。
- 条件自动配置。

`synapse-cloud` 禁止提供：

- Gateway。
- Gateway `RouteLocator`。
- Gateway Filter 业务逻辑。
- 注册中心。
- 配置中心。
- 服务治理后台。
- IAM。
- 登录认证。
- 用户、角色、菜单。
- 业务权限判断。
- Nacos 配置管理。
- Seata 事务协调。
- RocketMQ adapter。
- 业务服务 SDK。

## 3. 依赖边界

目标依赖方向：

```text
synapse-core
  ↑
synapse-cloud
```

`synapse-cloud` 可以依赖：

- `synapse-core`。
- `spring-boot-autoconfigure`。
- `spring-cloud-openfeign-core` 或 `feign-core`。
- Jackson，仅用于 `ErrorDecoder` 解析远程错误响应。

`synapse-cloud` 禁止依赖：

- `synapse-webmvc`。
- `synapse-webflux`。
- `synapse-security`。
- `synapse-mq`。
- `spring-cloud-starter-gateway`。
- Nacos。
- Seata。
- RocketMQ。
- 业务模块。

特别说明：

- `synapse-cloud` 不通过依赖 WebMVC / WebFlux 复用 `Result`。
- `synapse-cloud` 不复用 MQ codec。
- Header codec 当前放在 `synapse-cloud`。
- 后续如果 MQ / Cloud / WebFlux 复用需求稳定，再单独评估是否抽纯 Java codec 到 `synapse-core`。
- `synapse-core` 不允许出现 HTTP、Feign、Spring、Servlet、Reactor 依赖。

## 4. Header 契约

第一版服务间 HTTP Header 契约如下。

基础追踪：

- `X-Trace-Id`
- `X-Request-Id`

Synapse 上下文：

- `X-Synapse-Context-Version`
- `X-Synapse-Tenant-Id`

Actor：

- `X-Synapse-Actor-Type`
- `X-Synapse-Actor-Id`
- `X-Synapse-Actor-Name`

Initiator：

- `X-Synapse-Initiator-Type`
- `X-Synapse-Initiator-Id`
- `X-Synapse-Initiator-Name`

Source：

- `X-Synapse-Source-Type`
- `X-Synapse-Source-Name`
- `X-Synapse-Source-Instance-Id`
- `X-Synapse-Source-Entrypoint`

Locale / Timezone：

- `X-Synapse-Locale`
- `X-Synapse-Time-Zone`

Internal Call：

- `X-Synapse-Internal-Call`
- `X-Synapse-Internal-Caller`
- `X-Synapse-Timestamp`
- `X-Synapse-Nonce`
- `X-Synapse-Signature`

### 4.1 TASK-203 第一轮必须支持

- `X-Trace-Id`
- `X-Request-Id`
- `X-Synapse-Tenant-Id`
- `X-Synapse-Actor-Type`
- `X-Synapse-Actor-Id`
- `X-Synapse-Actor-Name`
- `X-Synapse-Initiator-Type`
- `X-Synapse-Initiator-Id`
- `X-Synapse-Initiator-Name`
- `X-Synapse-Source-Type`
- `X-Synapse-Source-Name`

### 4.2 第一轮可选支持

- `X-Synapse-Locale`
- `X-Synapse-Time-Zone`
- `X-Synapse-Context-Version`

### 4.3 只规划，不在第一轮强制实现

- `X-Synapse-Internal-Call`
- `X-Synapse-Internal-Caller`
- `X-Synapse-Timestamp`
- `X-Synapse-Nonce`
- `X-Synapse-Signature`

### 4.4 禁止传播

服务间 Header 禁止传播：

- roles。
- permissions。
- menu codes。
- organization tree。
- raw token。
- password。
- credential。
- business data。

## 5. OperationContext HTTP 传播规则

### 5.1 出站传播

Feign `RequestInterceptor` 从当前 `OperationContext` 读取上下文并写入 Header。

有上下文时：

- 写入 `traceId`。
- 写入 `requestId`。
- 写入 `tenantId`。
- 写入 `actor`。
- 写入 `initiator`。
- 写入 `source`。
- 可选写入 `locale` / `timezone`。

无上下文时：

- 不伪造 actor。
- 不默认创建 system actor。
- 不写入无法证明来源的身份字段。
- 是否生成 `traceId` / `requestId` 留给具体实现根据已有 trace 组件判断。

Header 已存在时：

- 默认不覆盖。
- 后续通过配置允许覆盖。

### 5.2 入站恢复

TASK-203 不负责 WebMVC / WebFlux 入站恢复的重构。

入站恢复归属：

- WebMVC 入站恢复由 `synapse-webmvc` 后续任务处理。
- WebFlux 入站恢复由 `synapse-webflux` 处理。
- Gateway 注入 trusted header 属于 Synapse Platform。
- trusted header 的信任判定由 Gateway / Security / Platform 共同约束，不由 `synapse-cloud` 完成。

### 5.3 system actor 规则

- 缺少 actor 时不能悄悄伪装成 system。
- system actor 必须显式指定。
- 异步 / MQ / Job 场景的 system actor 策略进入 TASK-204。

## 6. Feign 能力规划

### 6.1 RequestInterceptor

规划类名：

- `SynapseFeignRequestInterceptor`
- `SynapseFeignProperties`
- `SynapseCloudProperties`

职责：

- 读取 `OperationContextProvider` 或 `OperationContextHolder`。
- 编码 HTTP Header。
- 透传 `traceId` / `requestId`。
- 写入 `actor` / `initiator` / `source`。
- 可选调用 `InternalCallSigner`。
- 不依赖 WebMVC / WebFlux / Security / MQ。

### 6.2 ErrorDecoder

规划类名：

- `SynapseFeignErrorDecoder`
- `RemoteCallException`
- `RemoteErrorResponse`

职责：

- 解析远程错误响应。
- 保留 remote HTTP status。
- 保留 remote code。
- 保留 remote message。
- 保留 remote traceId。
- 保留 response body 摘要。
- 非标准 JSON / 空 body 时降级为通用远程调用异常。

约束：

- `RemoteCallException` 放在 `synapse-cloud`。
- 不放入 `synapse-core`。
- 不依赖 `synapse-webmvc.Result`。
- 不依赖 `synapse-webflux.Result`。
- 可用 Jackson tree 方式解析 `code` / `message` / `traceId`。

## 7. 签名扩展点规划

只规划，不实现完整认证体系。

规划接口：

- `InternalCallSigner`。
- `InternalCallVerifier`。

说明：

- Signer 用于出站请求追加 `timestamp` / `nonce` / `signature`。
- Verifier 只作为未来入站校验扩展点。
- 默认实现可以是 no-op。
- 不在 TASK-203-A 实现。
- 不做 IAM。
- 不做登录认证。
- 不做 Gateway 鉴权。

## 8. 自动配置规划

规划类名：

- `SynapseCloudAutoConfiguration`。
- `SynapseFeignAutoConfiguration`。
- `SynapseCloudProperties`。
- `SynapseFeignProperties`。

条件：

- `@ConditionalOnClass(feign.RequestInterceptor.class)`。
- `@ConditionalOnProperty(prefix = "synapse.cloud", name = "enabled", matchIfMissing = true)`。
- `@ConditionalOnProperty(prefix = "synapse.cloud.feign", name = "enabled", matchIfMissing = true)`。
- `@ConditionalOnMissingBean`。

配置项规划：

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

## 9. 子任务拆分

### TASK-203-A：Cloud 方案确认与 Header 契约冻结

目标：

- 通过文档冻结 `synapse-cloud` 模块边界。
- 冻结服务间 HTTP Header 契约。
- 明确 Feign、签名和后续任务边界。

修改范围：

- `docs/phase-2/04-cloud-context-propagation.md`。
- phase-2 roadmap / boundary checklist / module boundary。
- `AGENTS.md` 入口说明。

不做内容：

- 不新增 `synapse-cloud` module。
- 不修改 POM。
- 不新增 Java 类。

验收标准：

- 文档明确 `synapse-cloud` 只做技术适配。
- 文档明确 Header 契约和禁止传播字段。
- 文档明确 TASK-203-B 才能新增 module 和修改 POM。

### TASK-203-B：新增 synapse-cloud module 骨架与 POM

目标：

- 新增 `synapse-cloud` module 骨架。
- root POM 和 BOM 增加 `synapse-cloud` 声明。

修改范围：

- root `pom.xml`。
- `synapse-bom/pom.xml`。
- `synapse-cloud/pom.xml`。
- 最小 package marker / 自动配置资源，按执行计划确认。

不做内容：

- 不实现 Gateway。
- 不实现注册中心、配置中心或 IAM。
- 不实现 Feign 完整能力。

验收标准：

- reactor 包含 `synapse-cloud`。
- BOM 管理 `synapse-cloud`。
- `mvn validate` 通过。

### TASK-203-C：OperationContext HTTP Header Codec

目标：

- 实现 `OperationContext` 与 HTTP Header 的编解码。

修改范围：

- `synapse-cloud`。
- 必要时仅允许在 `synapse-core` 增加纯 Java 常量或 codec 抽象。

不做内容：

- 不让 `synapse-core` 依赖 HTTP、Feign、Spring、Servlet、Reactor。
- 不复用 MQ codec 作为 HTTP codec。

验收标准：

- Header 编码覆盖 actor、initiator、source、traceId、requestId、tenantId。
- null / blank 字段不写入。
- 缺少 actor 时不伪造 system。

### TASK-203-D：Feign RequestInterceptor 最小闭环

目标：

- 实现出站 Feign 上下文传播。

修改范围：

- `synapse-cloud`。

不做内容：

- 不实现 ErrorDecoder。
- 不实现完整签名验证。
- 不依赖 WebMVC / WebFlux / Security / MQ。

验收标准：

- 有 `OperationContext` 时写入约定 Header。
- 无 `OperationContext` 时不写入身份字段。
- 已存在 Header 默认不覆盖。

### TASK-203-E：Feign ErrorDecoder 最小闭环

目标：

- 实现远程错误响应解析和降级。

修改范围：

- `synapse-cloud`。

不做内容：

- 不依赖 `synapse-webmvc.Result`。
- 不依赖 `synapse-webflux.Result`。
- 不绑定业务错误码。

验收标准：

- 标准错误 JSON 可解析。
- 非标准 JSON / 空 body 降级为通用远程调用异常。
- 保留 remote HTTP status、code、message、traceId。

### TASK-203-F：自动配置与测试

目标：

- 完成 Feign 能力的条件装配和测试闭环。

修改范围：

- `synapse-cloud` 生产代码和测试代码。
- AutoConfiguration imports。

不做内容：

- 不新增启动服务。
- 不新增 Controller。

验收标准：

- 有 Feign 类时装配。
- 缺少 Feign 类时不误装配。
- 用户自定义 Bean 不覆盖。
- 配置关闭时不装配。

### TASK-203-G：文档与 Skill 收口

目标：

- 将 `synapse-cloud` 已实现事实写入模块手册和 Skill。

修改范围：

- README。
- `docs/modules/README.md`。
- `docs/modules/synapse-cloud.md`。
- `skills/synapse-cloud/SKILL.md`。

不做内容：

- 不把未实现能力写成当前事实。
- 不把 `synapse-cloud` 描述成 Gateway。

验收标准：

- 文档区分当前事实与后续规划。
- Skill 只沉淀已测试通过的最佳实践。

## 10. 边界检查命令

```bash
rg -n "@SpringBootApplication|SpringApplication\.run" .
rg -n "@RestController\b|@Controller\b|@RequestMapping\b|@GetMapping\b|@PostMapping\b" '*/src/main/java'
rg -n "spring-cloud-starter-gateway|nacos|seata|rocketmq" .
rg -n "synapse-webmvc|synapse-webflux" synapse-cloud || true
rg -n "X-Synapse-|X-Trace-Id|X-Request-Id" docs/phase-2 docs/modules README.md AGENTS.md
git diff --check
```

说明：

- TASK-203-A 不存在 `synapse-cloud` 目录，第四条命令在本任务中可以合理返回无目标路径。
- 文档中出现 Gateway、IAM、Nacos、Seata、RocketMQ 等词通常是边界说明，不代表实现。
- 后续 TASK-203-B 之后，必须确认 `synapse-cloud` 不依赖 WebMVC / WebFlux / Security / MQ。
