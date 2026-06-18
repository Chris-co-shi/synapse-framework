# synapse-oauth2-authorization-server-support 设计说明

## 1. 模块使命

该模块只提供 Authorization Server 签发 JWT 所需的密码学和 claims 技术支持，让 Synapse Platform IAM 可以复用统一的 RSA/JWK/JwtEncoder 与签发器，但不把完整 IAM 放入 Framework。

## 2. 边界

负责：

- RSA key 创建策略。
- `SigningKeyProvider` / `SigningKeySetProvider`。
- JWKSource、JwtEncoder 等签发基础 Bean。
- `JwtIssuanceClaims`。
- `SynapseJwtIssuer`。
- 开发密钥的显式条件自动配置。

不负责：

- 用户密码认证。
- `RegisteredClientRepository` 业务持久化。
- Authorization Code、Consent、OIDC 页面。
- Refresh Token rotation / revoke。
- IAM 用户、角色、菜单和管理 API。

## 3. 设计原则

### 3.1 私钥只属于签发侧

Resource Server 只需要公钥/JWK 验证，不应依赖本模块或接触私钥。

### 3.2 Framework 提供机制，Platform 决定密钥来源

生产环境可以从 HSM、KMS、Secret、数据库或文件系统加载 key。Framework 只定义 provider，不应在生产环境默认生成临时私钥。

### 3.3 开发密钥必须显式开启

运行时随机 key 会导致重启后旧 token 全部失效，因此只能用于本地开发。`production=true` 时应禁止开发 key，避免错误配置悄悄上线。

## 4. 核心对象角色

- `SigningKeyPolicy`：表达开发/生产密钥策略和算法约束。
- `SynapseRsaKeyFactory`：创建符合要求的 RSAKey，不负责持久化。
- `SigningKeyProvider`：提供当前签名 key。
- `SigningKeySetProvider`：提供验证和轮换需要的 key set。
- `JwtIssuanceClaims`：签发输入模型，区分技术 claims 与业务 IAM 数据加载。
- `SynapseJwtIssuer`：组装受控 claims 并调用 JwtEncoder。

## 5. 主链路

```text
Platform IAM authenticated subject
  -> build JwtIssuanceClaims
  -> SynapseJwtIssuer
  -> SigningKeyProvider / JwtEncoder
  -> signed JWT
  -> expose public JWK through Platform Authorization Server
```

本模块不负责前面的登录认证，也不负责后面的 token 存储和撤销业务。

## 6. 密钥轮换边界

- 新 token 使用 active signing key。
- 验证端在过渡期需要看到旧公钥。
- key id (`kid`) 必须稳定可区分。
- 私钥不得出现在日志、异常或公开 JWK。
- 完整轮换调度、密钥持久化和运维流程属于 Platform。

## 7. 失败边界

- 生产模式缺少 signing key：启动失败，而不是生成临时 key。
- claims 缺少必填 subject/client/type：签发前失败。
- 算法和 key 不匹配：配置阶段失败。
- JwtEncoder 失败：保留技术异常，不返回私钥或原始敏感 claims。

## 8. 扩展原则

- 生产 key：实现 provider Bean，默认配置退让。
- 多 key 轮换：实现 key set provider。
- IAM 自定义 claims：Platform 先构造受控 `JwtIssuanceClaims`；不要让 issuer 查询用户数据库。
- Refresh Token 与 Authorization 存储属于 IAM，不扩展本模块完成。

## 9. 源码阅读顺序

```text
SigningKeyPolicy
  -> SynapseRsaKeyFactory
  -> SigningKeyProvider / SigningKeySetProvider
  -> JwtIssuanceClaims
  -> SynapseJwtIssuer
  -> AuthorizationServerSupportAutoConfiguration
  -> development / production policy tests
```

## 10. 手写练习

1. 生成开发 RSA key 并签发包含 `kid` 的 JWT。
2. 只用公开 JWK 验证 token。
3. 重启并更换临时 key，观察旧 token 无法验证。
4. 在 production 配置下无 key 启动，验证明确失败。

## 11. 修改检查清单

- 是否把完整 IAM 或登录逻辑放入模块。
- 是否默认生成生产私钥。
- 是否让 Resource Server 依赖私钥模块。
- 是否把 key、token 或敏感 claims 写入日志。
- 是否忽略 key rotation 的旧公钥验证窗口。
- 用户自定义 provider 是否能覆盖默认实现。
