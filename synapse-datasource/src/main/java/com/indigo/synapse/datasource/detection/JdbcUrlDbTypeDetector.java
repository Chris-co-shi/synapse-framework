package com.indigo.synapse.datasource.detection;

import com.indigo.synapse.datasource.descriptor.SynapseDbType;

import javax.sql.DataSource;
import java.util.Locale;
import java.util.Optional;

public class JdbcUrlDbTypeDetector implements DbTypeDetector {

    @Override
    public Optional<SynapseDbType> detect(String dataSourceName, DataSource dataSource, String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return Optional.empty();
        }
        String normalizedUrl = jdbcUrl.toLowerCase(Locale.ROOT);
        if (normalizedUrl.startsWith("jdbc:postgresql:")) {
            return Optional.of(SynapseDbType.POSTGRESQL);
        }
        if (normalizedUrl.startsWith("jdbc:mysql:")) {
            return Optional.of(SynapseDbType.MYSQL);
        }
        if (normalizedUrl.startsWith("jdbc:mariadb:")) {
            return Optional.of(SynapseDbType.MARIADB);
        }
        if (normalizedUrl.startsWith("jdbc:oracle:")) {
            return Optional.of(SynapseDbType.ORACLE);
        }
        if (normalizedUrl.startsWith("jdbc:sqlserver:")) {
            return Optional.of(SynapseDbType.SQL_SERVER);
        }
        if (normalizedUrl.startsWith("jdbc:h2:")) {
            return Optional.of(SynapseDbType.H2);
        }
        if (normalizedUrl.startsWith("jdbc:clickhouse:")) {
            return Optional.of(SynapseDbType.CLICKHOUSE);
        }
        return Optional.empty();
    }
}
