package com.indigo.synapse.datasource.safety;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.descriptor.DataSourceRole;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.health.DataSourceHealthSnapshot;
import com.indigo.synapse.datasource.health.DataSourceHealthStatus;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import com.indigo.synapse.datasource.testsupport.TestDataSources;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceSafetyCheckerTest {

    @Test
    void shouldValidatePrimaryDatasourceName() {
        DataSourceSafetyChecker checker = new DataSourceSafetyChecker(new SynapseDatasourceProperties());

        assertThat(checker.checkPrimary("master").safe()).isTrue();
        assertThat(checker.checkPrimary("primary").safe()).isFalse();
    }

    @Test
    void shouldValidateStrictMode() {
        DataSourceSafetyChecker checker = new DataSourceSafetyChecker(new SynapseDatasourceProperties());

        assertThat(checker.checkStrict(true).safe()).isTrue();
        assertThat(checker.checkStrict(false).safe()).isFalse();
    }

    @Test
    void shouldReportMissingAndMismatchedPrimary() {
        DataSourceSafetyChecker checker = new DataSourceSafetyChecker(new SynapseDatasourceProperties());

        assertThat(code(checker.checkPrimary(null))).isEqualTo(DataSourceSafetyViolationCode.PRIMARY_MISSING.name());
        assertThat(code(checker.checkPrimary("primary"))).isEqualTo(DataSourceSafetyViolationCode.PRIMARY_NAME_MISMATCH.name());
    }

    @Test
    void shouldReportMasterDatasourceMissingFromInventory() {
        DataSourceSafetyChecker checker = new DataSourceSafetyChecker(new SynapseDatasourceProperties());
        DataSourceDescriptorRegistry registry = new DataSourceDescriptorRegistry();
        registry.register(descriptor("master", DataSourceRole.MASTER, SynapseDbType.POSTGRESQL, true, false));

        DataSourceSafetyReport report = checker.checkPrimaryDescriptor(Optional.of("master"), Map.of(), registry);

        assertThat(code(report)).isEqualTo(DataSourceSafetyViolationCode.MASTER_DATASOURCE_MISSING.name());
    }

    @Test
    void shouldReportMultiplePrimaryDescriptors() {
        DataSourceSafetyChecker checker = new DataSourceSafetyChecker(new SynapseDatasourceProperties());
        DataSourceDescriptorRegistry registry = new DataSourceDescriptorRegistry();
        registry.register(descriptor("master", DataSourceRole.MASTER, SynapseDbType.POSTGRESQL, true, false));
        registry.register(descriptor("master_2", DataSourceRole.MASTER, SynapseDbType.POSTGRESQL, true, false));

        DataSourceSafetyReport report = checker.checkPrimaryDescriptor(
                Optional.of("master"),
                Map.of("master", TestDataSources.healthy("PostgreSQL")),
                registry
        );

        assertThat(code(report)).isEqualTo(DataSourceSafetyViolationCode.MULTIPLE_PRIMARY_DATASOURCES.name());
    }

    @Test
    void shouldReportPrimaryRoleMismatch() {
        DataSourceSafetyChecker checker = new DataSourceSafetyChecker(new SynapseDatasourceProperties());
        DataSourceDescriptorRegistry registry = new DataSourceDescriptorRegistry();
        registry.register(descriptor("master", DataSourceRole.SLAVE, SynapseDbType.POSTGRESQL, true, true));

        DataSourceSafetyReport report = checker.checkPrimaryDescriptor(
                Optional.of("master"),
                Map.of("master", TestDataSources.healthy("PostgreSQL")),
                registry
        );

        assertThat(code(report)).isEqualTo(DataSourceSafetyViolationCode.PRIMARY_ROLE_MISMATCH.name());
    }

    @Test
    void shouldReportUnavailableMasterAndUnknownDatabaseType() {
        DataSourceSafetyChecker checker = new DataSourceSafetyChecker(new SynapseDatasourceProperties());
        DataSourceHealthRegistry healthRegistry = new DataSourceHealthRegistry();
        DataSourceDescriptor master = descriptor("master", DataSourceRole.MASTER, SynapseDbType.POSTGRESQL, true, false);

        assertThat(code(checker.checkMasterAvailable(master, healthRegistry)))
                .isEqualTo(DataSourceSafetyViolationCode.MASTER_UNAVAILABLE.name());
        assertThat(code(checker.checkKnownDatabaseTypes(List.of(
                descriptor("unknown", DataSourceRole.UNKNOWN, SynapseDbType.UNKNOWN, false, false)
        )))).isEqualTo(DataSourceSafetyViolationCode.UNKNOWN_DATABASE_TYPE.name());
    }

    @Test
    void shouldReportReadonlyRoleMismatch() {
        DataSourceSafetyChecker checker = new DataSourceSafetyChecker(new SynapseDatasourceProperties());
        DataSourceHealthRegistry healthRegistry = new DataSourceHealthRegistry();
        healthRegistry.update(new DataSourceHealthSnapshot(
                "slave_1",
                "slave",
                DataSourceHealthStatus.UP,
                0,
                0,
                Instant.now(),
                null,
                null,
                DataSourceRole.MASTER,
                "PostgreSQL",
                "test",
                null
        ));

        DataSourceSafetyReport report = checker.checkReadonlyRole(List.of(
                descriptor("slave_1", DataSourceRole.SLAVE, SynapseDbType.POSTGRESQL, false, true)
        ), healthRegistry);

        assertThat(code(report)).isEqualTo(DataSourceSafetyViolationCode.READONLY_ROLE_MISMATCH.name());
    }

    private static String code(DataSourceSafetyReport report) {
        return report.violations().getFirst().code();
    }

    private static DataSourceDescriptor descriptor(
            String name,
            DataSourceRole role,
            SynapseDbType dbType,
            boolean primary,
            boolean readonly
    ) {
        return new DataSourceDescriptor(
                name,
                role == DataSourceRole.MASTER ? "master" : "slave",
                role,
                dbType,
                primary,
                readonly,
                true,
                Map.of()
        );
    }
}
