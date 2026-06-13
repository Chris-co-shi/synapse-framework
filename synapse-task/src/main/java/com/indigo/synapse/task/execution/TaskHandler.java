package com.indigo.synapse.task.execution;

public interface TaskHandler {

    String taskName();

    void execute(TaskExecutionContext context);
}
