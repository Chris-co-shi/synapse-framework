package com.indigo.synapse.security;

import com.indigo.synapse.common.CoreModule;

public final class SecurityModule {

    public static final String NAME = "synapse-security";

    private SecurityModule() {
    }

    public static String dependsOn() {
        return CoreModule.NAME;
    }
}
