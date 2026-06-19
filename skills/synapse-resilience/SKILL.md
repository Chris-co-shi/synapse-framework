# synapse-resilience Skill

## 职责

基于 Resilience4j 提供 timeout、retry、circuit breaker、bulkhead 与 Observation 编排。

## 规则

- 非幂等操作 `maxAttempts` 必须为 1。
- 重试必须由 `ResilienceExceptionClassifier` 明确分类。
- policy name 必须稳定且低基数，同名策略首次配置后不得动态变化。
- 异常和拒绝必须向调用方传播，不提供通用假成功 fallback。

## 验证

- 测试幂等重试、非重试异常、超时、熔断状态和并发隔离。
- 检查不依赖 Sentinel 或具体 APM。
