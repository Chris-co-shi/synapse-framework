package com.indigo.synapse.data;

import com.indigo.synapse.common.CommonModule;

public final class DataModule {

    public static final String NAME = "synapse-data";

    private DataModule() {
    }

    public static String dependsOn() {
        return CommonModule.NAME;
    }
}
