package com.indigo.synapse.observability;

import java.util.concurrent.Callable;

/** 执行 Framework Observation 的稳定入口。 */
public interface SynapseObservationOperations {

    /**
     * @param name {@link SynapseObservationNames} 中的稳定名称
     * @param module 稳定模块 tag
     * @param operation 稳定操作 tag，禁止 URL、用户 ID、token、SQL 或异常消息
     * @param action 被观测操作
     * @return 操作结果
     * @throws Exception 原始操作异常
     */
    <T> T observe(String name, String module, String operation, Callable<T> action) throws Exception;
}
