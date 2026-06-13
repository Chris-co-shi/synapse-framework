package com.indigo.synapse.file.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "synapse.file")
public class SynapseFileProperties {

    private Path localRoot = Path.of(System.getProperty("java.io.tmpdir"), "synapse-file");

    public Path getLocalRoot() {
        return localRoot;
    }

    public void setLocalRoot(Path localRoot) {
        this.localRoot = localRoot;
    }
}
