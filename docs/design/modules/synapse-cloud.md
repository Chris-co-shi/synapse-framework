# synapse-cloud 设计说明

## 1. 模块使命

`synapse-cloud` 负责服务间 HTTP 调用的技术上下文传播和远程错误解码。它连接 core 的 `OperationContext` 与 OpenFeign，但不承担 Gateway、注册中心、配置中心或业务服务 SDK 职责。

## 2. 边界

负责：

- Synapse 服务间 Header 契约。
- OperationContext 到 HTTP Header 的编解码适配。
- Feign 出站 `RequestInterceptor`。
- Feign `ErrorDecoder`。
- 内部调用签名 Port。
- 条件自动配置。

不负责：

- 登录、权限判断和 IAM。
- Gateway 路由、Filter 和启动服务。
- Nacos、Seata、RocketMQ。
- raw Bearer Token 的任意传播。
- 业务 API Client 和业务 DTO。

## 3. 依赖方向

```text
synapse-cloud
  -> synapse-core
  -> OpenFeign core

must not depend on:
  webmvc / webflux / security / mq
```

Cloud 通过 core 的 provider 和 carrier 读取上下文，因此不会被具体 Web 技术栈绑定。

## 4. 核心对象角色

### 4.1 `OperationContextHttpHeaderCodec`

将 core carrier 映射为 HTTP Header。只写非空字段，不伪造 actor，默认不覆盖调用方已有 Header。

### 4.2 `SynapseFeignRequestInterceptor`

出站调用入口：

```text
OperationContextProvider
  -> Header codec
  -> RequestTemplate
  -> optional InternalCallSigner
```

它不判断目标接口的业务权限。

### 4.3 `SynapseFeignErrorDecoder`

把远程 HTTP 失败转换为稳定的 `RemoteCallException`，保留 status、remote code、message、traceId 和有限 body 摘要，但不依赖 WebMVC/WebFlux 的 Result 类型。

### 4.4 `InternalCallSigner`

签名扩展点默认 Noop。Framework 不默认建立“有 Header 就可信”的伪安全体系，真实密钥、nonce 存储和验证由 Platform / 消费方实现。

## 5. 主链路

```text
Service A
  -> Feign client
  -> SynapseFeignRequestInterceptor
  -> encode OperationContext headers
  -> optional sign
  -> Service B HTTP entry
  -> webmvc / webflux decode context
```

失败链路：

```text
Remote non-2xx response
  -> SynapseFeignErrorDecoder
  -> parse standard fields if possible
  -> RemoteCallException
```

## 6. 传播约束

允许传播：

- traceId / requestId。
- actor / initiator 的最小标识。
- source。
- tenantId 承载位。
- locale / time zone 技术偏好。

禁止传播：

- roles、permissions、菜单、组织树。
- password、credential。
- 默认情况下的 raw token。
- 大段业务对象。

上下文传播不是跨服务授权。下游服务仍需要自己的安全边界。

## 7. 生命周期与失败边界

- 无 OperationContext 时不写身份 Header。
- 已有 Header 默认不覆盖，避免破坏显式调用方设置。
- 错误 body 只保留有限摘要，防止内存和敏感信息风险。
- 非标准 JSON 应降级为通用远程错误，不能因解析失败掩盖原 status。
- 签名失败语义由自定义 signer/verifier 明确处理。

## 8. 扩展原则

- 自定义上下文来源：替换 `OperationContextProvider`。
- 自定义签名：实现 `InternalCallSigner` / `InternalCallVerifier`。
- 自定义错误协议：替换 `ErrorDecoder` 或 body parser。
- 不在 Cloud 模块中增加某个业务服务的 Feign Client。

## 9. 源码阅读顺序

```text
SynapseCloudHeaders
  -> OperationContextHttpHeaderCodec
  -> SynapseFeignProperties
  -> SynapseFeignRequestInterceptor
  -> RemoteErrorResponse / RemoteCallException
  -> RemoteErrorBodyParser
  -> SynapseFeignErrorDecoder
  -> InternalCallSigner
  -> AutoConfiguration
```

## 10. 手写练习

1. 建立一个 USER OperationContext。
2. 手写 RequestInterceptor 写入 trace、actor 和 source Header。
3. 验证 permissions 与 token 不会被写入。
4. 模拟远程 400 JSON 和非 JSON 500，分别解码成稳定异常。

## 11. 修改检查清单

- 是否把 Gateway 或业务 SDK 放入模块。
- 是否传播了敏感信息或完整权限快照。
- 是否在无上下文时伪造 system actor。
- 是否强制覆盖调用方已有 Header。
- 是否依赖 WebMVC/WebFlux Result。
- 新配置项是否有 metadata 和退让条件。
