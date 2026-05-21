---
name: synapse-security
description: Synapse Security 基础能力最佳实践。Use when Codex implements or reviews synapse-security code involving Spring Security 6.5.x, OAuth2 Authorization Server, OAuth2 Resource Server, JWT, JWK, PasswordEncoder, SecurityContext, permission annotations, token denylist, or security tests.
---

# Synapse Security

## 必读

- `AGENTS.md`
- `docs/01-architecture.md`
- `docs/02-module-boundary.md`
- `docs/06-security-rules.md`
- `docs/07-test-rules.md`
- `docs/10-technical-foundation-baseline.md`
- `skills/synapse-cache/SKILL.md`
- `skills/synapse-security/SKILL.md`

## 职责和边界

- 提供 OAuth2 Authorization Server 基座。
- 提供 OAuth2 Resource Server 基座。
- 使用 JWT + JWK。
- 提供 PasswordEncoder、SecurityContext、LoginUser、权限注解扩展点。
- 提供 Spring Boot 自动配置。
- 提供最小 OAuth2 Authorization Server 基础 Bean。
- 提供 JWT 签发和验签服务。
- IAM/Auth/RBAC 是后续验证模块，不直接塞进 security 基座核心。

## 推荐包结构

```text
com.indigo.synapse.security
├── oauth2
├── jwt
├── jwk
├── context
├── permission
├── password
└── autoconfigure
```

## 标准实现模式

- 第一层 Security Foundation 必须先定义纯 Java 安全契约，再接入 Spring Security / OAuth2 Server 具体配置。
- Spring Boot 自动配置统一由 `SynapseSecurityAutoConfiguration` 暴露。
- 自动配置必须写入 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。
- 默认拒绝，公开端点必须白名单。
- `OAuth2PublicEndpointPolicy` 只默认放行 `/oauth2/**` 和 `/.well-known/**`。
- `/api/admin/auth/login-options`、`/api/admin/auth/login`、`/api/admin/auth/refresh` 属于 IAM/Auth 验证模块启用后的公开端点，不进入 security 基座默认白名单。
- JWT 由 Authorization Server 签发，Resource Server 通过 JWK 验签。
- JWT 签发和验签统一通过 `SynapseJwtService`。
- JWT token type claim 固定使用 `token_type`，值为 `JwtTokenType` 名称。
- 基座 `issuer` 允许普通字符串；读取时不要强行使用 `Jwt.getIssuer()` URL 转换。
- `JwtClaims` 必须包含 issuer、subject、tokenId、tokenType、issuedAt、expiresAt，且 expiresAt 必须晚于 issuedAt。
- access token 类型使用 `JwtTokenType.ACCESS_TOKEN`；refresh token 属于 IAM/Auth 验证模块，不作为 security 基座第一层模型。
- `JwkKeyDescriptor` 必须表达 keyId、algorithm、use、createdAt、expiresAt，JWK 轮换通过 keyId 和过期时间预留。
- 生产环境禁止默认密钥。
- `SecurityKeyPolicy` 必须在生产环境拒绝 `default`、`dev`、`test`、`synapse-default`、`synapse-dev` 等默认 key id。
- `SynapseRsaKeyFactory` 只提供 v0.1 开发/测试期 RSA JWK 生成能力；`production=true` 时自动配置禁止自动生成临时 `RSAKey`。
- `production=true` 时应用必须显式提供生产密钥材料对应的 `RSAKey`、`JWKSource`、`JwtEncoder/JwtDecoder` 等可覆盖 Bean，或后续接入配置/密钥管理自动配置。
- `SynapseSecurityProperties` 使用 `synapse.security` 前缀，至少包含 issuer、keyId、production、accessTokenTtl。
- `AuthorizationServerSettings` 默认来自 `synapse.security.issuer`。
- `RegisteredClientRepository` 在开发模式中只提供内存空实现，应用必须覆盖它接入 IAM 或配置化客户端；`production=true` 时禁止使用基座默认内存实现。
- `OAuth2AuthorizationService`、`OAuth2AuthorizationConsentService` 第一层使用内存实现，应用可覆盖为持久化实现。
- token denylist 通过 cache 模块扩展。
- token 吊销只定义 `TokenDenylistPort`，Redis 实现放在 cache/security adapter 或 starter 自动配置中；`production=true` 时禁止使用基座默认 `NoopTokenDenylistPort`。
- 权限注解只表达权限语义，不直接访问业务 Mapper。
- `SecurityContext` 使用 ThreadLocal 时，Filter / 拦截器 / 测试必须在请求结束后 clear。
- `LoginUser` 只保存当前用户身份摘要、角色和权限标识，不持有 Entity、Mapper 或数据库连接。

## 允许技术和禁止事项

允许：

- Spring Security 6.5.x。
- Spring Authorization Server。
- OAuth2 Resource Server。
- Spring Security OAuth2 JOSE。
- Nimbus JWK/JWT。
- BCrypt PasswordEncoder。
- Spring Boot AutoConfiguration。

禁止：

- 把 IAM 用户、角色、菜单、登录流程写入 `synapse-security` 基座。
- 在基座中创建默认业务客户端并用于生产。
- 在生产环境使用默认 key id 或临时生成 key 作为最终方案。
- 在生产环境依赖默认内存客户端仓库。
- 在生产环境依赖默认 Noop token denylist。
- 默认放行非 OAuth2/JWK discovery 端点。
- 打印 token、client secret、私钥。
- 在权限注解中直接访问 Mapper 或数据库。

## 测试要求

- 纯 Java 契约必须覆盖公开端点白名单、JWT claims 校验、JWK 描述、生产默认 key 拒绝、SecurityContext clear、权限注解运行时可见。
- 验证 token 签发、JWK 暴露、资源服务器验签。
- 验证 `SynapseJwtService` 可以签发并用 JWK 公钥验签。
- 验证自动配置能注册 RSAKey、JWKSource、JwtEncoder、JwtDecoder、SynapseJwtService、PasswordEncoder、AuthorizationServerSettings、RegisteredClientRepository、OAuth2AuthorizationService、OAuth2AuthorizationConsentService。
- 覆盖未认证 401、无权限 403、无效 token、过期 token。
- 验证生产环境默认密钥保护策略。
- 验证 `production=true` 时缺少应用提供的 `RSAKey`、`RegisteredClientRepository`、`TokenDenylistPort` 会启动失败。
- 验证 `production=true` 且应用提供生产 Bean 时可以启动。
- 模块完成后先运行 `/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn -pl synapse-security -am test`，关键变更再运行根目录 `clean test`。

模块完成标准：

- 生产代码完成。
- 模块级测试通过。
- 根目录 `/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn clean test` 通过。
- `skills/synapse-security/SKILL.md` 已更新为最佳实践。
- `SKILL.md` 不写过程日志。

## 常见错误

- 接口默认放行。
- 在日志中打印 token 或密钥。
- 把 IAM 用户表逻辑写进 security 基座。
- 把 IAM 登录、refresh token rotation、用户状态校验提前塞进 `synapse-security`。
- 忘记清理 ThreadLocal 安全上下文，导致测试或请求串用户。
- 将 `/openapi/**`、`/actuator/**` 默认加入公开白名单；这些必须由环境和 starter 明确配置。
- 生产环境仍使用默认 key id 或默认密钥材料。
- 只禁止默认 key id，却仍允许生产模式自动生成临时 RSAKey。
- 生产模式下未覆盖 RegisteredClientRepository 或 TokenDenylistPort。
- 忘记给配置属性类添加 `@ConfigurationProperties`。
- JWT 测试使用固定过去时间，导致真实 decoder 判定 token 过期。
- 使用 `Jwt.getIssuer()` 强制把普通 issuer 转 URL。
- 提供默认 RegisteredClient 后被误用于生产。

## 示例任务拆分

- 定义 OAuth2 公开端点策略。
- 定义 JWT claims 和 JWK key 描述。
- 定义 LoginUser、SecurityContext、权限注解。
- 定义 token denylist port。
- 配置 Authorization Server 最小能力和可覆盖的客户端仓库。
- 配置 Resource Server JWT 验签和默认拒绝规则。
- 实现 SynapseJwtService 签发/验签。
- 实现 Spring Boot 自动配置。
- 增加权限注解和方法级授权测试。
