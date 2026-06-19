package com.indigo.synapse.messaging.autoconfigure;

import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.messaging.consumer.DefaultMessageExceptionClassifier;
import com.indigo.synapse.messaging.consumer.MessageConsumeTemplate;
import com.indigo.synapse.messaging.consumer.MessageExceptionClassifier;
import com.indigo.synapse.messaging.context.OperationContextMessageCodec;
import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageConsumeResult;
import com.indigo.synapse.messaging.core.MessagePublishResult;
import com.indigo.synapse.messaging.idempotent.MessageIdempotencyChecker;
import com.indigo.synapse.messaging.idempotent.NoopMessageIdempotencyChecker;
import com.indigo.synapse.messaging.producer.MessagePublishTemplate;
import com.indigo.synapse.messaging.producer.MessagePublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseMessagingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseMessagingAutoConfiguration.class));

    @Test
    void shouldRegisterDefaultBeansWithoutPublisher() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OperationContextProvider.class);
            assertThat(context).hasSingleBean(OperationContextMessageCodec.class);
            assertThat(context).hasSingleBean(OperationContextMessagePropagator.class);
            assertThat(context).hasSingleBean(MessageExceptionClassifier.class);
            assertThat(context).hasSingleBean(MessageIdempotencyChecker.class);
            assertThat(context).hasSingleBean(MessageConsumeTemplate.class);
            assertThat(context).doesNotHaveBean(MessagePublishTemplate.class);
            assertThat(context.getBean(MessageExceptionClassifier.class)).isInstanceOf(DefaultMessageExceptionClassifier.class);
            assertThat(context.getBean(MessageIdempotencyChecker.class)).isInstanceOf(NoopMessageIdempotencyChecker.class);
        });
    }

    @Test
    void shouldRegisterPublishTemplateWhenPublisherExists() {
        contextRunner.withUserConfiguration(PublisherConfiguration.class)
                .run(context -> assertThat(context).hasSingleBean(MessagePublishTemplate.class));
    }

    @Test
    void shouldRespectUserProvidedBeans() {
        contextRunner.withUserConfiguration(CustomBeanConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(OperationContextProvider.class);
                    assertThat(context).hasSingleBean(MessageExceptionClassifier.class);
                    assertThat(context).hasSingleBean(MessageIdempotencyChecker.class);
                    assertThat(context.getBean(OperationContextProvider.class)).isSameAs(context.getBean("customProvider"));
                    assertThat(context.getBean(MessageExceptionClassifier.class)).isSameAs(context.getBean("customClassifier"));
                    assertThat(context.getBean(MessageIdempotencyChecker.class)).isSameAs(context.getBean("customChecker"));
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class PublisherConfiguration {

        @Bean
        MessagePublisher messagePublisher() {
            return envelope -> MessagePublishResult.success(envelope.messageId(), "broker-1");
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomBeanConfiguration {

        @Bean
        OperationContextProvider customProvider() {
            return new OperationContextProvider() {
                @Override
                public Optional<OperationContext> current() {
                    return Optional.empty();
                }
            };
        }

        @Bean
        MessageExceptionClassifier customClassifier() {
            return throwable -> MessageConsumeResult.discard("custom");
        }

        @Bean
        MessageIdempotencyChecker customChecker() {
            return new MessageIdempotencyChecker() {
                @Override
                public boolean isProcessed(String idempotentKey) {
                    return true;
                }

                @Override
                public void markProcessed(String idempotentKey) {
                }
            };
        }
    }
}
