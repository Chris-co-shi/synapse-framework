package com.indigo.synapse.webflux.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.webflux.context.OperationContextWebFluxCodec;
import com.indigo.synapse.webflux.exception.SynapseWebFluxExceptionHandler;
import com.indigo.synapse.webflux.filter.SynapseWebFluxContextFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SynapseWebFluxAutoConfigurationTest {

    private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseWebFluxAutoConfiguration.class));

    @Test
    void shouldCreateWebFluxFoundationBeans() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(ObjectMapper.class));
            assertNotNull(context.getBean(OperationContextWebFluxCodec.class));
            assertNotNull(context.getBean(SynapseWebFluxContextFilter.class));
            assertNotNull(context.getBean(SynapseWebFluxExceptionHandler.class));
        });
    }

    @Test
    void shouldNotOverrideCustomObjectMapper() {
        ObjectMapper custom = new ObjectMapper();

        contextRunner.withBean(ObjectMapper.class, () -> custom)
                .run(context -> assertSame(custom, context.getBean(ObjectMapper.class)));
    }

    @Test
    void shouldNotLoadWhenWebFluxStackIsMissing() {
        contextRunner
                .withClassLoader(new FilteredClassLoader("org.springframework.web.server"))
                .run(context -> {
                    assertFalse(context.containsBean("synapseWebFluxContextFilter"));
                    assertFalse(context.containsBean("synapseWebFluxExceptionHandler"));
                });
    }
}
