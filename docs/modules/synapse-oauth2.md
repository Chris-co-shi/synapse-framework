# synapse-oauth2 使用手册

## 1. 模块定位

`synapse-oauth2` 是 Synapse Framework 的 OAuth2 / JWT / JWK 技术能力模块。

一阶段它只提供 JWT 签发校验、JWK/RSA 密钥基础 Bean 和 token denylist 端口，不提供完整认证平台。

当前核心能力：

- RSA JWK 生成与装配。
- `JwtEncoder` / `JwtDecoder` 自动配置。
- `SynapseJwtService` JWT 签发与校验。
- `JwtClaims` / `JwtTokenType` 载荷模型。
- `TokenDenylistPort` token 拒绝列表端口。
- 开发环境默认 RSA key。
- 生产环境保护策略。

## 2. 适用场景

业务系统或平台系统在以下场景可以引入 `synapse-oauth2`：

- 需要签发和校验 RS256 JWT。
- 需要统一 `JwtClaims` 模型。
- 需要基于 JWK/RSAKey 装配 `JwtEncoder` 和 `JwtDecoder`。
- 需要定义 token denylist 端口，供 Redis / DB / 其他存储实现接入。
- 需要在后续 IAM / Gateway / Auth Service 中复用 JWT 基础能力。

## 3. 不适用场景

`synapse-oauth2` 不适合承担以下职责：

- 用户登录。
- 用户注册。
- 密码校验流程。
- OAuth2 Authorization Server。
- OAuth2 Resource Server FilterChain。
- Spring Security Web 配置。
- 客户端管理后台。
- 授权码流程。
- refresh token 流程。
- token introspection 接口。
- 用户权限加载。
- RBAC / ABAC / DataScope。

这些能力属于后续 IAM / Auth 平台服务，不属于 framework 一阶段。

## 4. Maven 引入

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

再引入 oauth2 模块：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-oauth2</artifactId>
</dependency>
```

## 5. 核心能力

### 5.1 自动配置

核心类型：

```java
SynapseOAuth2AutoConfiguration
SynapseOAuth2Properties
```

自动注册：

```java
RSAKey
JWKSource<SecurityContext>
JwtEncoder
JwtDecoder
SynapseJwtService
TokenDenylistPort
```

说明：

- Bean 都支持 `@ConditionalOnMissingBean`。
- 消费方可以提供自己的 `RSAKey`、`JwtEncoder`、`JwtDecoder`、`SynapseJwtService` 或 `TokenDenylistPort`。
- 生产环境不会自动生成 RSA signing key。
- 生产环境不允许使用 `NoopTokenDenylistPort`。

### 5.2 JWT claims

核心类型：

```java
JwtClaims
JwtTokenType
```

字段：

```text
issuer
subject
audience
tokenId
tokenType
issuedAt
expiresAt
```

当前 token 类型：

```java
JwtTokenType.ACCESS_TOKEN
```

`JwtClaims` 不包含业务用户、角色、权限、菜单或组织结构。

### 5.3 JWT 服务

核心类型：

```java
SynapseJwtService
```

方法：

```java
String issue(JwtClaims claims);
JwtClaims verify(String token);
```

`issue` 负责签发 RS256 JWT。

`verify` 负责调用 `JwtDecoder` 完成基础校验，并还原 `JwtClaims`。

注意：

- `verify` 不检查 denylist。
- `verify` 不检查用户状态。
- `verify` 不加载权限。
- `verify` 不恢复 `SecurityContext`。

这些由调用方或平台服务负责。

### 5.4 RSA / JWK

核心类型：

```java
SynapseRsaKeyFactory
SecurityKeyPolicy
```

`SynapseRsaKeyFactory.generate(keyId)` 生成开发环境临时 RSAKey。

生产环境应由应用提供真实 `RSAKey` Bean，例如从：

- 配置文件。
- 证书库。
- KMS。
- 密钥管理服务。

### 5.5 Token denylist

核心类型：

```java
TokenDenylistPort
NoopTokenDenylistPort
```

端口方法：

```java
void deny(String tokenId, Instant expiresAt);
boolean isDenied(String tokenId);
```

说明：

- framework 只定义端口。
- 默认 Noop 只适合开发和测试。
- 生产环境必须提供真实实现。

## 6. 快速使用

### 6.1 开发环境签发 token

```java
JwtClaims claims = new JwtClaims(
        "synapse",
        "10001",
        Set.of("sample-service"),
        UUID.randomUUID().toString(),
        JwtTokenType.ACCESS_TOKEN,
        Instant.now(),
        Instant.now().plus(Duration.ofMinutes(15))
);

String token = synapseJwtService.issue(claims);
```

### 6.2 校验 token

```java
JwtClaims claims = synapseJwtService.verify(token);
```

如果需要检查主动失效：

```java
JwtClaims claims = synapseJwtService.verify(token);
if (tokenDenylistPort.isDenied(claims.tokenId())) {
    throw new SynapseAuthenticationException();
}
```

### 6.3 生产环境提供 TokenDenylistPort

```java
@Bean
TokenDenylistPort tokenDenylistPort() {
    return new RedisTokenDenylistPort(redisTemplate);
}
```

注意：Redis 实现不属于 `synapse-oauth2` 一阶段内容，可由平台服务或业务系统实现。

### 6.4 生产环境提供 RSAKey

```java
@Bean
RSAKey synapseRsaKey() {
    KeyPair keyPair = loadKeyPairFromSecureSource();
    return SynapseRsaKeyFactory.fromKeyPair("prod-key-2026-01", keyPair);
}
```

## 7. 扩展方式

### 7.1 替换 RSAKey

提供 `RSAKey` Bean 即可覆盖默认开发密钥。

### 7.2 替换 JwtEncoder / JwtDecoder

业务系统可以提供自己的：

```java
@Bean
JwtEncoder jwtEncoder() { ... }

@Bean
JwtDecoder jwtDecoder() { ... }
```

### 7.3 替换 TokenDenylistPort

生产环境必须替换：

```java
@Bean
TokenDenylistPort tokenDenylistPort() { ... }
```

## 8. 配置项

配置前缀：

```yaml
synapse.oauth2
```

配置项：

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `issuer` | `synapse` | token issuer |
| `key-id` | `synapse-dev` | JWK key id |
| `production` | `false` | 是否生产环境 |
| `access-token-ttl` | `15m` | access token 默认有效期 |

开发环境示例：

```yaml
synapse:
  oauth2:
    issuer: synapse
    key-id: synapse-dev
    production: false
    access-token-ttl: 15m
```

生产环境示例：

```yaml
synapse:
  oauth2:
    issuer: synapse-auth
    key-id: prod-key-2026-01
    production: true
    access-token-ttl: 15m
```

生产环境还必须提供：

- `RSAKey` Bean。
- `TokenDenylistPort` Bean。

## 9. 边界与注意事项

### 9.1 不要把 oauth2 模块当作认证中心

`synapse-oauth2` 只提供 JWT/JWK 基础能力，不处理登录、客户端授权、用户授权、刷新令牌等完整流程。

### 9.2 生产环境不能使用默认密钥

默认 RSA key 是运行时生成的临时密钥，只适合开发测试。生产环境重启后默认密钥变化会导致历史 token 无法验证，也存在密钥管理风险。

### 9.3 verify 不等于完整认证

`SynapseJwtService.verify(token)` 只表示 JWT 基础校验通过，不代表：

- token 没有被主动吊销。
- 用户仍然有效。
- 用户权限仍然有效。
- 当前请求已恢复为 SecurityContext。

这些需要上层服务完成。

### 9.4 denylist 端口不绑定 Redis

framework 只定义 `TokenDenylistPort`，不在 oauth2 模块中引入 Redis、DB 或缓存依赖。

## 10. 常见问题

### Q1：为什么 oauth2 依赖 synapse-security？

OAuth2 是 security 能力的扩展方向，可以依赖 security 的基础安全模型；但 oauth2 不应反向依赖 web、data、audit、mq 等模块。

### Q2：为什么没有 Spring Security Resource Server？

一阶段不做完整 Web 安全过滤链。Resource Server 属于后续平台认证或网关接入能力。

### Q3：为什么生产环境必须提供 TokenDenylistPort？

如果没有真实 denylist，退出登录、强制下线、token 主动吊销都无法生效。Noop 只能用于开发测试。

### Q4：JwtClaims 为什么不包含 roles / permissions？

一阶段 JWT 模型只承载基础 token claims。角色、权限快照是否放入 token，应由 IAM / Auth 平台服务根据安全策略决定。

### Q5：是否支持 refresh token？

一阶段不支持。refresh token 属于完整认证授权流程，后续应在 IAM / Auth 平台服务中设计。

## 11. Configuration Metadata

`synapse-oauth2` 发布 jar 必须包含 `META-INF/spring-configuration-metadata.json`，覆盖 `synapse.oauth2.*`。敏感配置说明不得包含真实密码、token 或 credential 示例。
