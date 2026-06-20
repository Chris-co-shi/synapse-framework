package com.indigo.synapse.oauth2.resource.webmvc.autoconfigure;

import com.indigo.synapse.oauth2.resource.webmvc.jwt.SynapseJwtAuthenticationConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/** Servlet Resource Server 的应用类型和关闭开关契约。 */
class SynapseResourceServerWebMvcAutoConfigurationContractTest {
    private final WebApplicationContextRunner servletRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseResourceServerWebMvcAutoConfiguration.class));

    @Test
    void shouldCreateNoResourceServerBeansWhenDisabled() {
        servletRunner.withPropertyValues("synapse.security.resource-server.enabled=false")
                .run(context -> assertThat(context).hasNotFailed()
                        .doesNotHaveBean(SynapseJwtAuthenticationConverter.class));
    }

    @Test
    void shouldNotLoadInReactiveApplication() {
        new ReactiveWebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SynapseResourceServerWebMvcAutoConfiguration.class))
                .run(context -> assertThat(context).hasNotFailed()
                        .doesNotHaveBean(SynapseJwtAuthenticationConverter.class));
    }
}
