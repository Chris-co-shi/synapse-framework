package com.indigo.synapse.starter.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseStarterPropertiesTest {

    @Test
    void shouldEnableAllFoundationFeaturesByDefault() {
        SynapseStarterProperties properties = SynapseStarterProperties.defaults();

        for (SynapseFeature feature : SynapseFeature.values()) {
            assertTrue(properties.isEnabled(feature));
        }
    }

    @Test
    void shouldDisableFeatureExplicitlyWithoutMutatingOriginal() {
        SynapseStarterProperties defaults = SynapseStarterProperties.defaults();
        SynapseStarterProperties customized = defaults.withFeature(SynapseFeature.CACHE, false);

        assertTrue(defaults.isEnabled(SynapseFeature.CACHE));
        assertFalse(customized.isEnabled(SynapseFeature.CACHE));
    }

    @Test
    void shouldRejectNullFeature() {
        SynapseStarterProperties properties = SynapseStarterProperties.defaults();

        assertThrows(IllegalArgumentException.class, () -> properties.isEnabled(null));
        assertThrows(IllegalArgumentException.class, () -> properties.withFeature(null, true));
    }
}
