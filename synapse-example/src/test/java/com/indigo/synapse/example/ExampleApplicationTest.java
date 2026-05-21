package com.indigo.synapse.example;

import com.indigo.synapse.cache.script.RedisScriptExecutor;
import com.indigo.synapse.starter.autoconfigure.SynapseAutoConfigurationPlan;
import com.indigo.synapse.starter.properties.SynapseFeature;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExampleApplicationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ExampleApplication.class)
            .withPropertyValues(
                    "synapse.web.enabled=false",
                    "synapse.data.enabled=false",
                    "synapse.cache.enabled=false",
                    "synapse.security.enabled=false"
            );

    @Test
    void shouldStartThroughStarterAndApplyFeatureSwitches() {
        contextRunner.run(context -> {
            assertEquals(1, context.getBeansOfType(SynapseAutoConfigurationPlan.class).size());
            SynapseAutoConfigurationPlan plan = context.getBean(SynapseAutoConfigurationPlan.class);

            assertFalse(plan.shouldConfigure(SynapseFeature.WEB));
            assertFalse(plan.shouldConfigure(SynapseFeature.DATA));
            assertFalse(plan.shouldConfigure(SynapseFeature.CACHE));
            assertFalse(plan.shouldConfigure(SynapseFeature.SECURITY));
            assertTrue(plan.shouldConfigure(SynapseFeature.AUDIT));
            assertFalse(context.containsBean("synapseRedisScriptExecutor"));
            assertFalse(context.getBeansOfType(RedisScriptExecutor.class).containsKey("synapseRedisScriptExecutor"));
        });
    }
}
