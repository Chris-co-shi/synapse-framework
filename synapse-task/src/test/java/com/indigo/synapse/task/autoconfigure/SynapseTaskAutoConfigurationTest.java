package com.indigo.synapse.task.autoconfigure;

import com.indigo.synapse.task.execution.TaskExecutor;
import com.indigo.synapse.task.execution.TaskFailureHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseTaskAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseTaskAutoConfiguration.class));

    @Test
    void shouldCreateDefaultTaskBeans() {
        contextRunner.run(context -> assertThat(context)
                .hasSingleBean(TaskExecutor.class)
                .hasSingleBean(TaskFailureHandler.class)
                .hasSingleBean(SynapseTaskProperties.class));
    }

    @Test
    void shouldBackOffWhenDisabled() {
        contextRunner
                .withPropertyValues("synapse.task.enabled=false")
                .run(context -> assertThat(context)
                        .doesNotHaveBean(TaskExecutor.class)
                        .doesNotHaveBean(TaskFailureHandler.class));
    }
}
