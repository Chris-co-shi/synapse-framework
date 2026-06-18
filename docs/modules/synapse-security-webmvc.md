# synapse-security-webmvc 使用手册

## 1. 模块定位

`synapse-security-webmvc` 是 trusted-header 的 Servlet MVC 适配模块。

它只提供：

- `TrustedHeaderAuthenticationFilter`
- trusted-header Filter 自动配置
- Servlet FilterRegistrationBean

它依赖 `synapse-security` 和 `synapse-webmvc`，但不创建 Spring Security `SecurityFilterChain`。

## 2. 不适用场景

- OAuth2 Resource Server。
- IAM、登录认证、用户角色菜单管理。
- Gateway。
- 业务 Controller。

## 3. Maven 引入

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-security-webmvc</artifactId>
</dependency>
```

## 4. 配置

```yaml
synapse:
  security:
    trusted-header:
      enabled: true
      signature-enabled: true
      secret: your-shared-secret
      timestamp-tolerance: 300s
      fail-fast: true
```

Filter 会在内部建立安全上下文作用域，并在请求正常结束或发生异常时，
恢复进入 Filter 前的 SecurityContext 和 OperationContext。

## 5. 边界

- trusted-header 只适用于可信内网入口。
- 与 OAuth2 Resource Server 同时启用时，默认应 fail-fast，避免两个权威身份源。
- trusted-header 不能替代 Bearer Token 验证。
