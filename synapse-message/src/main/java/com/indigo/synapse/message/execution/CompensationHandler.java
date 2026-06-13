package com.indigo.synapse.message.execution;

/**
 * 业务补偿处理器。
 */
public interface CompensationHandler {

    String handlerName();

    void compensate(String messageId, String payload);
}
