# Skills 索引

本目录记录 Synapse Framework 已实现模块的 Agent 最佳实践。

## 当前 Skill

| 模块 | Skill |
| --- | --- |
| `synapse-bom` | [synapse-bom/SKILL.md](synapse-bom/SKILL.md) |
| `synapse-core` | [synapse-core/SKILL.md](synapse-core/SKILL.md) |
| `synapse-webmvc` | [synapse-webmvc/SKILL.md](synapse-webmvc/SKILL.md) |
| `synapse-webflux` | [synapse-webflux/SKILL.md](synapse-webflux/SKILL.md) |
| `synapse-time` | [synapse-time/SKILL.md](synapse-time/SKILL.md) |
| `synapse-config` | [synapse-config/SKILL.md](synapse-config/SKILL.md) |
| `synapse-i18n` | [synapse-i18n/SKILL.md](synapse-i18n/SKILL.md) |
| `synapse-data` | [synapse-data/SKILL.md](synapse-data/SKILL.md) |
| `synapse-mybatis-plus` | [synapse-mybatis-plus/SKILL.md](synapse-mybatis-plus/SKILL.md) |
| `synapse-datasource` | [synapse-datasource/SKILL.md](synapse-datasource/SKILL.md) |
| `synapse-cache` | [synapse-cache/SKILL.md](synapse-cache/SKILL.md) |
| `synapse-security` | [synapse-security/SKILL.md](synapse-security/SKILL.md) |
| `synapse-oauth2-core` | [synapse-oauth2-core/SKILL.md](synapse-oauth2-core/SKILL.md) |
| `synapse-oauth2-authorization-server-support` | [synapse-oauth2-authorization-server-support/SKILL.md](synapse-oauth2-authorization-server-support/SKILL.md) |
| `synapse-oauth2-client` | [synapse-oauth2-client/SKILL.md](synapse-oauth2-client/SKILL.md) |
| `synapse-oauth2-resource-server-core` | [synapse-oauth2-resource-server-core/SKILL.md](synapse-oauth2-resource-server-core/SKILL.md) |
| `synapse-oauth2-resource-server-webmvc` | [synapse-oauth2-resource-server-webmvc/SKILL.md](synapse-oauth2-resource-server-webmvc/SKILL.md) |
| `synapse-oauth2-resource-server-webflux` | [synapse-oauth2-resource-server-webflux/SKILL.md](synapse-oauth2-resource-server-webflux/SKILL.md) |
| `synapse-audit` | [synapse-audit/SKILL.md](synapse-audit/SKILL.md) |
| `synapse-messaging` | [synapse-messaging/SKILL.md](synapse-messaging/SKILL.md) |
| `synapse-observability` | [synapse-observability/SKILL.md](synapse-observability/SKILL.md) |
| `synapse-resilience` | [synapse-resilience/SKILL.md](synapse-resilience/SKILL.md) |

## 固定约定

- Skill 只写稳定最佳实践，不写过程日志。
- 新增模块并通过测试后必须新增对应 Skill。
- Framework 不创建 starter、demo、example、sample application。
- Skill 不得把 Platform 可启动服务描述成 Framework 能力。
- 身份认证只信任经过 Resource Server 验证的 Bearer Token。
- 公开 `@ConfigurationProperties` 必须生成 Spring Boot Configuration Metadata，并通过模块测试验证。
