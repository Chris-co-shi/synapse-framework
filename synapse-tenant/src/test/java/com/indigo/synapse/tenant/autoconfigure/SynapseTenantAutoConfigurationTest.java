package com.indigo.synapse.tenant.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SynapseTenantAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseTenantAutoConfiguration.class));

    @Test
    void shouldLoadTenantAutoConfiguration() {
        contextRunner.run(context -> assertNotNull(context.getBean(SynapseTenantAutoConfiguration.class)));
    }
}
