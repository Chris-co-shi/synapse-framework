package com.indigo.synapse.example;

import com.indigo.synapse.starter.StarterModule;

public final class ExampleModule {

    public static final String NAME = "synapse-example";

    private ExampleModule() {
    }

    public static String dependsOn() {
        return StarterModule.NAME;
    }
}
