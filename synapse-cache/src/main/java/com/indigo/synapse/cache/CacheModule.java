package com.indigo.synapse.cache;

import com.indigo.synapse.common.CoreModule;

public final class CacheModule {

    public static final String NAME = "synapse-cache";

    private CacheModule() {
    }

    public static String dependsOn() {
        return CoreModule.NAME;
    }
}
