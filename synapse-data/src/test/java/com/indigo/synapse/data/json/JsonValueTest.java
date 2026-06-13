package com.indigo.synapse.data.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonValueTest {

    @Test
    void shouldCreateJsonValue() {
        JsonValue value = JsonValue.of("{\"name\":\"synapse\"}");

        assertEquals("{\"name\":\"synapse\"}", value.value());
        assertEquals(value, JsonValue.of("{\"name\":\"synapse\"}"));
        assertEquals("{\"name\":\"synapse\"}", value.toString());
    }

    @Test
    void shouldRejectNullAndBlankValue() {
        assertThrows(IllegalArgumentException.class, () -> JsonValue.of(null));
        assertThrows(IllegalArgumentException.class, () -> JsonValue.of(" "));
    }

    @Test
    void shouldCreateNullableValue() {
        assertNull(JsonValue.ofNullable(null));
        assertEquals(JsonValue.of("null"), JsonValue.ofNullable("null"));
    }
}
