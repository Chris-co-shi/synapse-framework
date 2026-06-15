package com.indigo.synapse.time.autoconfigure;

import com.indigo.synapse.time.TimeRangeConverter;
import com.indigo.synapse.time.TimeZoneResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseTimeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseTimeAutoConfiguration.class));

    @Test
    void shouldAutoConfigureDefaultBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TimeRangeConverter.class);
            assertThat(context).hasSingleBean(TimeZoneResolver.class);
            assertThat(context.getBean(TimeZoneResolver.class).resolve()).contains(ZoneId.of("UTC"));
        });
    }

    @Test
    void shouldUseConfiguredDefaultZone() {
        contextRunner
                .withPropertyValues("synapse.time.default-zone=Asia/Shanghai")
                .run(context -> assertThat(context.getBean(TimeZoneResolver.class).resolve())
                        .contains(ZoneId.of("Asia/Shanghai")));
    }

    @Test
    void shouldNotOverrideCustomResolver() {
        TimeZoneResolver resolver = () -> Optional.of(ZoneId.of("Europe/Paris"));

        contextRunner
                .withBean(TimeZoneResolver.class, () -> resolver)
                .run(context -> assertThat(context.getBean(TimeZoneResolver.class)).isSameAs(resolver));
    }
}
