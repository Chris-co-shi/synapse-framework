package com.indigo.synapse.tenant;

import com.indigo.synapse.core.CoreModule;

public final class TenantModule {

    public static final String NAME = "synapse-tenant";

    private TenantModule() {
    }

    public static String dependsOn() {
        return CoreModule.NAME;
    }
}
