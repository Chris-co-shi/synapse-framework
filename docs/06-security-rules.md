# 安全与权限规则

## 1. 总原则

- 默认拒绝。
- 最小权限。
- 认证和授权分离。
- 功能权限和数据权限分离。
- Token 可吊销。
- 登录、登出、刷新必须可审计。

## 2. 认证模型

v0.1 采用：

```text
Access Token + Refresh Token
```

Access Token：

- 短有效期
- 用于接口访问
- JWT 格式
- 可通过黑名单临时吊销

Refresh Token：

- 长有效期
- 数据库存储 hash
- 支持 rotation
- 支持复用检测
- 登出时吊销

## 3. 登录流程

```text
login-options
  -> captcha/session challenge
login
  -> 校验用户名密码
  -> 校验状态/锁定
  -> 生成 access token
  -> 生成 refresh token
  -> 写登录日志
  -> 返回用户基本信息和权限摘要
```

## 4. 刷新流程

```text
refresh
  -> 校验 refresh token hash
  -> 校验未过期、未吊销、未旋转
  -> 原子标记旧 token rotated
  -> 生成新 refresh token
  -> 生成新 access token
  -> 写审计事件
```

## 5. 登出流程

```text
logout
  -> 校验 refresh token 归属
  -> 吊销 refresh token
  -> access token 加入黑名单
  -> 写登出日志
```

## 6. RBAC 模型

```text
User
  -> UserRole
  -> Role
  -> RoleMenu
  -> Menu
  -> Permission
```

菜单类型：

```text
CATALOG
MENU
BUTTON
API
EXTERNAL_LINK
IFRAME
```

## 7. 权限标识

格式：

```text
domain:resource:action
```

示例：

```text
system:user:list
system:user:create
system:user:update
system:user:delete
system:role:assign-menu
```

## 8. 后端权限校验

推荐：

```java
@RequirePermission("system:user:create")
```

或与 Spring Security 方法级授权集成。

所有非公开接口默认需要认证。

## 9. 公开接口白名单

只允许：

```text
/api/admin/auth/login-options
/api/admin/auth/login
/api/admin/auth/refresh
/actuator/health 受环境限制
/openapi/** 仅开发环境
```

## 10. 数据权限

功能权限和数据权限分离。

数据范围：

```text
ALL
SELF
DEPT
DEPT_AND_CHILDREN
CUSTOM_DEPT
```

禁止 v0.1 开放任意 SQL 片段数据权限。

## 11. 密码规则

- 使用 BCrypt/Argon2 等强 hash。
- 禁止明文存储。
- 禁止日志打印密码。
- 管理员重置密码必须写审计日志。

## 12. 登录风控

v0.1 至少实现：

- 连续失败次数
- 临时锁定
- 锁定到期自动恢复
- 验证码预留
- 登录日志

## 13. Token 安全

- Refresh token 只存 hash。
- access token secret 从配置或密钥管理读取。
- 生产环境禁止默认密钥。
- refresh rotation 必须原子化。
- reuse 检测写审计事件。

## 14. 安全审计事件

必须记录：

- 登录成功
- 登录失败
- 登出
- 刷新 token
- refresh token reuse
- 密码修改
- 角色授权变更
- 菜单授权变更
- 用户禁用/启用

## 15. 安全禁止事项

- 禁止接口默认放行。
- 禁止前端控制权限作为唯一权限。
- 禁止信任前端传入 userId/tenantId 判定当前用户。
- 禁止 token 明文入库。
- 禁止 token 明文打印日志。
- 禁止把 `ROLE_ADMIN` 写死为超级权限唯一判断。
