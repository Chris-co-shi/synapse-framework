package com.indigo.synapse.webmvc;

import com.indigo.synapse.core.CoreModule;

public final class WebModule {

    public static final String NAME = "synapse-webmvc";

    private WebModule() {
    }

    public static String dependsOn() {
        return CoreModule.NAME;
    }
}
