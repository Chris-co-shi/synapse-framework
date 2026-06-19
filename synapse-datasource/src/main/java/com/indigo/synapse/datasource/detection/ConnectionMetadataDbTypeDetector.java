package com.indigo.synapse.datasource.detection;

import com.indigo.synapse.datasource.descriptor.SynapseDbType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

public class ConnectionMetadataDbTypeDetector implements DbTypeDetector {

    @Override
    public Optional<SynapseDbType> detect(String dataSourceName, DataSource dataSource, String jdbcUrl) {
        if (dataSource == null) {
            return Optional.empty();
        }
        try (Connection connection = dataSource.getConnection()) {
            return detectProductName(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException ex) {
            return Optional.empty();
        }
    }

    private static Optional<SynapseDbType> detectProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            return Optional.empty();
        }
        String normalizedName = productName.toLowerCase(Locale.ROOT);
        if (normalizedName.contains("postgresql")) {
            return Optional.of(SynapseDbType.POSTGRESQL);
        }
        if (normalizedName.contains("mariadb")) {
            return Optional.of(SynapseDbType.MARIADB);
        }
        if (normalizedName.contains("mysql")) {
            return Optional.of(SynapseDbType.MYSQL);
        }
        if (normalizedName.contains("oracle")) {
            return Optional.of(SynapseDbType.ORACLE);
        }
        if (normalizedName.contains("sql server")) {
            return Optional.of(SynapseDbType.SQL_SERVER);
        }
        if (normalizedName.contains("h2")) {
            return Optional.of(SynapseDbType.H2);
        }
        if (normalizedName.contains("clickhouse")) {
            return Optional.of(SynapseDbType.CLICKHOUSE);
        }
        return Optional.empty();
    }
}
