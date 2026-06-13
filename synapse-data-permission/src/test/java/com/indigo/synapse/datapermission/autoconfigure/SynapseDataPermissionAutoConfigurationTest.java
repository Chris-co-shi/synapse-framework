package com.indigo.synapse.datapermission.autoconfigure;

import com.indigo.synapse.datapermission.model.DataPermissionPolicy;
import com.indigo.synapse.datapermission.model.DataPermissionScope;
import com.indigo.synapse.datapermission.resolver.DataPermissionResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseDataPermissionAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseDataPermissionAutoConfiguration.class));

    @Test
    void shouldCreateDefaultResolver() {
        contextRunner.run(context -> {
            DataPermissionResolver resolver = context.getBean(DataPermissionResolver.class);
            assertThat(resolver.resolve("1", "user:list").scope()).isEqualTo(DataPermissionScope.ALL);
        });
    }

    @Test
    void shouldRejectInvalidCustomPolicyInModel() {
        assertThat(new DataPermissionPolicy(DataPermissionScope.CUSTOM_DEPT, Set.of("10")).departmentIds()).containsExactly("10");
    }
}
