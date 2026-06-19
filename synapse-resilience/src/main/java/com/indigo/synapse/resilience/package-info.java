/**
 * 超时、重试、熔断和隔离的通用韧性边界。
 *
 * <p>基于 Resilience4j 提供保守的 timeout、retry、circuit breaker、bulkhead 与 Observation 编排，
 * 不依赖 Sentinel，也不提供返回假成功数据的通用降级。</p>
 */
package com.indigo.synapse.resilience;
