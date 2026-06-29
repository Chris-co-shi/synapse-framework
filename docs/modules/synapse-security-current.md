# synapse-security 当前目标

## 定位

`synapse-security` 是 Web 无关的安全主体与权限检查模块。

保留：

- `AuthenticatedPrincipal`；
- USER / CLIENT 主体；
- `CurrentPrincipalContext`；
- `PermissionChecker`；
- `@RequirePermission`；
- 安全主体到 `OperationContext` 的单向桥接；
- 密码编码技术能力。

删除：

- GatewayProof 协议、签名和验签；
- GatewayProof 配置；
- nonce replay store；
- 对 Gateway 可信入口的任何身份假设。

## 主体字段

USER 最小字段：

```text
subjectId
clientId
sessionId(optional)
```

CLIENT 最小字段：

```text
clientId
```

当前单租户不要求 tenant。roles 和 permissions 不从 JWT 写入主体。

## PermissionChecker

Framework 只定义检查入口和默认行为。Permission 数据来源由消费方提供，不在本模块查询 IAM 或业务数据库。

## 边界

本模块不负责 HTTP Token 解析、登录、Token 签发、用户管理、角色管理、业务数据权限或第三方厂商协议。

当前旧手册中的 GatewayProof 内容属于待删除代码事实，不再是推荐能力。
