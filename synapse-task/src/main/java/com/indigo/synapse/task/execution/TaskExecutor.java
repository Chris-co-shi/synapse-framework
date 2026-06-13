package com.indigo.synapse.task.execution;

public interface TaskExecutor {

    TaskExecutionResult execute(ScheduledTaskDefinition definition, TaskExecutionContext context);
}
