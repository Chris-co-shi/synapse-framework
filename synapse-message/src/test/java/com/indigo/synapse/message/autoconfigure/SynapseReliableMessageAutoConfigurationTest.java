package com.indigo.synapse.message.autoconfigure;

import com.indigo.synapse.message.execution.CompensationService;
import com.indigo.synapse.message.execution.OutboxAppender;
import com.indigo.synapse.message.port.CompensationRepository;
import com.indigo.synapse.message.port.DeadLetterRepository;
import com.indigo.synapse.message.port.ReliableMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseReliableMessageAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseReliableMessageAutoConfiguration.class));

    @Test
    void shouldNotRegisterReliableBeansByDefault() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(OutboxAppender.class));
    }

    @Test
    void shouldNotRegisterPortBackedBeansWhenRepositoriesMissing() {
        contextRunner
                .withPropertyValues("synapse.message.reliable.enabled=true")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ReliableMessageRepository.class);
                    assertThat(context).doesNotHaveBean(DeadLetterRepository.class);
                    assertThat(context).doesNotHaveBean(CompensationRepository.class);
                    assertThat(context).doesNotHaveBean(OutboxAppender.class);
                    assertThat(context).doesNotHaveBean(CompensationService.class);
                });
    }

    @Test
    void shouldRegisterReliableServicesWhenApplicationProvidesPorts() {
        ReliableMessageRepository repository = new NoopReliableMessageRepository();

        contextRunner
                .withPropertyValues(
                        "synapse.message.reliable.enabled=true",
                        "synapse.message.reliable.scheduler.enabled=false"
                )
                .withBean(ReliableMessageRepository.class, () -> repository)
                .withBean(DeadLetterRepository.class, () -> (message, reason) -> {
                })
                .withBean(CompensationRepository.class, () -> new CompensationRepository() {
                    @Override
                    public void save(String compensationId, String messageId, String handlerName, String payload, java.time.Instant now) {
                    }

                    @Override
                    public void markSucceeded(String compensationId, java.time.Instant now) {
                    }

                    @Override
                    public void markFailed(String compensationId, String errorMessage, java.time.Instant now) {
                    }
                })
                .run(context -> {
                    assertThat(context.getBean(ReliableMessageRepository.class)).isSameAs(repository);
                    assertThat(context).hasSingleBean(OutboxAppender.class);
                    assertThat(context).hasSingleBean(CompensationService.class);
                });
    }
}
