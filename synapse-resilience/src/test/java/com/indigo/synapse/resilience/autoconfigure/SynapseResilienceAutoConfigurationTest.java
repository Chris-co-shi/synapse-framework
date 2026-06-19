package com.indigo.synapse.resilience.autoconfigure;

import com.indigo.synapse.observability.autoconfigure.SynapseObservabilityAutoConfiguration;
import com.indigo.synapse.resilience.ResilienceOperations;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseResilienceAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SynapseObservabilityAutoConfiguration.class,
                    SynapseResilienceAutoConfiguration.class));

    @Test
    void shouldRegisterFoundationBeans() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(ResilienceOperations.class);
            assertThat(context).hasBean("synapseResilienceExecutor");
        });
    }

    @Test
    void shouldUseNamedUserExecutorWithoutAmbiguity() {
        ExecutorService custom = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();
        runner.withBean("synapseResilienceExecutor", ExecutorService.class, () -> custom)
                .withBean("applicationExecutor", ExecutorService.class,
                        java.util.concurrent.Executors::newVirtualThreadPerTaskExecutor)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ResilienceOperations.class);
                });
    }
}
