package com.indigo.synapse.message.autoconfigure;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.indigo.synapse.message.execution.OutboxAppender;
import com.indigo.synapse.message.port.CompensationRepository;
import com.indigo.synapse.message.port.DeadLetterRepository;
import com.indigo.synapse.message.port.ReliableMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseReliableMessageAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    MybatisPlusAutoConfiguration.class,
                    SynapseReliableMessageAutoConfiguration.class
            ));

    private final ApplicationContextRunner noInfrastructureContextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseReliableMessageAutoConfiguration.class));

    @Test
    void shouldNotRegisterReliableBeansByDefault() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(OutboxAppender.class));
    }

    @Test
    void shouldNotRegisterReliableBeansWhenInfrastructureMissing() {
        noInfrastructureContextRunner
                .withPropertyValues("synapse.message.reliable.enabled=true")
                .run(context -> assertThat(context).doesNotHaveBean(OutboxAppender.class));
    }

    @Test
    void shouldRegisterReliableBeansWhenEnabledAndInfrastructureExists() {
        contextRunner
                .withPropertyValues(
                        "synapse.message.reliable.enabled=true",
                        "synapse.message.reliable.scheduler.enabled=false",
                        "spring.datasource.url=jdbc:h2:mem:synapse_message_auto_config;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                        "spring.datasource.username=sa",
                        "spring.datasource.password="
                )
                .run(context -> assertThat(context).hasSingleBean(OutboxAppender.class));
    }

    @Test
    void shouldBackOffWhenApplicationProvidesRepository() {
        ReliableMessageRepository repository = new NoopReliableMessageRepository();

        contextRunner
                .withPropertyValues(
                        "synapse.message.reliable.enabled=true",
                        "synapse.message.reliable.scheduler.enabled=false",
                        "spring.datasource.url=jdbc:h2:mem:synapse_message_custom_repository;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                        "spring.datasource.username=sa",
                        "spring.datasource.password="
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
                .run(context -> assertThat(context.getBean(ReliableMessageRepository.class)).isSameAs(repository));
    }
}
