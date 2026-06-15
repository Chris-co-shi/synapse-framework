package com.indigo.synapse.webflux;

import com.indigo.synapse.core.CoreModule;

public final class WebFluxModule {

    public static final String NAME = "synapse-webflux";

    private WebFluxModule() {
    }

    public static String dependsOn() {
        return CoreModule.NAME;
    }
}
