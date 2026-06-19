package com.indigo.synapse.webmvc.openapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiVisibilityPolicyTest {

    @Test
    void shouldBeVisibleOnlyWhenEnabledAndDevelopmentProfile() {
        OpenApiProperties properties = OpenApiProperties.defaults();

        assertTrue(OpenApiVisibilityPolicy.visible(properties, "dev"));
        assertTrue(OpenApiVisibilityPolicy.visible(properties, "LOCAL"));
        assertTrue(OpenApiVisibilityPolicy.visible(properties, "test"));

        assertFalse(OpenApiVisibilityPolicy.visible(properties, "prod"));
        assertFalse(OpenApiVisibilityPolicy.visible(properties.disabled(), "dev"));
        assertFalse(OpenApiVisibilityPolicy.visible(properties, null));
    }

    @Test
    void shouldValidateOpenApiProperties() {
        assertThrows(IllegalArgumentException.class, () -> new OpenApiProperties(true, "", "1"));
        assertThrows(IllegalArgumentException.class, () -> new OpenApiProperties(true, "API", ""));
        assertThrows(IllegalArgumentException.class, () -> OpenApiVisibilityPolicy.visible(null, "dev"));
    }
}
