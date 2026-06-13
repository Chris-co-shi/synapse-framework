package com.indigo.synapse.message.port;

import com.indigo.synapse.message.failure.MessageFailure;

/**
 * 死信处理端口。
 */
@FunctionalInterface
public interface DeadLetterPort {

    void sendToDeadLetter(MessageFailure failure);
}
