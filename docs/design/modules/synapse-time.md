# synapse-time 设计说明

## 1. 模块使命

`synapse-time` 统一真实时间点、业务日期和时区之间的转换规则，使数据库 UTC 存储与用户/业务发生地本地日期查询能够正确衔接。

## 2. 为什么不放进 core

时间范围转换虽然通用，但包含明确的时区策略和 Spring Boot 配置。core 应保持最小纯技术契约，不承担默认 ZoneId 或日期查询规则，因此 time 独立成模块。

## 3. 时间模型原则

- 真实时间点：Java 使用 `Instant`，数据库统一存 UTC。
- 业务日期：使用 `LocalDate` 单独表达。
- 业务发生地时区：使用 IANA `ZoneId` 单独表达。
- 不用服务器默认时区解释业务时间。
- 不用 `LocalDateTime` 表示跨时区真实时间点。
- 数据库范围查询统一使用 `[startInclusive, endExclusive)`。

## 4. 边界

负责：

- `TimeZoneResolver` 时区解析端口。
- `TimeRangeConverter` 本地日期/时间到 UTC Instant 范围转换。
- `FixedTimeZoneResolver` 轻量默认实现。
- 默认 ZoneId 自动配置。

不负责：

- 用户、组织、工厂时区数据管理。
- 时区配置后台。
- 数据库字段设计和 SQL。
- 可启动 time-service。

## 5. 核心对象角色

### 5.1 `TimeZoneResolver`

回答“当前调用应该使用哪个 ZoneId”。Framework 只定义端口；消费方可根据用户偏好、工厂、门店或请求头实现。

### 5.2 `TimeRangeConverter`

负责明确转换：

```text
LocalDate + ZoneId
  -> zoned start of day
  -> next day start
  -> UTC Instant range [start, end)
```

使用下一日零点而不是固定加 24 小时，可正确处理夏令时日期。

### 5.3 `TimeRange`

表达 UTC 半开区间，避免 `23:59:59.999...` 精度和数据库类型差异。

## 6. 主链路

```text
Query local business date
  -> determine explicit ZoneId
  -> TimeRangeConverter
  -> UTC startInclusive / endExclusive
  -> repository range query
  -> display Instant using viewer ZoneId
```

查询发生地时区和查看人展示时区可能不同，不能混用。

## 7. 失败边界

- ZoneId 缺失：由入口策略决定是否使用明确配置的默认值，禁止使用 JVM 默认时区兜底。
- 非法 ZoneId：配置绑定或解析阶段明确失败。
- DST：依赖 `ZoneId` 规则计算边界，不假设一天固定 24 小时。
- end 必须大于 start，范围对象应维护不变量。

## 8. 扩展原则

- 用户时区：自定义 `TimeZoneResolver`。
- 业务发生地时区：业务实体单独保存 ZoneId，并在调用 converter 时显式传入。
- 更复杂查询语义可引入 `TimeQueryScope` / `TimeRangeResolver`，但仍必须输出 UTC 半开区间。

## 9. 源码阅读顺序

```text
TimeRange
  -> TimeZoneResolver
  -> FixedTimeZoneResolver
  -> TimeRangeConverter
  -> TimeProperties
  -> TimeAutoConfiguration
  -> DST and boundary tests
```

## 10. 手写练习

1. 将 `2026-06-18` + `Asia/Tokyo` 转成 UTC 半开区间。
2. 选择一个有 DST 的 ZoneId 测试切换日。
3. 验证不能使用 `date.atStartOfDay(ZoneId.systemDefault())`。
4. 写一个根据业务工厂 ZoneId 返回范围的 resolver。

## 11. 修改检查清单

- 是否使用了 JVM / 服务器默认时区。
- 是否把真实时间点建模为 LocalDateTime。
- 是否使用闭区间结束时间。
- 是否假设一天固定 24 小时。
- 是否把用户/工厂时区业务管理放进 Framework。
- 默认配置是否明确为 IANA ZoneId。
