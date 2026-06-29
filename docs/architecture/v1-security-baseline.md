# Synapse Framework V1 Security Baseline

## 定位

Framework 是 Java/Spring 官方技术适配器，不是 IAM、平台服务或第三方接入前提。

它提供 OAuth2 Resource Server、OAuth2 Client、JWT/JWK 验证、USER/CLIENT 主体、上下文桥接、权限检查 SPI 和审计主体。

用户、Client、Role、Permission、Session 和 Token 签发属于 Platform IAM。

## JWT 基线

Resource Server 默认验证：

- RS256；
- `typ=at+jwt` 和 `kid`；
- Issuer、单一 Audience；
- `exp`、`nbf`、`iat`；
- `sub`、`jti`、`client_id`、`principal_type`；
- Scope；
- USER Token 的 `sid`。

当前单租户不要求 `tenant_id`。JWT 不包含 roles、permissions、菜单、数据范围或组织树。

## 主体与权限

- USER 与 CLIENT 必须区分；
- `OperationContext` 只传播稳定 Actor Reference；
- Scope 可以映射为 `SCOPE_*`；
- 不再默认从 JWT 映射 `ROLE_*` 或 `PERM_*`；
- 业务 Permission 由消费方提供的 `PermissionChecker` 或授权 Provider 处理。

## Gateway

Gateway 保留 `synapse-oauth2-resource-server-webflux`，使用 Authentication Only 模式。

Gateway 验证 JWT，但不加载业务权限，不执行 Role/Permission，不生成或验证 GatewayProof。

## GatewayProof

GatewayProof 已取消。现有协议、签名、验签、Filter、WebFilter、配置和测试属于待删除代码，不得用于新功能。

下游服务信任原始 JWT，不信任 Gateway 证明或身份 Header。

## 第三方和遗留系统

第三方可以使用任意技术栈的标准 OAuth2/JWT 实现，不需要 Framework。

不支持 OAuth2 的遗留系统通过项目级 Adapter 接入。Framework 不建设 SAP、MES、WMS 等厂商专用协议。

## 当前取舍

### NOW

- JWT Resource Server WebMVC/WebFlux；
- Claim、Issuer、Audience 和时间验证；
- USER/CLIENT Principal；
- Scope Authority；
- PermissionChecker SPI；
- OAuth2 Client Credentials；
- Gateway Authentication Only；
- 删除 GatewayProof。

### NEXT

- Introspection 适配；
- 可选数据授权 SPI；
- 更完整的 JWK 轮换测试。

### LATER

- Authorization Snapshot Client；
- Revocation Feed Client；
- DPoP、mTLS、FAPI 和多租户扩展。

### REJECTED

- GatewayProof；
- 身份 Header 恢复；
- JWT roles/permissions 默认映射；
- Framework 承载 IAM 业务；
- 强制第三方使用 Framework。
