package com.indigo.synapse.security;

import com.indigo.synapse.common.CommonModule;

public final class SecurityModule {

    public static final String NAME = "synapse-security";

    private SecurityModule() {
    }

    public static String dependsOn() {
        return CommonModule.NAME;
    }
}
