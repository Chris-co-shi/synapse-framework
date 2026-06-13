package com.indigo.synapse.message.port;

import com.indigo.synapse.message.failure.MessageFailure;

/**
 * 补偿处理端口。
 */
@FunctionalInterface
public interface CompensationPort {

    void compensate(MessageFailure failure);
}
