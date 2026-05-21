package com.indigo.synapse.web.autoconfigure;

import com.indigo.synapse.web.openapi.OpenApiProperties;
import com.indigo.synapse.web.openapi.OpenApiVisibilityPolicy;
import com.indigo.synapse.web.trace.MvcTraceFilter;
import com.indigo.synapse.web.trace.WebFluxTraceWebFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseWebAutoConfigurationTest {

    private final SynapseWebAutoConfiguration configuration = new SynapseWebAutoConfiguration();
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SynapseWebAutoConfiguration.class,
                    SynapseWebMvcAutoConfiguration.class,
                    SynapseWebFluxAutoConfiguration.class
            ));

    @Test
    void shouldCreateBaseWebFoundationBeans() {
        OpenApiProperties openApiProperties = configuration.synapseOpenApiProperties(new MockEnvironment().withProperty("spring.profiles.active", "dev"));

        assertTrue(openApiProperties.enabled());
    }

    @Test
    void shouldCreateStackTraceBeansWhenStacksAreAvailable() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(OpenApiProperties.class));
            assertNotNull(context.getBean(MvcTraceFilter.class));
            assertNotNull(context.getBean(WebFluxTraceWebFilter.class));
        });
    }

    @Test
    void shouldNotLoadMvcTraceWhenServletStackIsMissing() {
        contextRunner
                .withClassLoader(new FilteredClassLoader("jakarta.servlet"))
                .run(context -> {
                    assertNotNull(context.getBean(OpenApiProperties.class));
                    assertFalse(context.containsBean("synapseMvcTraceFilter"));
                });
    }

    @Test
    void shouldNotLoadWebFluxTraceWhenReactiveStackIsMissing() {
        contextRunner
                .withClassLoader(new FilteredClassLoader("org.springframework.web.reactive", "org.springframework.web.server"))
                .run(context -> {
                    assertNotNull(context.getBean(OpenApiProperties.class));
                    assertFalse(context.containsBean("synapseWebFluxTraceWebFilter"));
                });
    }

    @Test
    void shouldExposeOpenApiVisibilityPolicy() {
        assertTrue(OpenApiVisibilityPolicy.visible(
                configuration.synapseOpenApiProperties(new MockEnvironment().withProperty("spring.profiles.active", "dev")),
                "dev"));
    }
}
