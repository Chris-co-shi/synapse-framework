package com.indigo.synapse.observability.autoconfigure;

import com.indigo.synapse.observability.*;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseObservabilityAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseObservabilityAutoConfiguration.class));

    @Test
    void shouldStartWithoutRegistryOrTraceProvider() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(SynapseObservationOperations.class);
            assertThat(context).doesNotHaveBean(SynapseObservationMdcHandler.class);
        });
    }

    @Test
    void shouldRegisterMdcHandlerWhenTraceProviderExists() {
        ObservationRegistry registry = ObservationRegistry.create();
        runner.withBean(ObservationRegistry.class, () -> registry)
                .withBean(TraceContextProvider.class, () -> () -> java.util.Optional.of("trace-1"))
                .run(context -> assertThat(context).hasSingleBean(SynapseObservationMdcHandler.class));
    }

    @Test
    void shouldKeepUserObservationOperations() {
        SynapseObservationOperations custom = new SynapseObservationOperations() {
            @Override
            public <T> T observe(String name, String module, String operation,
                                 java.util.concurrent.Callable<T> action) throws Exception {
                return action.call();
            }
        };
        runner.withBean(SynapseObservationOperations.class, () -> custom)
                .run(context -> assertThat(context.getBean(SynapseObservationOperations.class)).isSameAs(custom));
    }
}
