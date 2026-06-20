package com.indigo.synapse.messaging.autoconfigure;

import com.indigo.synapse.messaging.consumer.MessageDispatcher;
import com.indigo.synapse.messaging.consumer.MessageHandlerRegistry;
import com.indigo.synapse.messaging.producer.BestEffortMessagePublisher;
import com.indigo.synapse.messaging.producer.ReliableMessagePublisher;
import com.indigo.synapse.messaging.reliability.OutboxStore;
import com.indigo.synapse.messaging.transport.MessageTransport;
import com.indigo.synapse.messaging.transport.MessageTransportResult;
import com.indigo.synapse.messaging.transport.SpringStreamMessageTransport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.stream.function.StreamBridge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SynapseMessagingAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseMessagingAutoConfiguration.class,
                    SynapseMessagingStreamAutoConfiguration.class,
                    SynapseMessagingPublisherAutoConfiguration.class));

    @Test
    void shouldStartWithoutTransportOrStreamBridge() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(MessageHandlerRegistry.class);
            assertThat(context).hasSingleBean(MessageDispatcher.class);
            assertThat(context).doesNotHaveBean(MessageTransport.class);
            assertThat(context).doesNotHaveBean(BestEffortMessagePublisher.class);
            assertThat(context).doesNotHaveBean(ReliableMessagePublisher.class);
        });
    }

    @Test
    void shouldStartWhenSpringCloudStreamClassesAreMissing() {
        runner.withClassLoader(new FilteredClassLoader("org.springframework.cloud.stream"))
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void shouldPreferUserTransportAndCreateBestEffortPublisher() {
        runner.withBean(StreamBridge.class, () -> mock(StreamBridge.class))
                .withUserConfiguration(CustomTransportConfiguration.class).run(context -> {
            assertThat(context).hasSingleBean(MessageTransport.class);
            assertThat(context.getBean(MessageTransport.class)).isSameAs(context.getBean("customTransport"));
            assertThat(context).hasSingleBean(BestEffortMessagePublisher.class);
            assertThat(context).doesNotHaveBean(SpringStreamMessageTransport.class);
        });
    }

    @Test
    void shouldCreateDefaultStreamTransportAndPublisherWhenStreamBridgeExists() {
        runner.withBean(StreamBridge.class, () -> mock(StreamBridge.class)).run(context -> {
            assertThat(context).hasSingleBean(MessageTransport.class);
            assertThat(context).hasSingleBean(SpringStreamMessageTransport.class);
            assertThat(context).hasSingleBean(BestEffortMessagePublisher.class);
        });
    }

    @Test
    void shouldFailFastWhenReliableEnabledWithoutOutboxStore() {
        runner.withPropertyValues("synapse.messaging.reliable.enabled=true")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure().hasMessageContaining("OutboxStore"));
    }

    @Test
    void shouldCreateReliablePublisherWhenOutboxStoreExists() {
        runner.withPropertyValues("synapse.messaging.reliable.enabled=true")
                .withBean(OutboxStore.class, () -> envelope -> { })
                .run(context -> assertThat(context).hasSingleBean(ReliableMessagePublisher.class));
    }

    @Test
    void shouldDisableAllMessagingBeans() {
        runner.withPropertyValues("synapse.messaging.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(MessageDispatcher.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomTransportConfiguration {
        @Bean
        MessageTransport customTransport() {
            return envelope -> MessageTransportResult.accepted("custom");
        }
    }
}
