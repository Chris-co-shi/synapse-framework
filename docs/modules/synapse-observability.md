# synapse-observability 使用手册

## 模块定位

`synapse-observability` 提供基于 Micrometer Observation 的统一命名、低基数标签和上下文桥接约定。

## 当前事实

Phase 1 仅建立可编译 JAR 和包边界，尚未引入 Micrometer 依赖或自动配置。

## 边界

- 不自研 Tracer。
- 不绑定 Prometheus、Zipkin、Tempo、SkyWalking 或其他具体 APM。
- 不把用户 ID、Token、原始 URL、SQL 或完整异常消息作为标签。
