package com.indigo.synapse.mybatisplus.autoconfigure;

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
        assertProperty(json, "synapse.mybatis-plus.enabled", "java.lang.Boolean");
        assertProperty(json, "synapse.mybatis-plus.pagination.db-type",
                "com.baomidou.mybatisplus.annotation.DbType");
        assertProperty(json, "synapse.mybatis-plus.pagination.max-limit", "java.lang.Long");
        assertProperty(json, "synapse.mybatis-plus.illegal-sql.enabled", "java.lang.Boolean");
        assertProperty(json, "synapse.mybatis-plus.audit-fill.enabled", "java.lang.Boolean");
    }

    @Test
    void shouldProvideAdditionalHints() throws IOException {
        String json = resource("META-INF/additional-spring-configuration-metadata.json");

        assertJsonObject(json);
        assertTrue(json.contains("\"name\": \"synapse.mybatis-plus.pagination.db-type\""));
        assertTrue(json.contains("\"value\": \"POSTGRE_SQL\""));
        assertTrue(json.contains("\"value\": \"MYSQL\""));
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
