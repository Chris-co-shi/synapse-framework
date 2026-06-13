package com.indigo.synapse.task.execution;

public record ScheduledTaskDefinition(
        String name,
        String cronExpression,
        boolean enabled
) {

    public ScheduledTaskDefinition {
        validate(name, "name");
        validate(cronExpression, "cronExpression");
    }

    public static ScheduledTaskDefinition enabled(String name, String cronExpression) {
        return new ScheduledTaskDefinition(name, cronExpression, true);
    }

    private static void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
