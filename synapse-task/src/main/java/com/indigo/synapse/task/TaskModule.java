package com.indigo.synapse.task;

public final class TaskModule {

    public static final String NAME = "synapse-task";

    private TaskModule() {
    }

    public static String dependsOn() {
        return "synapse-core";
    }
}
