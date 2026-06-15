package com.indigo.synapse.webmvc.autoconfigure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.webmvc.context.MvcOperationContextFilter;
import com.indigo.synapse.webmvc.openapi.OpenApiProperties;
import com.indigo.synapse.webmvc.openapi.OpenApiVisibilityPolicy;
import com.indigo.synapse.webmvc.exception.SynapseExceptionBridgeFilter;
import com.indigo.synapse.webmvc.trace.MvcTraceFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.env.MockEnvironment;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseWebAutoConfigurationTest {

    private final SynapseWebAutoConfiguration configuration = new SynapseWebAutoConfiguration();
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SynapseWebErrorAutoConfiguration.class,
                    SynapseWebAutoConfiguration.class,
                    SynapseWebMvcAutoConfiguration.class
            ));

    @Test
    void shouldCreateBaseWebFoundationBeans() {
        OpenApiProperties openApiProperties = configuration.synapseOpenApiProperties(new MockEnvironment().withProperty("spring.profiles.active", "dev"));

        assertTrue(openApiProperties.enabled());
    }

    @Test
    void shouldCreateStackTraceBeansWhenStacksAreAvailable() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(ObjectMapper.class));
            assertNotNull(context.getBean(OpenApiProperties.class));
            assertNotNull(context.getBean(SynapseExceptionBridgeFilter.class));
            assertNotNull(context.getBean(MvcTraceFilter.class));
            assertNotNull(context.getBean(MvcOperationContextFilter.class));
        });
    }

    @Test
    void shouldRegisterSynapseExceptionBridgeFilterBeforeSecurityFilters() {
        contextRunner.run(context -> {
            @SuppressWarnings("unchecked")
            FilterRegistrationBean<SynapseExceptionBridgeFilter> registration =
                    context.getBean("synapseExceptionBridgeFilterRegistration", FilterRegistrationBean.class);

            assertEquals("synapseExceptionBridgeFilter", registration.getFilterName());
            assertEquals(SynapseExceptionBridgeFilter.ORDER, registration.getOrder());
            assertTrue(registration.getOrder() < -100);
        });
    }

    @Test
    void shouldUseUnifiedJacksonRules() {
        contextRunner.run(context -> {
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);

            String json = objectMapper.writeValueAsString(new JacksonSample(
                    LocalDateTime.of(2026, 5, 21, 10, 30),
                    SampleStatus.ENABLED,
                    null
            ));
            JacksonSample decoded = objectMapper.readValue(
                    "{\"createdAt\":\"2026-05-21T10:30:00\",\"status\":\"ENABLED\",\"ignored\":\"value\"}",
                    JacksonSample.class
            );

            assertTrue(json.contains("\"createdAt\":\"2026-05-21T10:30:00\""));
            assertTrue(json.contains("\"status\":\"ENABLED\""));
            assertTrue(json.contains("\"remark\":null"));
            assertEquals(SampleStatus.ENABLED, decoded.status());
            assertEquals(LocalDateTime.of(2026, 5, 21, 10, 30), decoded.createdAt());
        });
    }

    @Test
    void shouldNotOverrideCustomObjectMapper() {
        ObjectMapper customObjectMapper = new ObjectMapper();

        contextRunner
                .withBean(ObjectMapper.class, () -> customObjectMapper)
                .run(context -> assertSame(customObjectMapper, context.getBean(ObjectMapper.class)));
    }

    @Test
    void shouldProvideObjectMapperBeforeBootJacksonAutoConfiguration() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SynapseWebAutoConfiguration.class, JacksonAutoConfiguration.class))
                .run(context -> {
                    assertTrue(context.containsBean("synapseObjectMapper"));
                    assertSame(context.getBean("synapseObjectMapper"), context.getBean(ObjectMapper.class));
                });
    }

    @Test
    void shouldNotLoadMvcTraceWhenServletStackIsMissing() {
        contextRunner
                .withClassLoader(new FilteredClassLoader("jakarta.servlet"))
                .run(context -> {
                    assertNotNull(context.getBean(OpenApiProperties.class));
                    assertFalse(context.containsBean("synapseExceptionBridgeFilter"));
                    assertFalse(context.containsBean("synapseExceptionBridgeFilterRegistration"));
                    assertFalse(context.containsBean("synapseMvcTraceFilter"));
                    assertFalse(context.containsBean("synapseMvcOperationContextFilter"));
                });
    }

    @Test
    void shouldExposeOpenApiVisibilityPolicy() {
        assertTrue(OpenApiVisibilityPolicy.visible(
                configuration.synapseOpenApiProperties(new MockEnvironment().withProperty("spring.profiles.active", "dev")),
                "dev"));
    }

    private record JacksonSample(LocalDateTime createdAt, SampleStatus status, String remark) {

        @JsonCreator
        private JacksonSample(
                @JsonProperty("createdAt") LocalDateTime createdAt,
                @JsonProperty("status") SampleStatus status,
                @JsonProperty("remark") String remark
        ) {
            this.createdAt = createdAt;
            this.status = status;
            this.remark = remark;
        }
    }

    private enum SampleStatus {
        ENABLED
    }
}
