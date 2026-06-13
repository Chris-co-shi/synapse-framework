package com.indigo.synapse.message;

import com.indigo.synapse.core.CoreModule;

public final class MessageModule {

    public static final String NAME = "synapse-message";

    private MessageModule() {
    }

    public static String dependsOn() {
        return CoreModule.NAME;
    }
}
