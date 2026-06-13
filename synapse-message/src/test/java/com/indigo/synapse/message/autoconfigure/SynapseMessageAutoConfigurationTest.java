package com.indigo.synapse.message.autoconfigure;

import com.indigo.synapse.message.publisher.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseMessageAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseMessageAutoConfiguration.class));

    @Test
    void shouldCreatePublisherByDefault() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(DomainEventPublisher.class));
    }

    @Test
    void shouldBackOffWhenApplicationProvidesPublisher() {
        contextRunner
                .withBean("customPublisher", DomainEventPublisher.class, () -> event -> {
                })
                .run(context -> assertThat(context).hasSingleBean(DomainEventPublisher.class));
    }
}
