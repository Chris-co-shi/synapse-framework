package com.indigo.synapse.webflux;

import com.indigo.synapse.web.core.WebCoreModule;

public final class WebFluxModule {

    public static final String NAME = "synapse-webflux";

    private WebFluxModule() {
    }

    public static String dependsOn() {
        return WebCoreModule.NAME;
    }
}
