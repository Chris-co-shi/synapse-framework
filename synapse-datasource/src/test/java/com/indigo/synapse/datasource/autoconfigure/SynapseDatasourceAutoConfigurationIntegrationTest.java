package com.indigo.synapse.datasource.autoconfigure;

import com.indigo.synapse.datasource.dynamic.DatasourceInventory;
import com.indigo.synapse.datasource.lifecycle.ScheduledDataSourceHealthMonitor;
import com.indigo.synapse.datasource.router.DataSourceRouter;
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
            .withBean(DatasourceInventory.class, EmptyInventory::new)
            .withPropertyValues("synapse.datasource.safety.fail-on-master-unavailable=false");

    @Test
    void shouldCreateRouterOnlyWhenEnabled() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(DataSourceRouter.class));
        contextRunner.withPropertyValues("synapse.datasource.router.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(DataSourceRouter.class));
    }

    @Test
    void shouldDisableHealthInfrastructureWhenConfigured() {
        contextRunner.withPropertyValues("synapse.datasource.health.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TaskScheduler.class);
                    assertThat(context).doesNotHaveBean(ScheduledDataSourceHealthMonitor.class);
                });
    }

    static final class EmptyInventory implements DatasourceInventory {
        @Override
        public Map<String, DataSource> refreshInventory() {
            return Map.of();
        }

        @Override
        public Map<String, DataSource> getDataSources() {
            return Map.of();
        }

        @Override
        public Optional<String> getPrimaryName() {
            return Optional.empty();
        }

        @Override
        public boolean isStrict() {
            return true;
        }

        @Override
        public Optional<String> getJdbcUrl(String name) {
            return Optional.empty();
        }
    }
}
