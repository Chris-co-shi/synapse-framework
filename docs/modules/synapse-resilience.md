# synapse-resilience 使用手册

## 模块定位

`synapse-resilience` 提供超时、重试、熔断、隔离、异常分类和观测的统一技术边界。

## 当前能力

- `ResiliencePolicy`：显式声明幂等性、尝试次数、超时、熔断和 bulkhead 参数。
- `ResilienceExceptionClassifier`：消费方可覆盖的可重试异常分类端口。
- 默认只重试 `IOException` 与 `TimeoutException` 链路。
- `DefaultResilienceOperations`：按 bulkhead、circuit breaker、retry、time limiter 编排。
- 同名策略首次使用时固定配置并保留熔断状态。
- Java 21 虚拟线程执行超时任务；可通过命名 Bean `synapseResilienceExecutor` 覆盖。
- 每次执行使用 `synapse.resilience` Observation，operation 为稳定 policy name。

示例：

```java
ResiliencePolicy policy = new ResiliencePolicy(
        "inventory-read", true, 3, Duration.ofSeconds(2),
        50, 10, Duration.ofSeconds(30), 25, Duration.ZERO
);
Inventory result = resilienceOperations.execute(policy, client::loadInventory);
```

非幂等策略的 `maxAttempts` 必须为 1，否则构造时立即失败。执行异常、超时、熔断和隔离拒绝
都会向调用方抛出，不转换为默认值。

## 边界

- 非幂等操作默认不重试。
- 不提供返回假成功数据的通用 Fallback。
- 不依赖 Sentinel，不承载业务降级规则。
- 不把 policy name 动态拼接 URL、用户、订单或租户标识。
