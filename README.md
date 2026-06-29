# Synapse Framework

面向 Java 21 / Spring Boot 企业应用的通用技术基座。

Framework 只提供可复用的技术模块、抽象、SPI、自动配置和上下文传播，不提供可启动平台服务、业务系统、管理后台或第三方厂商适配平台。

## Security V1

- 标准 OAuth2 / JWT Resource Server；
- WebMVC 与 WebFlux 技术适配；
- RS256、`typ=at+jwt`、Issuer、Audience 和时间 Claim 验证；
- USER / CLIENT 主体；
- Scope Authority；
- PermissionChecker SPI；
- OAuth2 Client Credentials；
- Gateway Authentication Only；
- GatewayProof 取消并进入代码清理；
- JWT 不默认携带或映射 roles、permissions、菜单、数据范围和租户。

Framework 是 Java/Spring 官方适配器，不是第三方接入 Synapse 的前置条件。

## 当前模块

```text
synapse-framework
├── synapse-bom
├── synapse-core
├── synapse-web
│   ├── synapse-web-core
│   ├── synapse-webmvc
│   └── synapse-webflux
├── synapse-time
├── synapse-config
├── synapse-i18n
├── synapse-data
├── synapse-mybatis-plus
├── synapse-datasource
├── synapse-cache
├── synapse-security
├── synapse-oauth2
│   ├── synapse-oauth2-core
│   ├── synapse-oauth2-authorization-server-support
│   ├── synapse-oauth2-client
│   └── synapse-oauth2-resource-server
│       ├── synapse-oauth2-resource-server-core
│       ├── synapse-oauth2-resource-server-webmvc
│       └── synapse-oauth2-resource-server-webflux
├── synapse-audit
├── synapse-messaging
├── synapse-observability
└── synapse-resilience
```

## Start Here

1. [V1 Security Baseline](docs/architecture/v1-security-baseline.md)
2. [Repository Gap Analysis](docs/architecture/repository-gap-analysis.md)
3. [Framework Boundary](docs/phase-2/00-framework-boundary.md)
4. [Module Index](docs/modules/README.md)
5. [Security Module](docs/modules/synapse-security.md)
6. [Resource Server Core](docs/modules/synapse-oauth2-resource-server-core.md)

## 边界

Framework 禁止包含：

- `@SpringBootApplication` 生产启动类；
- 业务 Controller、Service、Entity、Mapper、Repository；
- 用户、角色、菜单、组织等平台模型；
- Gateway、IAM、File、Message 等可启动服务；
- starter 聚合包；
- demo / example / sample application；
- SAP、MES、WMS 等厂商专用业务协议。

## 构建

```text
Java 21
Maven 3.8.6+
```

```bash
mvn clean test
mvn validate
```

现有 GatewayProof、JWT roles/permissions 映射和 tenant 主体字段属于待迁移代码事实，不代表目标架构。新增任务必须先读取 Gap Analysis。
