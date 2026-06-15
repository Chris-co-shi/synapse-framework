# synapse-config Skill

## 职责

`synapse-config` 只提供配置抽象、运行时读取、类型解析和轻量默认客户端。

## 禁止事项

- 不做 config-service。
- 不做配置中心后台、发布、审批、灰度。
- 不新增配置数据库表。
- 不新增 Controller、Entity、Mapper、Repository、migration。
- 不创建 starter、demo、example、sample application。

## 标准实现

- 读取端口使用 `ConfigClient`。
- 类型解析使用 `ConfigParser`。
- 组合入口使用 `ConfigResolver`。
- 默认客户端只能是轻量本地实现，外部配置中心由消费方适配。

## 测试要求

- 覆盖配置存在、缺失、空 key。
- 覆盖常用类型解析。
- 覆盖不支持类型。
- 覆盖自动配置和自定义 Bean 不覆盖。

## 必读

- `AGENTS.md`
- `docs/modules/synapse-config.md`
- `docs/phase-2/00-framework-boundary.md`
