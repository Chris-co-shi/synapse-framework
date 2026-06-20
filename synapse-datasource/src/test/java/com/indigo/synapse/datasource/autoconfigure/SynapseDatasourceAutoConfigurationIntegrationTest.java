package com.indigo.synapse.datasource.autoconfigure;

import com.indigo.synapse.datasource.dynamic.DatasourceInventory;
import com.indigo.synapse.datasource.lifecycle.ScheduledDataSourceHealthMonitor;
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
    void shouldCreateRouterOnlyWhenEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(DataSourceRouter.class);
        });

        contextRunner.withPropertyValues("synapse.datasource.router.enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(DataSourceRouter.class);
                });
    }

    @Test
    void shouldDisableHealthInfrastructureWhenConfigured() {
        contextRunner.withPropertyValues("synapse.datasource.health.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(TaskScheduler.class);
                    assertThat(context).doesNotHaveBean(ScheduledDataSourceHealthMonitor.class);
                });
    }

    static final class TestDatasourceInventory implements DatasourceInventory {

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
            return Optional.of("jdbc:postgresql:test:" + name);
        }
    }
}
