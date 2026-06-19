# 05-GatewayProof 可信入口证明

本文档固化 GatewayProof 在 Framework 中的边界、协议和接入方式。

## 1. 目标

GatewayProof 用于证明入站请求经过可信 Platform Gateway。

固定安全模型：

```text
Platform Gateway
  -> 删除外部请求中所有 X-Synapse-Gateway-Proof-* Header
  -> 使用共享 secret 生成 GatewayProof
  -> 继续转发原始 Authorization: Bearer <token>
  -> Resource Server 先校验 GatewayProof
  -> Resource Server 再独立校验 JWT
```

GatewayProof 不是认证主体来源，不替代 JWT，不携带用户、角色或权限快照。下游服务必须同时满足：

1. GatewayProof 有效。
2. Bearer Token 有效。

## 2. 模块职责

| 模块 | 职责 |
| --- | --- |
| `synapse-security` | 提供 Web 无关 GatewayProof 协议模型、canonical string、HMAC-SHA256 signer/verifier、token hash、nonce replay store SPI 和配置属性 |
| `synapse-oauth2-resource-server-webmvc` | 在 Servlet Resource Server 链路中把 GatewayProof Filter 插入 `BearerTokenAuthenticationFilter` 之前 |
| `synapse-oauth2-resource-server-webflux` | 在 Reactive Resource Server 链路中把 GatewayProof WebFilter 插入 OAuth2 Authentication 之前 |
| Platform Gateway | 负责清理外部伪造 Header、生成签名、配置 secret、实现真实 nonce replay store |

Framework 不实现 Gateway 服务、RouteLocator、GlobalFilter、网关鉴权业务或平台路由后台。

## 3. Header 协议

GatewayProof v1 固定使用以下 Header：

```text
X-Synapse-Gateway-Proof-Version
X-Synapse-Gateway-Id
X-Synapse-Gateway-Timestamp
X-Synapse-Gateway-Nonce
X-Synapse-Gateway-Signature
```

`Version` 当前只支持 `v1`。`Timestamp` 使用 UTC epoch milliseconds。`Signature` 使用 HMAC-SHA256 后的 Base64 URL Safe 无 padding 编码。

## 4. Canonical String

v1 canonical string 固定为 8 行，以 `\n` 分隔：

```text
version
gatewayId
timestamp
nonce
HTTP_METHOD
normalized_path
normalized_query
bearer_token_sha256
```

规则：

- `HTTP_METHOD` 转为大写。
- 空 path 规范化为 `/`。
- query 解析为 name/value，按 name 排序，同名按 value 排序。
- query 使用 RFC 3986 UTF-8 percent encoding。
- 无 value 的 query 参数规范化为 `name=`。
- 空 query 规范化为空字符串。
- `bearer_token_sha256` 是 Bearer Token 本身的 SHA-256 小写十六进制；缺少 token 时为空字符串。

## 5. 重放保护

重放保护端口定义在 `synapse-security`：

```java
boolean markIfAbsent(String gatewayId, String nonce, Duration ttl);
```

语义：

- 第一次写入返回 `true`。
- 重复写入返回 `false`。
- 只有签名验证成功后才写入 replay store。
- 启用重放保护时必须提供真实 store；不能用 noop 默认实现。
- `fail-fast=true` 且缺少 store 时启动失败。
- `fail-fast=false` 且缺少 store 时请求返回 GatewayProof 配置错误。

## 6. 配置

配置前缀：

```yaml
synapse.security.gateway-proof
```

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `enabled` | `false` | 是否启用 GatewayProof 入站校验 |
| `required` | `true` | 启用后是否要求非 permit path 请求必须携带有效 proof |
| `gateway-id` | `synapse-gateway` | 当前服务信任的 Gateway 标识 |
| `secret` | `""` | HMAC secret，必须通过安全配置注入，至少 32 bytes |
| `timestamp-skew` | `60s` | 时间戳允许偏移 |
| `replay-protection-enabled` | `false` | 是否启用 nonce 重放保护 |
| `fail-fast` | `true` | 配置非法时是否启动失败 |
| `permit-paths` | `/actuator/health`, `/error` | 跳过 GatewayProof 的技术路径 |

## 7. 错误码

GatewayProof 使用 `synapse-security` 的稳定错误码：

```text
SECURITY_GATEWAY_PROOF_MISSING
SECURITY_GATEWAY_PROOF_UNSUPPORTED_VERSION
SECURITY_GATEWAY_PROOF_UNKNOWN_GATEWAY
SECURITY_GATEWAY_PROOF_EXPIRED
SECURITY_GATEWAY_PROOF_INVALID
SECURITY_GATEWAY_PROOF_REPLAYED
SECURITY_GATEWAY_PROOF_CONFIGURATION_INVALID
```

WebMVC / WebFlux 适配模块统一写出 403 `Result`，不暴露 canonical string、secret、token 指纹或过多验签细节。

## 8. Platform Gateway 接入步骤

Platform Gateway 后续接入时应：

1. 在转发前删除外部请求携带的所有 `X-Synapse-Gateway-Proof-*` Header。
2. 使用与下游服务一致的 canonical string 规则生成签名。
3. 保留原始 `Authorization: Bearer <token>`，不得改写为身份 Header。
4. 为生产环境提供分布式 nonce replay store。
5. 通过 Secret Manager 或环境变量注入 GatewayProof secret。
6. 对 secret 轮换、gateway-id 多实例策略和时钟同步建立运维规范。

## 9. 边界

GatewayProof 只证明请求经过可信入口，不证明用户身份、不表达权限、不表示服务间调用可信。

它不得用于：

- 登录认证。
- 用户、角色、菜单、组织等业务模型传输。
- 替代 OAuth2 Resource Server 的 JWT 验证。
- 替代 `synapse-cloud` 的出站服务间调用签名扩展点。
- 建设 Gateway 可启动服务或网关业务鉴权。
