# synapse-security-webmvc Skill

## 职责

只提供 trusted-header Servlet MVC Filter 适配和自动配置。

## 禁止事项

- 不创建 Spring Security `SecurityFilterChain`。
- 不做 OAuth2 / IAM / 登录。
- 不新增 Controller、Entity、Mapper、Repository、migration。
- 不创建 starter、demo、example、sample application。

## 测试要求

- Filter 成功恢复 `SecurityContext` 和 `OperationContext`。
- 异常后 Scope 清理。
- 进入 Filter 前已有 OperationContext 时能恢复。
