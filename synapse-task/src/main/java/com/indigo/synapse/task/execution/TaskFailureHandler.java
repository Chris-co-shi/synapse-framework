package com.indigo.synapse.task.execution;

public interface TaskFailureHandler {

    void handleFailure(ScheduledTaskDefinition definition, TaskExecutionContext context, Throwable failure);
}
