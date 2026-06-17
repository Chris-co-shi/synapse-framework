package com.indigo.synapse.oauth2.authorization.autoconfigure;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationMetadataTest {

    @Test
    void shouldGenerateConfigurationMetadata() throws Exception {
        String metadata = new String(getClass().getClassLoader()
                .getResourceAsStream("META-INF/spring-configuration-metadata.json")
                .readAllBytes(), StandardCharsets.UTF_8);

        assertThat(metadata).contains("synapse.oauth2.authorization.development-key-enabled");
        assertThat(metadata).contains("synapse.oauth2.authorization.production");
        assertThat(metadata).contains("synapse.oauth2.authorization.key-id");
    }
}
