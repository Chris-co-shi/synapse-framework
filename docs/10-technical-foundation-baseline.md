# Synapse Framework 技术基座基线

## 1. 目标

Synapse Framework v0.1 先交付通用 Java 技术基座，再通过 IAM/Auth/RBAC 验证基座能力。

第一阶段不以完整后台管理系统为目标。

## 2. 技术版本

| 能力 | 决策 |
|---|---|
| Java | Java 21 |
| Maven | 3.9.0，当前工作站使用 `/Users/sxc/Documents/tool/apache-maven-3.9.0` |
| Spring Boot | 3.5.14 |
| Spring Security | Boot 管理，6.5.x |
| OAuth2 | Authorization Server + Resource Server |
| Token | JWT + JWK |
| MyBatis-Plus | 3.5.9，完整使用官方能力 |
| 动态数据源 | dynamic-datasource Spring Boot 3 starter，配置级多数据源切换 |
| 数据库 | 不绑定具体厂商，通过方言适配层支持切换 |
| Redis | Spring Data Redis + Lettuce |
| 分布式锁 | Redis + Lua，可重入 |
| 限流 | Redis + Lua 滑动窗口 |
| 测试 | H2 + Testcontainers |
| OpenAPI | springdoc 2.8.x |
| 工具 | Lombok + MapStruct |

## 3. 第一阶段核心模块

```text
synapse-common
synapse-web
synapse-data
synapse-cache
synapse-security
synapse-audit
synapse-starter
synapse-example
```

## 4. 不做的事项

- 不做完整后台管理系统。
- 不做应用层读写分离。
- 不做应用层数据库故障切换。
- 不做运行时动态增删数据源。
- 不做 Redlock。
- 不做完整微服务治理。
- 不做低代码、工作流、复杂多租户。

## 5. Skill 交付规则

每完成一个模块并通过测试后，必须新增或更新：

```text
skills/<module-name>/SKILL.md
```

`SKILL.md` 必须是最佳实践，不能写成过程日志。

必须包含：

- 模块职责和边界
- 推荐包结构
- 允许使用的技术和禁止事项
- 标准实现模式
- 测试要求
- 常见错误
- Codex 执行前必须读取的文档
- 示例任务拆分方式
