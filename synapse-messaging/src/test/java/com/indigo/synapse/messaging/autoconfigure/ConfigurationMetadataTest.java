package com.indigo.synapse.messaging.autoconfigure;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationMetadataTest {
    @Test
    void shouldGenerateDocumentedConfigurationMetadata() throws Exception {
        String metadata = new String(getClass().getClassLoader()
                .getResourceAsStream("META-INF/spring-configuration-metadata.json")
                .readAllBytes(), StandardCharsets.UTF_8);
        assertThat(metadata)
                .contains("synapse.messaging.enabled")
                .contains("synapse.messaging.reliable.enabled")
                .contains("synapse.messaging.stream.enabled")
                .contains("开启后必须提供 OutboxStore Bean")
                .contains("StreamBridge 存在时创建默认 MessageTransport");
    }
}
