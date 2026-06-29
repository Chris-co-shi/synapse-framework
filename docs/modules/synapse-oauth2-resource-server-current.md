# OAuth2 Resource Server 当前目标

## 共享验证

WebMVC 与 WebFlux 必须共享以下语义：

- RS256；
- `typ=at+jwt`；
- Issuer；
- 单一 Audience；
- 时间 Claim 与 60 秒 clock skew；
- 必要 Claim；
- USER / CLIENT 主体。

## Authority

默认只根据标准 Scope 生成 `SCOPE_*`。

不再默认读取 JWT roles 和 permissions，也不生成 `ROLE_*` 或 `PERM_*`。

业务 Permission 通过 `PermissionChecker` 或消费方授权 Provider 获得。

## WebMVC

负责 Servlet Resource Server、JWT Converter、主体上下文桥接和统一 401/403。

删除 GatewayProof Verification Filter。

## WebFlux

负责 Reactive Resource Server、Reactor Context 主体桥接和统一 401/403。

删除 GatewayProof WebFilter。

Gateway 可以自定义 `SecurityWebFilterChain` 并复用 Framework Configurer，形成 Authentication Only 模式。

## 边界

Resource Server 不签发 Token，不保存私钥，不实现 IAM，不查询业务权限数据库，不承担 Gateway 路由。
