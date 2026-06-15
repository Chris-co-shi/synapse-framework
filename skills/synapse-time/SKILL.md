# synapse-time Skill

## 职责

`synapse-time` 只提供时间和时区技术支撑：`TimeZoneResolver`、`TimeRangeConverter`、UTC 查询范围转换和自动配置。

## 禁止事项

- 不做时区配置后台。
- 不做用户资料、组织、工厂、门店等业务规则。
- 不新增 Controller、Entity、Mapper、Repository、migration。
- 不创建 starter、demo、example、sample application。

## 标准实现

- 本地日期按用户 `ZoneId` 转换成 UTC `[startInclusive, endExclusive)`。
- 默认时区来自 `synapse.time.default-zone`。
- 消费方自定义 `TimeZoneResolver` 时自动配置不得覆盖。

## 测试要求

- 覆盖 `LocalDate` 到 UTC 范围。
- 覆盖 `LocalDateTime` 范围。
- 覆盖非法范围。
- 覆盖自动配置和自定义 Bean 不覆盖。

## 必读

- `AGENTS.md`
- `docs/modules/synapse-time.md`
- `docs/phase-2/03-boundary-checklist.md`
