package com.indigo.synapse.starter.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseBootPropertiesTest {

    @Test
    void shouldEnableAllFeaturesByDefault() {
        SynapseBootProperties properties = new SynapseBootProperties();

        for (SynapseFeature feature : SynapseFeature.values()) {
            assertTrue(properties.isEnabled(feature));
        }
    }

    @Test
    void shouldConvertToStarterProperties() {
        SynapseBootProperties properties = new SynapseBootProperties();
        properties.getCache().setEnabled(false);

        SynapseStarterProperties starterProperties = properties.toStarterProperties();

        assertFalse(starterProperties.isEnabled(SynapseFeature.CACHE));
        assertTrue(starterProperties.isEnabled(SynapseFeature.WEB));
    }

    @Test
    void shouldRejectNullFeature() {
        assertThrows(IllegalArgumentException.class, () -> new SynapseBootProperties().isEnabled(null));
    }
}
