package com.indigo.synapse.datasource.autoconfigure;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.dynamic.DatasourceInventory;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.health.DataSourceHealthStatus;
import com.indigo.synapse.datasource.lifecycle.ScheduledDataSourceHealthMonitor;
import com.indigo.synapse.datasource.loadbalance.FirstAvailableLoadBalanceSelector;
import com.indigo.synapse.datasource.loadbalance.LoadBalanceSelector;
import com.indigo.synapse.datasource.router.DataSourceRouter;
import com.indigo.synapse.datasource.testsupport.TestDataSources;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.TaskScheduler;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseDatasourceAutoConfigurationIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseDatasourceAutoConfiguration.class))
            .withBean(DatasourceInventory.class, TestDatasourceInventory::new)
            .withPropertyValues(
                    "synapse.datasource.health.initial-delay=1h",
                    "synapse.datasource.safety.fail-on-master-unavailable=true"
            );

    @Test
    void shouldRegisterDescriptorsAndHealthSnapshotsFromInventory() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DataSourceDescriptorRegistry.class);
            assertThat(context).hasSingleBean(DataSourceHealthRegistry.class);
            assertThat(context).doesNotHaveBean(DataSourceRouter.class);

            DataSourceDescriptorRegistry descriptors = context.getBean(DataSourceDescriptorRegistry.class);
            DataSourceHealthRegistry health = context.getBean(DataSourceHealthRegistry.class);

            assertThat(descriptors.find("master")).isPresent();
            assertThat(descriptors.find("slave_1")).isPresent();
            assertThat(descriptors.findPrimary()).map(descriptor -> descriptor.name()).contains("master");
            assertThat(health.find("master")).map(snapshot -> snapshot.status()).contains(DataSourceHealthStatus.UP);
            assertThat(health.find("slave_1")).map(snapshot -> snapshot.status()).contains(DataSourceHealthStatus.UP);
        });
    }

    @Test
    void shouldCreateRouterOnlyWhenEnabled() {
        contextRunner
                .withPropertyValues("synapse.datasource.router.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(DataSourceRouter.class));
    }

    @Test
    void shouldDisableHealthSchedulerAndChecksWhenHealthDisabled() {
        contextRunner
                .withPropertyValues("synapse.datasource.health.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TaskScheduler.class);
                    assertThat(context).doesNotHaveBean(ScheduledDataSourceHealthMonitor.class);
                    DataSourceHealthRegistry health = context.getBean(DataSourceHealthRegistry.class);
                    assertThat(health.find("master")).map(snapshot -> snapshot.status())
                            .contains(DataSourceHealthStatus.UNKNOWN);
                });
    }

    @Test
    void shouldUseFirstAvailableSelectorWhenLoadBalanceDisabled() {
        contextRunner
                .withPropertyValues("synapse.datasource.load-balance.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(LoadBalanceSelector.class);
                    assertThat(context.getBean(LoadBalanceSelector.class))
                            .isInstanceOf(FirstAvailableLoadBalanceSelector.class);
                });
    }

    static class TestDatasourceInventory implements DatasourceInventory {

        private final Map<String, DataSource> dataSources = Map.of(
                "master", TestDataSources.healthy("PostgreSQL"),
                "slave_1", TestDataSources.healthy("PostgreSQL")
        );

        @Override
        public Map<String, DataSource> refreshInventory() {
            return dataSources;
        }

        @Override
        public Map<String, DataSource> getDataSources() {
            return dataSources;
        }

        @Override
        public Optional<String> getPrimaryName() {
            return Optional.of("master");
        }

        @Override
        public boolean isStrict() {
            return true;
        }

        @Override
        public Optional<String> getJdbcUrl(String name) {
            return Optional.of("jdbc:postgresql://localhost/" + name);
        }
    }
}
