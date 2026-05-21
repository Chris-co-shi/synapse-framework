# TASK-003-01 Web Foundation

## 1. 任务目标

为 Synapse Framework 建立最小 Web 基础能力。

本任务只实现后台管理框架后续接口开发所需的基础设施，包括：

- 统一响应结构
- 错误码基础模型
- 通用业务异常
- 全局异常处理
- 参数校验异常处理

本任务不实现任何 Auth、Token、数据库、用户、权限相关业务。

---

## 2. 背景说明

当前项目已完成 Maven 多模块骨架。

第三阶段将逐步实现后台 Auth 最小闭环，但在实现登录接口之前，需要先沉淀一套通用 Web 基础能力，避免后续 Controller 各自返回不同结构，也避免业务异常、参数异常、系统异常处理方式不统一。

本任务完成后，后续接口统一返回：

```json
{
  "code": "0",
  "message": "success",
  "data": {}
}
```

失败时统一返回：

```json
{
  "code": "COMMON_BAD_REQUEST",
  "message": "请求参数错误",
  "data": null
}
```

---

## 3. 本任务范围

### 3.1 Common 模块

需要实现：

- `ErrorCode`
- `CommonErrorCode`
- `BusinessException`

### 3.2 Web 模块

需要实现：

- `ApiResponse`
- `GlobalExceptionHandler`

### 3.3 测试

需要补充基础测试，验证：

- 成功响应
- 失败响应
- 业务异常
- 参数校验异常
- 未知异常兜底

---

## 4. 非目标

本任务明确不做：

- 不实现 Auth 登录
- 不实现 Token
- 不实现 Spring Security 配置
- 不新增数据库表
- 不新增 Entity
- 不新增 Mapper
- 不新增 Repository
- 不实现用户查询
- 不实现密码校验
- 不实现权限判断
- 不实现菜单、角色、权限、租户
- 不实现前端页面
- 不创建 `docs/run-logs` 目录
- 不编写独立 Run Log

---

## 5. 建议模块边界

### 5.1 `synapse-common`

职责：

- 放置跨模块可复用的基础异常和错误码抽象
- 不依赖 Web
- 不依赖 Spring MVC
- 不依赖 Admin
- 不依赖 Security
- 不依赖 Data

建议包结构：

```text
synapse-common
  src/main/java/.../common/error/
    ErrorCode.java
    CommonErrorCode.java

  src/main/java/.../common/exception/
    BusinessException.java
```

---

### 5.2 `synapse-web`

职责：

- 放置 Web 层通用能力
- 可以依赖 `synapse-common`
- 不依赖 Admin
- 不依赖 Security
- 不依赖 Data

建议包结构：

```text
synapse-web
  src/main/java/.../web/response/
    ApiResponse.java

  src/main/java/.../web/exception/
    GlobalExceptionHandler.java
```

---

## 6. 设计要求

### 6.1 ErrorCode

`ErrorCode` 是错误码基础接口。

建议包含：

```java
String code();

String message();

int httpStatus();
```

要求：

- 不绑定具体业务
- 不依赖 Spring MVC
- 可被 Common、Auth、RBAC 等后续模块复用

---

### 6.2 CommonErrorCode

`CommonErrorCode` 是通用错误码枚举。

至少包含：

| 枚举 | code | message | HTTP |
|---|---|---|---:|
| SUCCESS | 0 | success | 200 |
| COMMON_BAD_REQUEST | COMMON_BAD_REQUEST | 请求参数错误 | 400 |
| COMMON_UNAUTHORIZED | COMMON_UNAUTHORIZED | 未认证 | 401 |
| COMMON_FORBIDDEN | COMMON_FORBIDDEN | 无权限 | 403 |
| COMMON_NOT_FOUND | COMMON_NOT_FOUND | 资源不存在 | 404 |
| COMMON_CONFLICT | COMMON_CONFLICT | 数据冲突 | 409 |
| COMMON_INTERNAL_ERROR | COMMON_INTERNAL_ERROR | 系统内部错误 | 500 |

要求：

- `SUCCESS` 的 code 使用 `"0"`
- 业务失败不得使用 `"0"`
- 错误码字符串保持稳定，后续前端会依赖

---

### 6.3 BusinessException

`BusinessException` 用于表达可预期业务异常。

建议字段：

```java
private final ErrorCode errorCode;
```

建议构造方法：

```java
BusinessException(ErrorCode errorCode)

BusinessException(ErrorCode errorCode, String message)
```

要求：

- 必须保留 `ErrorCode`
- 可以覆盖默认 message
- 不直接绑定 HTTP Response
- 不打印敏感信息

---

### 6.4 ApiResponse

`ApiResponse<T>` 是统一响应对象。

建议字段：

```java
private String code;
private String message;
private T data;
```

建议静态方法：

```java
success()

success(T data)

fail(ErrorCode errorCode)

fail(ErrorCode errorCode, String message)
```

要求：

- 成功响应 code 必须为 `"0"`
- 失败响应 data 默认是 `null`
- 不把异常堆栈返回给前端
- 不在响应结构中加入过多字段

本阶段暂不加入：

- traceId
- timestamp
- path
- requestId

这些后续可以在 Observability 或 Web Enhancement Task 中扩展。

---

### 6.5 GlobalExceptionHandler

`GlobalExceptionHandler` 负责统一异常处理。

至少处理：

| 异常类型 | 返回错误码 |
|---|---|
| BusinessException | 使用异常中的 ErrorCode |
| MethodArgumentNotValidException | COMMON_BAD_REQUEST |
| BindException | COMMON_BAD_REQUEST |
| ConstraintViolationException | COMMON_BAD_REQUEST |
| Exception | COMMON_INTERNAL_ERROR |

要求：

- 业务异常按业务错误码返回
- 参数校验异常返回 400 语义
- 未知异常返回 500 语义
- 未知异常不能把堆栈返回给前端
- 可以记录日志，但不能输出敏感信息
- 不吞异常导致测试无法判断
- 不处理 Auth 专属异常

---

## 7. 模块依赖约束

必须遵守：

- `synapse-common` 不得依赖 `synapse-web`
- `synapse-common` 不得依赖 `synapse-admin`
- `synapse-common` 不得依赖 `synapse-security`
- `synapse-web` 可以依赖 `synapse-common`
- `synapse-web` 不得依赖 `synapse-admin`
- `synapse-web` 不得依赖 `synapse-security`
- `synapse-web` 不得依赖 `synapse-data`

不得为了实现本任务破坏现有 Maven 多模块结构。

---

## 8. 测试要求

至少补充以下测试。

### 8.1 ApiResponse 测试

覆盖：

- `success()` 返回 code = `"0"`
- `success(data)` 能正确返回 data
- `fail(errorCode)` 能正确返回错误码和 message
- `fail(errorCode, message)` 能覆盖默认 message
- 失败响应 data 为 `null`

---

### 8.2 BusinessException 测试

覆盖：

- 能正确持有 `ErrorCode`
- 默认 message 来自 `ErrorCode`
- 自定义 message 能覆盖默认 message

---

### 8.3 GlobalExceptionHandler 测试

覆盖：

- `BusinessException` 返回对应错误码
- 参数校验异常返回 `COMMON_BAD_REQUEST`
- 未知异常返回 `COMMON_INTERNAL_ERROR`

如果当前测试环境不方便直接测 `GlobalExceptionHandler`，至少需要通过单元测试验证核心方法，不要为了测试引入过重上下文。

---

## 9. 验收标准

必须通过：

```bash
mvn clean test
```

如果项目已有更轻量的模块级测试命令，也可以先执行模块级测试，但最终必须执行根目录：

```bash
mvn clean test
```

验收结果需要满足：

- 新增代码编译通过
- 现有测试不被破坏
- 新增测试通过
- 没有引入 Auth、Token、数据库、权限相关代码
- 没有创建 Run Log

---

## 10. Git 要求

建议分支：

```bash
feature/TASK-003-auth-minimal-loop
```

本任务提交信息建议：

```bash
git commit -m "TASK-003-01 add web foundation"
```

不允许使用以下提交信息：

```bash
git commit -m "update"
git commit -m "fix"
git commit -m "test"
git commit -m "修改"
```

---

## 11. Codex 执行 Prompt

可以将以下内容直接发给 Codex：

```text
你现在执行 Synapse Framework 的 TASK-003-01：Web Foundation。

请先读取：

- AGENTS.md
- README.md
- docs/tasks/TASK-003-01-web-foundation.md

本次只允许实现：

- ErrorCode
- CommonErrorCode
- BusinessException
- ApiResponse
- GlobalExceptionHandler
- 参数校验异常处理
- 对应基础测试

禁止实现：

- Auth 登录
- Token
- Spring Security 配置
- 数据库表
- Entity
- Mapper
- Repository
- 用户查询
- 密码校验
- 权限判断
- 菜单、角色、权限、租户
- 前端页面
- docs/run-logs 目录
- 独立 Run Log

开始修改前，先输出自查结果：

1. 本次涉及哪些模块？
2. 会新增哪些类？
3. 会修改哪些类？
4. 会修改哪些 pom？
5. 是否会引入新依赖？
6. 是否存在模块反向依赖风险？
7. 是否会影响已有测试？
8. 本次如何验证？

确认边界后再开始实现。

实现完成后必须执行：

mvn clean test

最后输出：

1. 修改文件列表
2. 新增文件列表
3. 删除文件列表，如果没有则写无
4. 测试结果
5. 遗留问题
6. 下一步建议

禁止创建 docs/run-logs 目录。
禁止编写独立 Run Log。
```

---

## 12. Completion Summary

任务完成后填写。

```text
Status:
Branch:
Commit:
Test:
Notes:
```