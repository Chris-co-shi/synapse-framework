package com.indigo.synapse.web;

import com.indigo.synapse.common.CoreModule;

public final class WebModule {

    public static final String NAME = "synapse-web";

    private WebModule() {
    }

    public static String dependsOn() {
        return CoreModule.NAME;
    }
}
