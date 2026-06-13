package com.indigo.synapse.datapermission;

import com.indigo.synapse.core.CoreModule;

public final class DataPermissionModule {

    public static final String NAME = "synapse-data-permission";

    private DataPermissionModule() {
    }

    public static String dependsOn() {
        return CoreModule.NAME;
    }
}
