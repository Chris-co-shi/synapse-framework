# synapse-observability 使用手册

## 模块定位

`synapse-observability` 提供基于 Micrometer Observation 的统一命名、低基数标签和上下文桥接约定。

## 当前能力

- `SynapseObservationNames`：Security、OAuth2、Messaging、Audit、Datasource、Cache、Resilience 稳定名称。
- `SynapseObservationOperations`：统一成功/失败观测入口。
- 固定低基数 tags：`synapse.module`、`synapse.operation`、`synapse.outcome`。
- `TraceContextProvider`：具体 tracer 向 Framework 提供 traceId/spanId 的 SPI。
- `SynapseObservationMdcHandler`：作用域内写入并恢复 MDC。
- `FrameworkHealthIndicator` / `FrameworkHealthSnapshot`：不依赖 Actuator 的健康扩展约定。
- 自动配置：缺少 `ObservationRegistry` 时使用 NOOP，不创建 exporter 或 tracer。

`module` 与 `operation` 只接受最长 64 位的小写稳定标识。原始 URL、用户 ID、token、完整异常
消息和 SQL 不能作为 tag。Resilience 当前直接使用该入口；Messaging/Audit 的发布链路将在其模块
重构阶段接入同一契约。

## 边界

- 不自研 Tracer。
- 不绑定 Prometheus、Zipkin、Tempo、SkyWalking 或其他具体 APM。
- 不把用户 ID、Token、原始 URL、SQL 或完整异常消息作为标签。
- 不创建 `ObservationRegistry`、MeterRegistry、Tracer 或 exporter。
