package com.indigo.synapse.cache;

import com.indigo.synapse.common.CommonModule;

public final class CacheModule {

    public static final String NAME = "synapse-cache";

    private CacheModule() {
    }

    public static String dependsOn() {
        return CommonModule.NAME;
    }
}
