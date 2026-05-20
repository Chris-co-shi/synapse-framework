package com.indigo.synapse.audit;

import com.indigo.synapse.common.CommonModule;

public final class AuditModule {

    public static final String NAME = "synapse-audit";

    private AuditModule() {
    }

    public static String dependsOn() {
        return CommonModule.NAME;
    }
}
