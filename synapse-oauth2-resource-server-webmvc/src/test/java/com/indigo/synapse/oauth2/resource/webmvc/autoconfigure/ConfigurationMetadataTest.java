package com.indigo.synapse.oauth2.resource.webmvc.autoconfigure;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationMetadataTest {

    @Test
    void shouldGenerateConfigurationMetadata() throws Exception {
        String metadata = new String(getClass().getClassLoader()
                .getResourceAsStream("META-INF/spring-configuration-metadata.json")
                .readAllBytes(), StandardCharsets.UTF_8);

        assertThat(metadata).contains("synapse.security.resource-server.enabled");
        assertThat(metadata).contains("synapse.security.resource-server.issuer-uri");
        assertThat(metadata).contains("synapse.security.resource-server.audiences");
    }
}
