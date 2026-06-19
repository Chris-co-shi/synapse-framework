# synapse-observability Skill

## 职责

提供 Micrometer Observation 稳定命名、低基数标签、trace context SPI、MDC 和健康扩展约定。

## 规则

- Observation 名称使用 `SynapseObservationNames`。
- module/operation 必须是稳定低基数标识，禁止 URL、用户 ID、token、SQL 和异常消息。
- 具体 tracer 通过 `TraceContextProvider` 适配；Framework 不创建 tracer 或 exporter。
- 缺少 registry 时保持 NOOP，不得阻止业务启动。

## 验证

- 成功和异常路径都必须停止 Observation。
- MDC handler 停止时必须恢复调用前值。
- 不引入 Prometheus、Zipkin、Tempo、SkyWalking 等实现依赖。
