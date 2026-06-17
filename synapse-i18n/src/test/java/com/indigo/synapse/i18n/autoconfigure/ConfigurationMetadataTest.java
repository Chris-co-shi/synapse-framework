package com.indigo.synapse.i18n.autoconfigure;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationMetadataTest {

    @Test
    void shouldGenerateConfigurationMetadata() throws IOException {
        String json = metadata();

        assertJsonObject(json);
        assertProperty(json, "synapse.i18n.default-locale", "java.util.Locale");
        assertProperty(json, "synapse.i18n.messages", "java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.String>>");
        assertTrue(json.contains("\"name\": \"synapse.i18n.default-locale\""));
        assertTrue(json.contains("\"value\": \"zh-CN\""));
    }

    private static String metadata() throws IOException {
        try (var input = ConfigurationMetadataTest.class.getClassLoader()
                .getResourceAsStream("META-INF/spring-configuration-metadata.json")) {
            assertNotNull(input, "spring-configuration-metadata.json must exist");
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
