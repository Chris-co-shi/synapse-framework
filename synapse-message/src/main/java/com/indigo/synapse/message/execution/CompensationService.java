package com.indigo.synapse.message.execution;

import com.indigo.synapse.message.port.CompensationRepository;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 补偿任务服务。
 *
 * <p>框架只负责补偿任务记录和调用消费方 handler，不内置任何业务补偿逻辑。</p>
 */
public final class CompensationService {

    private final CompensationRepository repository;
    private final Map<String, CompensationHandler> handlers;
    private final Clock clock;

    public CompensationService(CompensationRepository repository, java.util.Collection<CompensationHandler> handlers, Clock clock) {
        if (repository == null || handlers == null) {
            throw new IllegalArgumentException("compensation dependencies must not be null");
        }
        this.repository = repository;
        this.handlers = handlers.stream().collect(Collectors.toUnmodifiableMap(CompensationHandler::handlerName, Function.identity()));
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public String register(String messageId, String handlerName, String payload) {
        validate(messageId, "messageId");
        validate(handlerName, "handlerName");
        String compensationId = UUID.randomUUID().toString();
        repository.save(compensationId, messageId, handlerName, payload, clock.instant());
        return compensationId;
    }

    public void compensate(String compensationId, String messageId, String handlerName, String payload) {
        validate(compensationId, "compensationId");
        validate(messageId, "messageId");
        validate(handlerName, "handlerName");
        CompensationHandler handler = handlers.get(handlerName);
        if (handler == null) {
            throw new IllegalStateException("compensation handler not found: " + handlerName);
        }
        try {
            handler.compensate(messageId, payload);
            repository.markSucceeded(compensationId, clock.instant());
        } catch (RuntimeException failure) {
            repository.markFailed(compensationId, failure.getMessage(), clock.instant());
            throw failure;
        }
    }

    private static void validate(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
