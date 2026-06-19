# synapse-oauth2-client 使用手册

## 模块定位

`synapse-oauth2-client` 承载 OAuth2 出站 Token Relay、Client Credentials、Authorized
Client 管理和 Token 生命周期扩展。

## 当前事实

Phase 1 仅建立可编译 JAR 和包边界，尚未提供运行时实现或自动配置。

## 边界

- 不包装 `@FeignClient`，不替代 Spring Cloud OpenFeign。
- 不把出站 Client Token 写入当前主体上下文。
- 不实现登录、客户端管理后台或 IAM 服务。
