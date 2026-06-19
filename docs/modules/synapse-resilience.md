# synapse-resilience 使用手册

## 模块定位

`synapse-resilience` 提供超时、重试、熔断、隔离、异常分类和观测的统一技术边界。

## 当前事实

Phase 1 仅建立可编译 JAR 和包边界，尚未引入 Resilience4j 或 CircuitBreaker 实现。

## 边界

- 非幂等操作默认不重试。
- 不提供返回假成功数据的通用 Fallback。
- 不依赖 Sentinel，不承载业务降级规则。
