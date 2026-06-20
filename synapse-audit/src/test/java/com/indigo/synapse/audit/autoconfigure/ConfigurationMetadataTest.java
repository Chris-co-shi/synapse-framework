package com.indigo.synapse.audit.autoconfigure;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationMetadataTest {
    @Test
    void shouldGenerateDocumentedMetadata() throws Exception {
        String metadata = new String(getClass().getClassLoader().getResourceAsStream(
                "META-INF/spring-configuration-metadata.json").readAllBytes(), StandardCharsets.UTF_8);
        assertThat(metadata).contains("synapse.audit.enabled")
                .contains("synapse.audit.destination")
                .contains("synapse.audit.aop-enabled")
                .contains("审计消息逻辑 binding 名")
                .contains("是否启用 @Audited 方法切面");
    }
}
