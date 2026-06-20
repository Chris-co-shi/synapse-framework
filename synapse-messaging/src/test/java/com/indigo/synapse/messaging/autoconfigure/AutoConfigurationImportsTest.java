package com.indigo.synapse.messaging.autoconfigure;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigurationImportsTest {
    @Test
    void shouldListBothAutoConfigurations() throws Exception {
        String imports = new String(getClass().getClassLoader().getResourceAsStream(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
                .readAllBytes(), StandardCharsets.UTF_8);
        assertThat(imports).contains(SynapseMessagingAutoConfiguration.class.getName())
                .contains(SynapseMessagingStreamAutoConfiguration.class.getName())
                .contains(SynapseMessagingPublisherAutoConfiguration.class.getName());
    }
}
