package com.indigo.synapse.resilience;

import java.util.concurrent.Callable;

/** timeout、retry、circuit breaker 与 bulkhead 的统一执行入口。 */
public interface ResilienceOperations {

    /**
     * @param policy 韧性策略
     * @param action 原始操作
     * @return 原始结果
     * @throws Exception 原始异常或 timeout/circuit/bulkhead 拒绝异常
     */
    <T> T execute(ResiliencePolicy policy, Callable<T> action) throws Exception;
}
