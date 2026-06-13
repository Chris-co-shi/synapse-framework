package com.indigo.synapse.task.execution;

import java.util.Optional;

public record TaskExecutionResult(
        TaskExecutionStatus status,
        String message
) {

    public TaskExecutionResult {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
    }

    public static TaskExecutionResult success() {
        return new TaskExecutionResult(TaskExecutionStatus.SUCCESS, null);
    }

    public static TaskExecutionResult failed(String message) {
        return new TaskExecutionResult(TaskExecutionStatus.FAILED, message);
    }

    public boolean succeeded() {
        return status == TaskExecutionStatus.SUCCESS;
    }

    public Optional<String> failureMessage() {
        return status == TaskExecutionStatus.FAILED ? Optional.ofNullable(message) : Optional.empty();
    }
}
