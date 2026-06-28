package com.indigo.synapse.resilience.autoconfigure;

import com.indigo.synapse.observability.SynapseObservationOperations;
import com.indigo.synapse.resilience.DefaultResilienceExceptionClassifier;
import com.indigo.synapse.resilience.DefaultResilienceOperations;
import com.indigo.synapse.resilience.ResilienceExceptionClassifier;
import com.indigo.synapse.resilience.ResilienceOperations;
import io.github.resilience4j.retry.Retry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Resilience4j 韧性基础自动配置，不创建 fallback 或业务策略。 */
@AutoConfiguration
@ConditionalOnClass(Retry.class)
public class SynapseResilienceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ResilienceExceptionClassifier resilienceExceptionClassifier() {
        return new DefaultResilienceExceptionClassifier();
    }

    /** 使用 Java 21 虚拟线程执行需要超时控制的阻塞任务。 */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(name = "synapseResilienceExecutor")
    public ExecutorService synapseResilienceExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ResilienceOperations resilienceOperations(
            ResilienceExceptionClassifier classifier,
            SynapseObservationOperations observations,
            @Qualifier("synapseResilienceExecutor") ExecutorService synapseResilienceExecutor) {
        return new DefaultResilienceOperations(classifier, observations, synapseResilienceExecutor);
    }
}
