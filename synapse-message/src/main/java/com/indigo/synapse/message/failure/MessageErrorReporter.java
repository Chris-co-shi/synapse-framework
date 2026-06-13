package com.indigo.synapse.message.failure;

/**
 * 消息错误上报接口。
 */
@FunctionalInterface
public interface MessageErrorReporter {

    void report(MessageFailure failure);
}
