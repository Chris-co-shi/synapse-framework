package com.indigo.synapse.messaging.reliability;

/** 最终消费失败记录端口；Framework 不提供数据库或文件实现。 */
@FunctionalInterface
public interface MessageFailureStore {
    void record(MessageFailure failure);
}
