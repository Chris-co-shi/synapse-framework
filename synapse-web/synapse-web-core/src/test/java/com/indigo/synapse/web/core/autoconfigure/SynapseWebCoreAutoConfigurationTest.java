package com.indigo.synapse.web.core.autoconfigure;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseWebCoreAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SynapseWebCoreAutoConfiguration.class,
                    JacksonAutoConfiguration.class
            ));

    @Test
    void shouldCustomizeBootObjectMapperWithoutProvidingAnotherMapper() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ObjectMapper.class);
            assertThat(context).doesNotHaveBean("synapseObjectMapper");

            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
            String json = objectMapper.writeValueAsString(
                    new TimePayload(LocalDateTime.of(2026, 6, 19, 12, 30), null)
            );

            assertThat(json).contains("\"createdAt\":\"2026-06-19T12:30:00\"");
            assertThat(json).contains("\"remark\":null");
        });
    }

    @Test
    void shouldPreserveSpringJacksonProperties() {
        contextRunner
                .withPropertyValues("spring.jackson.default-property-inclusion=non_null")
                .run(context -> {
                    String json = context.getBean(ObjectMapper.class)
                            .writeValueAsString(new TimePayload(
                                    LocalDateTime.of(2026, 6, 19, 12, 30),
                                    null
                            ));

                    assertThat(json).doesNotContain("remark");
                });
    }

    @Test
    void shouldBackOffForUserObjectMapper() {
        ObjectMapper userMapper = new ObjectMapper();

        contextRunner
                .withBean(ObjectMapper.class, () -> userMapper)
                .run(context -> assertThat(context.getBean(ObjectMapper.class))
                        .isSameAs(userMapper));
    }

    @Test
    void shouldComposeWithUserModuleAndCustomizer() {
        contextRunner
                .withBean(Module.class, SynapseWebCoreAutoConfigurationTest::customModule)
                .withBean(
                        "userJacksonCustomizer",
                        Jackson2ObjectMapperBuilderCustomizer.class,
                        () -> builder -> builder.propertyNamingStrategy(
                                PropertyNamingStrategies.SNAKE_CASE
                        )
                )
                .run(context -> {
                    String json = context.getBean(ObjectMapper.class)
                            .writeValueAsString(new CustomPayload(
                                    new CustomValue("value")
                            ));

                    assertThat(json).isEqualTo(
                            "{\"custom_value\":\"module:value\"}"
                    );
                });
    }

    private static Module customModule() {
        SimpleModule module = new SimpleModule("user-module");
        module.addSerializer(CustomValue.class, new JsonSerializer<>() {
            @Override
            public void serialize(
                    CustomValue value,
                    JsonGenerator generator,
                    SerializerProvider serializers
            ) throws IOException {
                generator.writeString("module:" + value.value());
            }
        });
        return module;
    }

    private record TimePayload(LocalDateTime createdAt, String remark) {
    }

    private record CustomPayload(CustomValue customValue) {
    }

    private record CustomValue(String value) {
    }
}
