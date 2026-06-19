package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceRole;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import com.indigo.synapse.datasource.testsupport.TestDataSources;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceHealthCheckerStateMachineTest {

    @Test
    void shouldMoveThroughExpectedStateMachineAndPublishBoundaryEvents() {
        SynapseDatasourceProperties properties = new SynapseDatasourceProperties();
        properties.getHealth().setFailureThreshold(2);
        properties.getHealth().setRecoveryThreshold(2);
        DataSourceHealthRegistry registry = new DataSourceHealthRegistry();
        List<Object> events = new ArrayList<>();
        DataSourceHealthChecker checker = new DataSourceHealthChecker(
                properties,
                registry,
                List.of(new PostgreSqlDataSourceValidationStrategy()),
                events::add
        );
        DataSourceDescriptor descriptor = descriptor("master", DataSourceRole.MASTER, true, false);

        DataSourceHealthSnapshot up = checker.check(descriptor, TestDataSources.healthy("PostgreSQL"));
        assertThat(up.status()).isEqualTo(DataSourceHealthStatus.UP);
        assertThat(up.successCount()).isZero();

        DataSourceHealthSnapshot degraded = checker.check(descriptor, TestDataSources.failing("PostgreSQL"));
        assertThat(degraded.status()).isEqualTo(DataSourceHealthStatus.DEGRADED);

        DataSourceHealthSnapshot down = checker.check(descriptor, TestDataSources.failing("PostgreSQL"));
        assertThat(down.status()).isEqualTo(DataSourceHealthStatus.DOWN);
        assertThat(events.stream().filter(DataSourceDownEvent.class::isInstance)).hasSize(1);

        DataSourceHealthSnapshot stillDown = checker.check(descriptor, TestDataSources.failing("PostgreSQL"));
        assertThat(stillDown.status()).isEqualTo(DataSourceHealthStatus.DOWN);
        assertThat(events.stream().filter(DataSourceDownEvent.class::isInstance)).hasSize(1);

        DataSourceHealthSnapshot recovering = checker.check(descriptor, TestDataSources.healthy("PostgreSQL"));
        assertThat(recovering.status()).isEqualTo(DataSourceHealthStatus.RECOVERING);
        assertThat(recovering.successCount()).isOne();

        DataSourceHealthSnapshot recovered = checker.check(descriptor, TestDataSources.healthy("PostgreSQL"));
        assertThat(recovered.status()).isEqualTo(DataSourceHealthStatus.UP);
        assertThat(recovered.successCount()).isZero();
        assertThat(events.stream().filter(DataSourceRecoveredEvent.class::isInstance)).hasSize(1);
    }

    @Test
    void shouldMoveRecoveringBackToDownOnFailure() {
        SynapseDatasourceProperties properties = new SynapseDatasourceProperties();
        properties.getHealth().setFailureThreshold(2);
        properties.getHealth().setRecoveryThreshold(2);
        DataSourceHealthRegistry registry = new DataSourceHealthRegistry();
        registry.update(new DataSourceHealthSnapshot(
                "master",
                "master",
                DataSourceHealthStatus.RECOVERING,
                0,
                1,
                Instant.now(),
                null,
                null
        ));
        DataSourceHealthChecker checker = new DataSourceHealthChecker(
                properties,
                registry,
                List.of(new PostgreSqlDataSourceValidationStrategy())
        );

        DataSourceHealthSnapshot snapshot = checker.check(
                descriptor("master", DataSourceRole.MASTER, true, false),
                TestDataSources.failing("PostgreSQL")
        );

        assertThat(snapshot.status()).isEqualTo(DataSourceHealthStatus.DOWN);
        assertThat(snapshot.successCount()).isZero();
        assertThat(snapshot.failureCount()).isEqualTo(2);
    }

    @Test
    void shouldKeepDisabledSnapshotUnchanged() {
        SynapseDatasourceProperties properties = new SynapseDatasourceProperties();
        DataSourceHealthRegistry registry = new DataSourceHealthRegistry();
        DataSourceHealthSnapshot disabled = new DataSourceHealthSnapshot(
                "master",
                "master",
                DataSourceHealthStatus.DISABLED,
                7,
                7,
                null,
                Instant.now(),
                "disabled"
        );
        registry.update(disabled);
        DataSourceHealthChecker checker = new DataSourceHealthChecker(
                properties,
                registry,
                List.of(new PostgreSqlDataSourceValidationStrategy())
        );

        DataSourceHealthSnapshot snapshot = checker.check(
                descriptor("master", DataSourceRole.MASTER, true, false),
                TestDataSources.healthy("PostgreSQL")
        );

        assertThat(snapshot).isSameAs(disabled);
        assertThat(registry.find("master")).containsSame(disabled);
    }

    @Test
    void shouldRecoverImmediatelyWhenRecoveryThresholdIsOne() {
        SynapseDatasourceProperties properties = new SynapseDatasourceProperties();
        properties.getHealth().setRecoveryThreshold(1);
        DataSourceHealthRegistry registry = new DataSourceHealthRegistry();
        registry.update(new DataSourceHealthSnapshot(
                "master",
                "master",
                DataSourceHealthStatus.DOWN,
                1,
                0,
                null,
                Instant.now(),
                "down"
        ));
        DataSourceHealthChecker checker = new DataSourceHealthChecker(
                properties,
                registry,
                List.of(new PostgreSqlDataSourceValidationStrategy())
        );

        DataSourceHealthSnapshot snapshot = checker.check(
                descriptor("master", DataSourceRole.MASTER, true, false),
                TestDataSources.healthy("PostgreSQL")
        );

        assertThat(snapshot.status()).isEqualTo(DataSourceHealthStatus.UP);
        assertThat(snapshot.successCount()).isZero();
    }

    private static DataSourceDescriptor descriptor(
            String name,
            DataSourceRole role,
            boolean primary,
            boolean readonly
    ) {
        return new DataSourceDescriptor(name, role == DataSourceRole.MASTER ? "master" : "slave", role,
                SynapseDbType.POSTGRESQL, primary, readonly, true, Map.of());
    }
}
