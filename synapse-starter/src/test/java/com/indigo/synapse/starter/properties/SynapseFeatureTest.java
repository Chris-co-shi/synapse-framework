package com.indigo.synapse.starter.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseFeatureTest {

    @Test
    void shouldMarkExternalInfrastructureFeatures() {
        assertTrue(SynapseFeature.DATA.requiresExternalInfrastructure());
        assertTrue(SynapseFeature.CACHE.requiresExternalInfrastructure());

        assertFalse(SynapseFeature.WEB.requiresExternalInfrastructure());
        assertFalse(SynapseFeature.SECURITY.requiresExternalInfrastructure());
        assertFalse(SynapseFeature.AUDIT.requiresExternalInfrastructure());
    }

    @Test
    void shouldResolveFeatureFromAutoConfigurationClassName() {
        assertEquals(
                SynapseFeature.WEB,
                SynapseFeature.fromAutoConfigurationClassName("com.indigo.synapse.web.autoconfigure.SynapseWebAutoConfiguration")
        );
        assertEquals(
                SynapseFeature.WEB,
                SynapseFeature.fromAutoConfigurationClassName("com.indigo.synapse.web.autoconfigure.SynapseWebMvcAutoConfiguration")
        );
        assertEquals(
                SynapseFeature.DATA,
                SynapseFeature.fromAutoConfigurationClassName("org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration")
        );
        assertEquals(
                SynapseFeature.CACHE,
                SynapseFeature.fromAutoConfigurationClassName("org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration")
        );
        assertNull(SynapseFeature.fromAutoConfigurationClassName("com.example.OtherAutoConfiguration"));
    }
}
