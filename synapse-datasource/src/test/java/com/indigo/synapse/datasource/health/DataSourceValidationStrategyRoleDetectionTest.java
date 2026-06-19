package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceRole;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;
import com.indigo.synapse.datasource.testsupport.TestDataSources;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceValidationStrategyRoleDetectionTest {

    @Test
    void shouldDetectPostgresqlMasterAndSlaveRoles() {
        PostgreSqlDataSourceValidationStrategy strategy = new PostgreSqlDataSourceValidationStrategy();

        DataSourceValidationResult master = strategy.validate(
                descriptor("master", DataSourceRole.MASTER, SynapseDbType.POSTGRESQL, false),
                TestDataSources.healthy("PostgreSQL"),
                1
        );
        DataSourceValidationResult slave = strategy.validate(
                descriptor("slave_1", DataSourceRole.SLAVE, SynapseDbType.POSTGRESQL, true),
                TestDataSources.healthyReadonly("PostgreSQL"),
                1
        );

        assertThat(master.success()).isTrue();
        assertThat(master.detectedRole()).isEqualTo(DataSourceRole.MASTER);
        assertThat(slave.success()).isTrue();
        assertThat(slave.detectedRole()).isEqualTo(DataSourceRole.SLAVE);
    }

    @Test
    void shouldDetectMysqlMasterAndSlaveRoles() {
        MySqlDataSourceValidationStrategy strategy = new MySqlDataSourceValidationStrategy();

        DataSourceValidationResult master = strategy.validate(
                descriptor("master", DataSourceRole.MASTER, SynapseDbType.MYSQL, false),
                TestDataSources.healthy("MySQL"),
                1
        );
        DataSourceValidationResult slave = strategy.validate(
                descriptor("slave_1", DataSourceRole.SLAVE, SynapseDbType.MYSQL, true),
                TestDataSources.healthyReadonly("MySQL"),
                1
        );

        assertThat(master.success()).isTrue();
        assertThat(master.detectedRole()).isEqualTo(DataSourceRole.MASTER);
        assertThat(slave.success()).isTrue();
        assertThat(slave.detectedRole()).isEqualTo(DataSourceRole.SLAVE);
    }

    @Test
    void shouldKeepOracleRoleDetectionUnsupported() {
        OracleDataSourceValidationStrategy strategy = new OracleDataSourceValidationStrategy();

        DataSourceValidationResult result = strategy.validate(
                descriptor("oracle", DataSourceRole.UNKNOWN, SynapseDbType.ORACLE, false),
                TestDataSources.healthy("Oracle"),
                1
        );

        assertThat(result.success()).isTrue();
        assertThat(result.detectedRole()).isNull();
        assertThat(result.roleDetectionSupported()).isFalse();
    }

    private static DataSourceDescriptor descriptor(
            String name,
            DataSourceRole role,
            SynapseDbType dbType,
            boolean readonly
    ) {
        return new DataSourceDescriptor(
                name,
                role == DataSourceRole.MASTER ? "master" : "slave",
                role,
                dbType,
                role == DataSourceRole.MASTER,
                readonly,
                true,
                Map.of()
        );
    }
}
