package com.indigo.synapse.file;

public final class FileModule {

    public static final String NAME = "synapse-file";

    private FileModule() {
    }

    public static String dependsOn() {
        return "synapse-core";
    }
}
