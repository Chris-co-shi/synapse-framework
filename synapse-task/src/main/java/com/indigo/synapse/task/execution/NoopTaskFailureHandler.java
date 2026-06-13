package com.indigo.synapse.task.execution;

public final class NoopTaskFailureHandler implements TaskFailureHandler {

    @Override
    public void handleFailure(ScheduledTaskDefinition definition, TaskExecutionContext context, Throwable failure) {
        if (definition == null || context == null || failure == null) {
            throw new IllegalArgumentException("failure context must not be null");
        }
    }
}
