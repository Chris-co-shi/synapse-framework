package com.indigo.synapse.config.autoconfigure;

import com.indigo.synapse.config.ConfigClient;
import com.indigo.synapse.config.ConfigParser;
import com.indigo.synapse.config.ConfigResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseConfigAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseConfigAutoConfiguration.class));

    @Test
    void shouldAutoConfigureDefaultBeans() {
        contextRunner
                .withPropertyValues("synapse.config.values.feature=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(ConfigClient.class);
                    assertThat(context).hasSingleBean(ConfigParser.class);
                    assertThat(context).hasSingleBean(ConfigResolver.class);
                    assertThat(context.getBean(ConfigResolver.class).resolve("feature", Boolean.class)).contains(true);
                });
    }

    @Test
    void shouldNotOverrideCustomClient() {
        ConfigClient client = key -> Optional.of("custom");

        contextRunner
                .withBean(ConfigClient.class, () -> client)
                .run(context -> assertThat(context.getBean(ConfigClient.class)).isSameAs(client));
    }
}
