package com.indigo.synapse.datasource.autoconfigure;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationMetadataTest {

    @Test
    void shouldGenerateConfigurationMetadata() throws IOException {
        String json = resource("META-INF/spring-configuration-metadata.json");

        assertJsonObject(json);
        assertProperty(json, "synapse.datasource.enabled", "java.lang.Boolean");
        assertProperty(json, "synapse.datasource.convention.master-name", "java.lang.String");
        assertProperty(json, "synapse.datasource.health.interval", "java.time.Duration");
        assertProperty(json, "synapse.datasource.load-balance.default-strategy",
                "com.indigo.synapse.datasource.loadbalance.LoadBalanceStrategy");
        assertProperty(json, "synapse.datasource.router.sql-auto-routing", "java.lang.Boolean");
    }

    @Test
    void shouldProvideAdditionalHints() throws IOException {
        String json = resource("META-INF/additional-spring-configuration-metadata.json");

        assertJsonObject(json);
        assertTrue(json.contains("\"name\": \"synapse.datasource.load-balance.default-strategy\""));
        assertTrue(json.contains("\"value\": \"ROUND_ROBIN\""));
        assertTrue(json.contains("\"name\": \"synapse.datasource.health.interval\""));
        assertTrue(json.contains("\"value\": \"30s\""));
    }

    private static String resource(String name) throws IOException {
        try (var input = ConfigurationMetadataTest.class.getClassLoader().getResourceAsStream(name)) {
            assertNotNull(input, name + " must exist");
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void assertProperty(String json, String name, String type) {
        int index = json.indexOf("\"name\": \"" + name + "\"");
        assertTrue(index >= 0, name + " must exist");
        int next = json.indexOf("\"name\": \"", index + 1);
        String segment = next < 0 ? json.substring(index) : json.substring(index, next);
        assertTrue(segment.contains("\"type\": \"" + type + "\""), name + " type must be " + type);
        assertTrue(segment.contains("\"description\": "), name + " description must exist");
    }

    private static void assertJsonObject(String json) {
        assertFalse(json.isBlank());
        assertTrue(json.trim().startsWith("{"));
        assertTrue(json.trim().endsWith("}"));
    }
}
