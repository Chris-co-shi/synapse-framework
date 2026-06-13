package com.indigo.synapse.task.execution;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DefaultTaskExecutor implements TaskExecutor {

    private final Map<String, TaskHandler> handlers;
    private final TaskFailureHandler failureHandler;

    public DefaultTaskExecutor(Collection<TaskHandler> handlers, TaskFailureHandler failureHandler) {
        if (handlers == null) {
            throw new IllegalArgumentException("handlers must not be null");
        }
        if (failureHandler == null) {
            throw new IllegalArgumentException("failureHandler must not be null");
        }
        this.handlers = indexHandlers(handlers);
        this.failureHandler = failureHandler;
    }

    @Override
    public TaskExecutionResult execute(ScheduledTaskDefinition definition, TaskExecutionContext context) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        if (!definition.name().equals(context.taskName())) {
            throw new IllegalArgumentException("context taskName must match definition name");
        }
        if (!definition.enabled()) {
            return TaskExecutionResult.failed("task is disabled");
        }
        TaskHandler handler = handlers.get(definition.name());
        if (handler == null) {
            throw new IllegalStateException("task handler not found: " + definition.name());
        }
        try {
            handler.execute(context);
            return TaskExecutionResult.success();
        } catch (RuntimeException failure) {
            failureHandler.handleFailure(definition, context, failure);
            return TaskExecutionResult.failed(failure.getMessage());
        }
    }

    private static Map<String, TaskHandler> indexHandlers(Collection<TaskHandler> handlers) {
        Map<String, TaskHandler> indexed = new LinkedHashMap<>();
        for (TaskHandler handler : handlers) {
            if (handler == null) {
                throw new IllegalArgumentException("handler must not be null");
            }
            String taskName = handler.taskName();
            if (taskName == null || taskName.isBlank()) {
                throw new IllegalArgumentException("handler taskName must not be blank");
            }
            TaskHandler previous = indexed.putIfAbsent(taskName, handler);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate task handler: " + taskName);
            }
        }
        return Map.copyOf(indexed);
    }
}
