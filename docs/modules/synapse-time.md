# synapse-time 使用手册

## 1. 模块定位

`synapse-time` 提供时间和时区技术支撑，独立于 `synapse-core`。

当前能力：

- `TimeZoneResolver`：当前调用方时区解析端口。
- `TimeRangeConverter`：本地日期时间到 UTC `Instant` 查询范围转换。
- `FixedTimeZoneResolver`：固定默认时区轻量实现。
- Spring Boot 自动配置。

## 2. 适用场景

- 用户按本地日期查询，数据库按 UTC 时间存储。
- 平台服务需要统一把 `LocalDate` 转换为 UTC 起止范围。
- 消费方需要替换自己的时区解析逻辑。

## 3. 不适用场景

- 时区配置后台。
- 用户资料管理。
- 组织、工厂、门店时区业务规则。
- 可启动 time-service。

## 4. Maven 引入

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-time</artifactId>
</dependency>
```

## 5. 快速使用

```java
TimeRange range = converter.dayRange(LocalDate.of(2026, 6, 15), ZoneId.of("Asia/Shanghai"));
```

结果为 UTC `[startInclusive, endExclusive)`，适合数据库范围查询。

## 6. 配置项

```yaml
synapse:
  time:
    default-zone: UTC
```

## 7. 扩展方式

消费方可以提供自己的 `TimeZoneResolver` Bean，自动配置不会覆盖。

## 8. 边界

- 不并入 core。
- 不依赖 Web、Security、Data。
- 不创建 Controller、Entity、Mapper、migration。
- 不做任何可启动服务。

## 9. Configuration Metadata

`synapse-time` 发布 jar 必须包含 `META-INF/spring-configuration-metadata.json`，覆盖 `synapse.time.default-zone`。常用 ZoneId 候选值通过 additional metadata 提供。
