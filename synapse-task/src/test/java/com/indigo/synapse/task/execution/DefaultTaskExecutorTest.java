package com.indigo.synapse.task.execution;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultTaskExecutorTest {

    @Test
    void shouldExecuteMatchedHandler() {
        AtomicBoolean executed = new AtomicBoolean(false);
        TaskHandler handler = new RecordingTaskHandler("sync-user", executed);
        DefaultTaskExecutor executor = new DefaultTaskExecutor(List.of(handler), new NoopTaskFailureHandler());

        TaskExecutionResult result = executor.execute(
                ScheduledTaskDefinition.enabled("sync-user", "0 0 * * * *"),
                TaskExecutionContext.start("sync-user")
        );

        assertTrue(result.succeeded());
        assertTrue(executed.get());
    }

    @Test
    void shouldCallFailureHandlerAndReturnFailedResult() {
        RuntimeException failure = new RuntimeException("network timeout");
        AtomicReference<Throwable> capturedFailure = new AtomicReference<>();
        TaskHandler handler = new FailingTaskHandler("sync-user", failure);
        TaskFailureHandler failureHandler = (definition, context, throwable) -> capturedFailure.set(throwable);
        DefaultTaskExecutor executor = new DefaultTaskExecutor(List.of(handler), failureHandler);

        TaskExecutionResult result = executor.execute(
                ScheduledTaskDefinition.enabled("sync-user", "0 0 * * * *"),
                TaskExecutionContext.start("sync-user")
        );

        assertFalse(result.succeeded());
        assertEquals(failure, capturedFailure.get());
        assertEquals("network timeout", result.failureMessage().orElseThrow());
    }

    @Test
    void shouldRejectMissingOrDuplicateHandler() {
        DefaultTaskExecutor executor = new DefaultTaskExecutor(List.of(), new NoopTaskFailureHandler());
        ScheduledTaskDefinition definition = ScheduledTaskDefinition.enabled("sync-user", "0 0 * * * *");

        assertThrows(IllegalStateException.class, () -> executor.execute(definition, TaskExecutionContext.start("sync-user")));
        assertThrows(
                IllegalArgumentException.class,
                () -> new DefaultTaskExecutor(
                        List.of(new RecordingTaskHandler("sync-user", new AtomicBoolean()), new RecordingTaskHandler("sync-user", new AtomicBoolean())),
                        new NoopTaskFailureHandler()
                )
        );
    }

    @Test
    void shouldRejectMismatchedContext() {
        DefaultTaskExecutor executor = new DefaultTaskExecutor(
                List.of(new RecordingTaskHandler("sync-user", new AtomicBoolean())),
                new NoopTaskFailureHandler()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(ScheduledTaskDefinition.enabled("sync-user", "0 0 * * * *"), TaskExecutionContext.start("other-task"))
        );
    }

    private record RecordingTaskHandler(String taskName, AtomicBoolean executed) implements TaskHandler {

        @Override
        public void execute(TaskExecutionContext context) {
            executed.set(true);
        }
    }

    private record FailingTaskHandler(String taskName, RuntimeException failure) implements TaskHandler {

        @Override
        public void execute(TaskExecutionContext context) {
            throw failure;
        }
    }
}
