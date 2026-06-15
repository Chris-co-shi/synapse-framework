# synapse-i18n Skill

## 职责

`synapse-i18n` 只提供 Locale 解析、资源加载和消息格式化抽象。

## 禁止事项

- 不做 i18n-resource-center。
- 不做翻译审批、资源发布、语言维护后台。
- 不新增 Controller、Entity、Mapper、Repository、migration。
- 不创建 starter、demo、example、sample application。

## 标准实现

- 当前 Locale 通过 `LocaleResolver` 获取。
- 消息资源通过 `I18nResourceLoader` 加载。
- 消息解析通过 `I18nMessageResolver` 完成。
- 默认实现只能使用轻量本地 Map，不连接平台资源中心。

## 测试要求

- 覆盖按 Locale 解析消息。
- 覆盖参数格式化。
- 覆盖缺失 key。
- 覆盖自动配置和自定义 Bean 不覆盖。

## 必读

- `AGENTS.md`
- `docs/modules/synapse-i18n.md`
- `docs/phase-2/00-framework-boundary.md`
