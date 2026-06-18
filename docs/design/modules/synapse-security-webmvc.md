# synapse-security-webmvc 设计说明

## 1. 模块使命

`synapse-security-webmvc` 是 trusted-header 协议到 Servlet Filter 生命周期的适配器。它把经过校验的 Header 恢复为 `AuthenticatedPrincipal`，并在请求作用域内建立/清理 Synapse SecurityContext。

## 2. 为什么单独拆模块

`synapse-security` 必须保持 Web 无关；Servlet API、Filter 顺序和响应桥接属于 MVC 适配。因此 trusted-header 的纯签名/解析逻辑留在 security，执行入口放在本模块。

## 3. 边界

负责：

- `TrustedHeaderAuthenticationFilter`。
- Filter 自动配置和注册顺序。
- 调用 security 中的 timestamp、signature、principal resolver。

[//]: # (- 使用 `SecurityContextBinder.bind` 管理请求生命周期。)

不负责：

- OAuth2 Resource Server。
- Spring Security FilterChain。
- Gateway Header 注入。
- 用户查询与登录。
- 网络可信边界本身。

## 4. 主链路

```text
Trusted caller request
  -> SynapseExceptionBridgeFilter from webmvc
  -> TrustedHeaderAuthenticationFilter
  -> read X-Synapse-* headers
  -> validate required fields
  -> validate timestamp
  -> optional HMAC signature verification
  -> resolve AuthenticatedUser / Client
  -> SecurityContextBinder.bind
  -> downstream filters / controller
  -> scope.close in all outcomes
```

## 5. Filter 顺序

- 异常桥接 Filter 必须更靠外，才能把 trusted-header 校验失败写成统一 Result。
- trusted-header Filter 应在业务 Controller 前完成身份建立。
- 与 OAuth2 Resource Server 同时启用时应默认拒绝启动或要求明确策略，避免双权威身份源。

## 6. 安全边界

HMAC Header 校验只能证明“持有共享密钥的一方生成了这些字段”，不能单独解决：

- 外网直接访问业务服务。
- 共享密钥泄露。
- nonce 重放存储。
- 服务身份和权限管理。
- TLS 与网络隔离。

因此部署仍需保证业务服务不接受绕过 Gateway 的不可信 Header。

## 7. 生命周期与失败边界

- 任何异常路径都必须关闭 scope。
- fail-fast 开启时，缺失/非法 Header 明确失败；关闭时是否匿名继续必须谨慎评估。
- 签名比较使用恒定时间策略，避免简单字符串比较。
- timestamp tolerance 使用明确 Clock，便于测试。
- Header 原文和 secret 不得写入日志。

## 8. 扩展原则

- Header 生成由 Platform Gateway / IAM adapter 实现。
- nonce 存储和重放防护由消费方扩展。
- 若改为 OAuth2，应引入 Resource Server 模块，不继续扩展 trusted-header 成自制 OAuth2。

## 9. 源码阅读顺序

```text
SecurityHeaders
  -> TrustedHeaderCanonicalizer
  -> TrustedHeaderTimestampValidator
  -> TrustedHeaderSignatureVerifier
  -> TrustedHeaderAuthenticatedUserResolver
  -> TrustedHeaderAuthenticationFilter
  -> WebMvc AutoConfiguration
  -> filter order and cleanup tests
```

## 10. 手写练习

1. 构造合法 Header 并计算 HMAC。
2. Filter 恢复用户后在 Controller 读取当前主体。
3. 修改 timestamp 验证过期失败。
4. 在下游抛异常，验证请求结束后 SecurityContext 为空。

## 11. 修改检查清单

- 是否把纯协议逻辑重复写进 Filter。
- 是否记录 secret、signature 或完整身份 Header。
- 是否忘记异常桥接 Filter 顺序。
- 是否在请求结束后遗留 ThreadLocal。
- 是否允许 OAuth2 和 trusted-header 无策略地同时成为身份权威。
- 是否错误宣称 HMAC 可替代网络隔离和 Bearer 验证。
