package com.indigo.synapse.datasource.health;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceHealthRegistryTest {

    @Test
    void shouldUpdateAndQuerySnapshot() {
        DataSourceHealthRegistry registry = new DataSourceHealthRegistry();
        DataSourceHealthSnapshot snapshot = snapshot("slave_1", DataSourceHealthStatus.UP);

        registry.update(snapshot);

        assertThat(registry.find("slave_1")).contains(snapshot);
        assertThat(registry.findAll()).containsExactly(snapshot);
    }

    @Test
    void shouldTreatUpAndDegradedAsAvailable() {
        DataSourceHealthRegistry registry = new DataSourceHealthRegistry();
        registry.update(snapshot("up", DataSourceHealthStatus.UP));
        registry.update(snapshot("degraded", DataSourceHealthStatus.DEGRADED));
        registry.update(snapshot("down", DataSourceHealthStatus.DOWN));
        registry.update(snapshot("disabled", DataSourceHealthStatus.DISABLED));

        assertThat(registry.isAvailable("up")).isTrue();
        assertThat(registry.isAvailable("degraded")).isTrue();
        assertThat(registry.isAvailable("down")).isFalse();
        assertThat(registry.isAvailable("disabled")).isFalse();
        assertThat(registry.isAvailable("missing")).isFalse();
    }

    private static DataSourceHealthSnapshot snapshot(String name, DataSourceHealthStatus status) {
        return new DataSourceHealthSnapshot(name, "slave", status, 0, 1, Instant.now(), null, null);
    }
}
