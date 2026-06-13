package com.indigo.synapse.task.autoconfigure;

import com.indigo.synapse.task.execution.DefaultTaskExecutor;
import com.indigo.synapse.task.execution.NoopTaskFailureHandler;
import com.indigo.synapse.task.execution.TaskExecutor;
import com.indigo.synapse.task.execution.TaskFailureHandler;
import com.indigo.synapse.task.execution.TaskHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Collection;

@AutoConfiguration
@EnableConfigurationProperties(SynapseTaskProperties.class)
@ConditionalOnProperty(prefix = "synapse.task", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SynapseTaskAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TaskFailureHandler synapseTaskFailureHandler() {
        return new NoopTaskFailureHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskExecutor synapseTaskExecutor(Collection<TaskHandler> handlers, TaskFailureHandler failureHandler) {
        return new DefaultTaskExecutor(handlers, failureHandler);
    }
}
