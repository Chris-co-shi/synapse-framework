package com.indigo.synapse.data;

import com.indigo.synapse.core.CoreModule;

public final class DataModule {

    public static final String NAME = "synapse-data";

    private DataModule() {
    }

    public static String dependsOn() {
        return CoreModule.NAME;
    }
}
