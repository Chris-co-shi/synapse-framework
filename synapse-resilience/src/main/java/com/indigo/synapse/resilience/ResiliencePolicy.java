package com.indigo.synapse.resilience;

import java.time.Duration;

/**
 * 单个稳定操作的韧性策略。
 *
 * @param name 稳定策略名，不得包含 URL、用户 ID 等高基数值
 * @param idempotent 操作是否明确幂等
 * @param maxAttempts 最大尝试次数；非幂等操作只能为 1
 * @param timeout 总执行超时
 * @param failureRateThreshold 熔断失败率阈值
 * @param minimumNumberOfCalls 计算失败率前的最小调用数
 * @param openStateWaitDuration 熔断打开等待时间
 * @param maxConcurrentCalls 最大并发调用数
 * @param bulkheadWaitDuration 等待隔离许可的最长时间
 */
public record ResiliencePolicy(String name, boolean idempotent, int maxAttempts, Duration timeout,
                               float failureRateThreshold, int minimumNumberOfCalls,
                               Duration openStateWaitDuration, int maxConcurrentCalls,
                               Duration bulkheadWaitDuration) {

    public ResiliencePolicy {
        if (name == null || !name.matches("[a-z0-9._-]{1,64}")) {
            throw new IllegalArgumentException("name must be a stable low-cardinality value");
        }
        if (maxAttempts < 1 || (!idempotent && maxAttempts > 1)) {
            throw new IllegalArgumentException("non-idempotent operation cannot be retried");
        }
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        if (failureRateThreshold <= 0 || failureRateThreshold > 100 || minimumNumberOfCalls < 1) {
            throw new IllegalArgumentException("invalid circuit breaker thresholds");
        }
        if (openStateWaitDuration == null || openStateWaitDuration.isNegative()
                || maxConcurrentCalls < 1 || bulkheadWaitDuration == null || bulkheadWaitDuration.isNegative()) {
            throw new IllegalArgumentException("invalid wait duration or bulkhead capacity");
        }
    }

    /** 保守默认策略：单次执行、不重试、30 秒超时。 */
    public static ResiliencePolicy conservative(String name) {
        return new ResiliencePolicy(name, false, 1, Duration.ofSeconds(30),
                50, 10, Duration.ofSeconds(30), 25, Duration.ZERO);
    }
}
