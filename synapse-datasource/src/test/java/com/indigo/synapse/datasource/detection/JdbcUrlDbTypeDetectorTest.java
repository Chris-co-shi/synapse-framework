package com.indigo.synapse.datasource.detection;

import com.indigo.synapse.datasource.descriptor.SynapseDbType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcUrlDbTypeDetectorTest {

    private final JdbcUrlDbTypeDetector detector = new JdbcUrlDbTypeDetector();

    @Test
    void shouldDetectKnownJdbcUrls() {
        assertThat(detector.detect("postgres", null, "jdbc:postgresql://localhost/db")).contains(SynapseDbType.POSTGRESQL);
        assertThat(detector.detect("mysql", null, "jdbc:mysql://localhost/db")).contains(SynapseDbType.MYSQL);
        assertThat(detector.detect("mariadb", null, "jdbc:mariadb://localhost/db")).contains(SynapseDbType.MARIADB);
        assertThat(detector.detect("oracle", null, "jdbc:oracle:thin:@localhost:1521:xe")).contains(SynapseDbType.ORACLE);
        assertThat(detector.detect("sqlserver", null, "jdbc:sqlserver://localhost;databaseName=db")).contains(SynapseDbType.SQL_SERVER);
        assertThat(detector.detect("h2", null, "jdbc:h2:mem:test")).contains(SynapseDbType.H2);
        assertThat(detector.detect("clickhouse", null, "jdbc:clickhouse://localhost:8123/db")).contains(SynapseDbType.CLICKHOUSE);
    }

    @Test
    void shouldReturnEmptyForUnknownJdbcUrl() {
        assertThat(detector.detect("unknown", null, "jdbc:unknown://localhost/db")).isEmpty();
    }
}
