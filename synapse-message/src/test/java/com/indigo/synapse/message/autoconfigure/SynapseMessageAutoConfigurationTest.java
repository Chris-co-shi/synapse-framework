package com.indigo.synapse.message.autoconfigure;

import com.indigo.synapse.message.context.OperationContextMessageCodec;
import com.indigo.synapse.message.context.OperationContextMessagePropagator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseMessageAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseMessageAutoConfiguration.class));

    @Test
    void shouldCreateContextPropagationBeansByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OperationContextMessageCodec.class);
            assertThat(context).hasSingleBean(OperationContextMessagePropagator.class);
        });
    }

    @Test
    void shouldBackOffWhenApplicationProvidesContextPropagationBeans() {
        OperationContextMessageCodec codec = new OperationContextMessageCodec();
        OperationContextMessagePropagator propagator = new OperationContextMessagePropagator(codec);

        contextRunner
                .withBean(OperationContextMessageCodec.class, () -> codec)
                .withBean(OperationContextMessagePropagator.class, () -> propagator)
                .run(context -> {
                    assertThat(context.getBean(OperationContextMessageCodec.class)).isSameAs(codec);
                    assertThat(context.getBean(OperationContextMessagePropagator.class)).isSameAs(propagator);
                });
    }
}
