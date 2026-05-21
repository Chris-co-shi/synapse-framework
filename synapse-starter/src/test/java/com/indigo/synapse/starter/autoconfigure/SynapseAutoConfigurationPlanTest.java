package com.indigo.synapse.starter.autoconfigure;

import com.indigo.synapse.starter.properties.SynapseFeature;
import com.indigo.synapse.starter.properties.SynapseStarterProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseAutoConfigurationPlanTest {

    @Test
    void shouldExposeEnabledModuleNames() {
        SynapseStarterProperties properties = SynapseStarterProperties.defaults()
                .withFeature(SynapseFeature.CACHE, false);
        SynapseAutoConfigurationPlan plan = new SynapseAutoConfigurationPlan(properties);

        assertEquals(
                List.of("synapse-web", "synapse-data", "synapse-security", "synapse-audit"),
                plan.enabledModuleNames()
        );
        assertFalse(plan.shouldConfigure(SynapseFeature.CACHE));
        assertTrue(plan.shouldConfigure(SynapseFeature.WEB));
    }

    @Test
    void shouldNeverCreateExternalConnectionAtPlanLayer() {
        SynapseAutoConfigurationPlan plan = SynapseAutoConfigurationPlan.defaults();

        assertFalse(plan.shouldCreateExternalConnection(SynapseFeature.DATA));
        assertFalse(plan.shouldCreateExternalConnection(SynapseFeature.CACHE));
        assertFalse(plan.shouldCreateExternalConnection(SynapseFeature.WEB));
    }

    @Test
    void shouldValidatePlanInput() {
        SynapseAutoConfigurationPlan plan = SynapseAutoConfigurationPlan.defaults();

        assertThrows(IllegalArgumentException.class, () -> new SynapseAutoConfigurationPlan(null));
        assertThrows(IllegalArgumentException.class, () -> plan.shouldCreateExternalConnection(null));
    }
}
