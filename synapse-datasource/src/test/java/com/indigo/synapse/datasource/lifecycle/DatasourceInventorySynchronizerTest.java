package com.indigo.synapse.datasource.lifecycle;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorResolver;
import com.indigo.synapse.datasource.detection.CompositeDbTypeDetector;
import com.indigo.synapse.datasource.detection.ConnectionMetadataDbTypeDetector;
import com.indigo.synapse.datasource.detection.JdbcUrlDbTypeDetector;
import com.indigo.synapse.datasource.dynamic.DatasourceInventory;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.health.DataSourceHealthSnapshot;
import com.indigo.synapse.datasource.health.DataSourceHealthStatus;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import com.indigo.synapse.datasource.testsupport.TestDataSources;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DatasourceInventorySynchronizerTest {

    @Test
    void shouldSynchronizeAddedAndRemovedDatasourcesIdempotently() {
        MutableInventory inventory = new MutableInventory();
        inventory.put("master", TestDataSources.healthy("PostgreSQL"));
        inventory.put("slave_1", TestDataSources.healthyReadonly("PostgreSQL"));
        DataSourceDescriptorRegistry descriptorRegistry = new DataSourceDescriptorRegistry();
        DataSourceHealthRegistry healthRegistry = new DataSourceHealthRegistry();
        DatasourceInventorySynchronizer synchronizer = synchronizer(inventory, descriptorRegistry, healthRegistry);

        DatasourceInventorySnapshot first = synchronizer.synchronize();
        assertThat(first.dataSources()).containsOnlyKeys("master", "slave_1");
        assertThat(descriptorRegistry.find("master")).isPresent();
        assertThat(descriptorRegistry.find("slave_1")).isPresent();
        assertThat(healthRegistry.find("slave_1")).map(DataSourceHealthSnapshot::status)
                .contains(DataSourceHealthStatus.UNKNOWN);

        healthRegistry.update(new DataSourceHealthSnapshot(
                "slave_1",
                "slave",
                DataSourceHealthStatus.UP,
                0,
                0,
                Instant.now(),
                null,
                null
        ));
        inventory.put("slave_2", TestDataSources.healthyReadonly("PostgreSQL"));
        DatasourceInventorySnapshot second = synchronizer.synchronize();
        assertThat(second.dataSources()).containsOnlyKeys("master", "slave_1", "slave_2");
        assertThat(healthRegistry.find("slave_1")).map(DataSourceHealthSnapshot::status)
                .contains(DataSourceHealthStatus.UP);
        assertThat(healthRegistry.find("slave_2")).map(DataSourceHealthSnapshot::status)
                .contains(DataSourceHealthStatus.UNKNOWN);

        inventory.remove("slave_1");
        DatasourceInventorySnapshot third = synchronizer.synchronize();
        assertThat(third.dataSources()).containsOnlyKeys("master", "slave_2");
        assertThat(descriptorRegistry.find("slave_1")).isEmpty();
        assertThat(healthRegistry.find("slave_1")).isEmpty();

        DatasourceInventorySnapshot fourth = synchronizer.synchronize();
        assertThat(fourth.dataSources()).containsOnlyKeys("master", "slave_2");
        assertThat(descriptorRegistry.findAll()).hasSize(2);
        assertThat(healthRegistry.findAll()).hasSize(2);
    }

    private static DatasourceInventorySynchronizer synchronizer(
            DatasourceInventory inventory,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceHealthRegistry healthRegistry
    ) {
        SynapseDatasourceProperties properties = new SynapseDatasourceProperties();
        CompositeDbTypeDetector detector = new CompositeDbTypeDetector(
                properties,
                new JdbcUrlDbTypeDetector(),
                new ConnectionMetadataDbTypeDetector()
        );
        return new DatasourceInventorySynchronizer(
                inventory,
                new DataSourceDescriptorResolver(properties, detector),
                descriptorRegistry,
                healthRegistry
        );
    }

    private static final class MutableInventory implements DatasourceInventory {

        private final Map<String, DataSource> dataSources = new LinkedHashMap<>();

        void put(String name, DataSource dataSource) {
            dataSources.put(name, dataSource);
        }

        void remove(String name) {
            dataSources.remove(name);
        }

        @Override
        public Map<String, DataSource> refreshInventory() {
            return Map.copyOf(dataSources);
        }

        @Override
        public Map<String, DataSource> getDataSources() {
            return Map.copyOf(dataSources);
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
