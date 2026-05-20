# API 设计规则

## 1. 总原则

- REST 风格优先。
- URL 表达资源，HTTP Method 表达动作。
- 请求和响应结构统一。
- 错误码稳定。
- 参数校验前置。
- 权限默认拒绝。

## 2. URL 规范

统一前缀：

```text
/api/admin
```

示例：

```text
GET    /api/admin/users
POST   /api/admin/users
GET    /api/admin/users/{id}
PUT    /api/admin/users/{id}
DELETE /api/admin/users/{id}
```

权限分配：

```text
PUT /api/admin/users/{id}/roles
PUT /api/admin/roles/{id}/menus
```

## 3. HTTP Method 规则

| Method | 用途 |
|---|---|
| GET | 查询 |
| POST | 新增、复杂动作 |
| PUT | 全量或语义修改 |
| PATCH | 局部修改，v0.1 谨慎使用 |
| DELETE | 删除 |

## 4. 统一响应

```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": {},
  "traceId": "...",
  "timestamp": "2026-05-20T12:00:00Z"
}
```

分页响应：

```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": {
    "records": [],
    "pageNo": 1,
    "pageSize": 20,
    "total": 100
  },
  "traceId": "...",
  "timestamp": "..."
}
```

## 5. 错误码规则

错误码分层：

```text
COMMON_XXXX
AUTH_XXXX
PERMISSION_XXXX
USER_XXXX
ROLE_XXXX
MENU_XXXX
DATA_XXXX
SYSTEM_XXXX
```

示例：

```text
COMMON_BAD_REQUEST
AUTH_INVALID_TOKEN
AUTH_REFRESH_TOKEN_INVALID
PERMISSION_DENIED
USER_NOT_FOUND
USER_USERNAME_EXISTS
DATA_CONFLICT
```

## 6. 参数校验

Controller request DTO 必须使用 Bean Validation：

- `@NotNull`
- `@NotBlank`
- `@Size`
- `@Pattern`
- `@Email`

复杂校验放 Application Service。

## 7. 分页规则

分页参数：

```text
pageNo 默认 1
pageSize 默认 20，最大 200
```

排序字段必须白名单。

禁止前端直接传数据库字段名。

## 8. 幂等规则

新增/修改类关键接口预留幂等机制：

- 请求头 `Idempotency-Key`
- Redis 幂等记录
- 唯一业务键

v0.1 可先提供注解和接口，后续实现。

## 9. 权限命名规则

权限标识：

```text
system:user:list
system:user:create
system:user:update
system:user:delete
system:role:assign-menu
```

菜单权限和 API 权限要能关联，但不要完全混为一体。

## 10. OpenAPI 规则

每个 Controller 必须补充：

- API 分组
- 接口说明
- 请求字段说明
- 响应字段说明
- 错误码说明

## 11. 日志与 Trace

每个请求必须有 traceId。

错误响应必须返回 traceId，便于排查。

## 12. 安全响应规则

禁止返回：

- password
- passwordHash
- salt
- access token 明文日志
- refresh token 明文日志
- secret key
- 内部异常栈给前端
