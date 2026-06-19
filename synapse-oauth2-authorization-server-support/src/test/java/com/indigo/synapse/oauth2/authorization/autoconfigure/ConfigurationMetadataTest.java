package com.indigo.synapse.oauth2.authorization.autoconfigure;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationMetadataTest {

    @Test
    void shouldGenerateConfigurationMetadata() throws Exception {
        String metadata = readResource("META-INF/spring-configuration-metadata.json");

        assertThat(metadata).contains("synapse.oauth2.authorization.development-key-enabled");
        assertThat(metadata).contains("synapse.oauth2.authorization.production");
        assertThat(metadata).contains("synapse.oauth2.authorization.key-id");
        assertThat(metadata).contains("是否启用开发 RSAKey 自动生成。默认关闭。");
        assertThat(metadata).contains("生产环境不得使用默认 key id。");
    }

    @Test
    void shouldProvideAdditionalConfigurationMetadataHints() throws Exception {
        String metadata = readResource("META-INF/additional-spring-configuration-metadata.json");

        assertThat(metadata).contains("synapse.oauth2.authorization.key-id");
        assertThat(metadata).contains("synapse-dev");
        assertThat(metadata).contains("开发环境默认 key id；生产环境不得使用该值。");
    }

    private String readResource(String resourceName) throws Exception {
        return new String(getClass().getClassLoader()
                .getResourceAsStream(resourceName)
                .readAllBytes(), StandardCharsets.UTF_8);
    }
}
