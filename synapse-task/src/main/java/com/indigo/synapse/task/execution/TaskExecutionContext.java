package com.indigo.synapse.task.execution;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TaskExecutionContext(
        String taskName,
        String executionId,
        Instant triggeredAt,
        Map<String, String> attributes
) {

    public TaskExecutionContext {
        validate(taskName, "taskName");
        validate(executionId, "executionId");
        if (triggeredAt == null) {
            throw new IllegalArgumentException("triggeredAt must not be null");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static TaskExecutionContext start(String taskName) {
        return new TaskExecutionContext(taskName, UUID.randomUUID().toString(), Instant.now(), Map.of());
    }

    private static void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
