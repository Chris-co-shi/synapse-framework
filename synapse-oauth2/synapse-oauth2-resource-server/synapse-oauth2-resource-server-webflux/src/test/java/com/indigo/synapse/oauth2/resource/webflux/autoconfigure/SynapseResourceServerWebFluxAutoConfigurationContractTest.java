package com.indigo.synapse.oauth2.resource.webflux.autoconfigure;

import com.indigo.synapse.oauth2.resource.webflux.jwt.SynapseReactiveJwtAuthenticationConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/** Reactive Resource Server 的应用类型和关闭开关契约。 */
class SynapseResourceServerWebFluxAutoConfigurationContractTest {
    private final ReactiveWebApplicationContextRunner reactiveRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseResourceServerWebFluxAutoConfiguration.class));

    @Test
    void shouldCreateNoResourceServerBeansWhenDisabled() {
        reactiveRunner.withPropertyValues("synapse.security.resource-server.enabled=false")
                .run(context -> assertThat(context).hasNotFailed()
                        .doesNotHaveBean(SynapseReactiveJwtAuthenticationConverter.class));
    }

    @Test
    void shouldNotLoadInServletApplication() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SynapseResourceServerWebFluxAutoConfiguration.class))
                .run(context -> assertThat(context).hasNotFailed()
                        .doesNotHaveBean(SynapseReactiveJwtAuthenticationConverter.class));
    }
}
