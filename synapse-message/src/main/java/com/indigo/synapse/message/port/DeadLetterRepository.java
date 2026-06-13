package com.indigo.synapse.message.port;

import com.indigo.synapse.message.core.ReliableMessage;

/**
 * 死信仓储端口。
 */
public interface DeadLetterRepository {

    void save(ReliableMessage message, String reason);
}
