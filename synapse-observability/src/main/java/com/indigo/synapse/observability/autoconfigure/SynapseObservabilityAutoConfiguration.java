package com.indigo.synapse.observability.autoconfigure;

import com.indigo.synapse.observability.DefaultSynapseObservationOperations;
import com.indigo.synapse.observability.SynapseObservationMdcHandler;
import com.indigo.synapse.observability.SynapseObservationOperations;
import com.indigo.synapse.observability.TraceContextProvider;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Synapse Micrometer Observation 基础自动配置，不创建 registry 或 exporter。 */
@AutoConfiguration
@ConditionalOnClass(ObservationRegistry.class)
public class SynapseObservabilityAutoConfiguration {

    /** 缺少消费方 registry 时使用 NOOP registry，保证可选观测不阻止启动。 */
    @Bean
    @ConditionalOnMissingBean
    public SynapseObservationOperations synapseObservationOperations(
            ObjectProvider<ObservationRegistry> registryProvider) {
        return new DefaultSynapseObservationOperations(
                registryProvider.getIfAvailable(() -> ObservationRegistry.NOOP));
    }

    /** 存在 trace provider 时注册 MDC handler；不创建 tracer。 */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(TraceContextProvider.class)
    public SynapseObservationMdcHandler synapseObservationMdcHandler(
            TraceContextProvider traceContextProvider,
            ObjectProvider<ObservationRegistry> registryProvider) {
        SynapseObservationMdcHandler handler = new SynapseObservationMdcHandler(traceContextProvider);
        ObservationRegistry registry = registryProvider.getIfAvailable();
        if (registry != null) {
            registry.observationConfig().observationHandler(handler);
        }
        return handler;
    }
}
