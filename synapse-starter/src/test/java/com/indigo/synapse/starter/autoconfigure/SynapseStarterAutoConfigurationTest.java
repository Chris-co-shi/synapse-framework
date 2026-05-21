package com.indigo.synapse.starter.autoconfigure;

import com.indigo.synapse.starter.properties.SynapseFeature;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseStarterAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseStarterAutoConfiguration.class));

    @Test
    void shouldCreatePlanFromProperties() {
        contextRunner
                .withPropertyValues("synapse.cache.enabled=false")
                .run(context -> {
                    SynapseAutoConfigurationPlan plan = context.getBean(SynapseAutoConfigurationPlan.class);

                    assertFalse(plan.shouldConfigure(SynapseFeature.CACHE));
                    assertTrue(plan.shouldConfigure(SynapseFeature.WEB));
                });
    }

    @Test
    void shouldKeepDefaultCompatibility() {
        contextRunner.run(context -> {
            SynapseAutoConfigurationPlan plan = context.getBean(SynapseAutoConfigurationPlan.class);

            assertTrue(plan.shouldConfigure(SynapseFeature.SECURITY));
            assertTrue(plan.shouldConfigure(SynapseFeature.AUDIT));
        });
    }
}
