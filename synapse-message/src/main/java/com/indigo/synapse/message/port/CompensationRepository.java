package com.indigo.synapse.message.port;

import java.time.Instant;

/**
 * 补偿任务仓储端口。
 */
public interface CompensationRepository {

    void save(String compensationId, String messageId, String handlerName, String payload, Instant now);

    void markSucceeded(String compensationId, Instant now);

    void markFailed(String compensationId, String errorMessage, Instant now);
}
