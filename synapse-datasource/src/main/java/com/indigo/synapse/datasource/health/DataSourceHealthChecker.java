package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;

public class DataSourceHealthChecker {

    private final SynapseDatasourceProperties properties;
    private final DataSourceHealthRegistry registry;

    public DataSourceHealthChecker(SynapseDatasourceProperties properties, DataSourceHealthRegistry registry) {
        this.properties = properties;
        this.registry = registry;
    }

    public DataSourceHealthSnapshot check(String dataSourceName, String group, DataSource dataSource) {
        DataSourceHealthSnapshot previous = registry.find(dataSourceName).orElse(null);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.setQueryTimeout((int) Math.max(1, properties.getHealth().getTimeout().toSeconds()));
            statement.execute("select 1");
            DataSourceHealthSnapshot snapshot = successSnapshot(dataSourceName, group, previous);
            registry.update(snapshot);
            return snapshot;
        } catch (Exception ex) {
            DataSourceHealthSnapshot snapshot = failureSnapshot(dataSourceName, group, previous, ex);
            registry.update(snapshot);
            return snapshot;
        }
    }

    private DataSourceHealthSnapshot successSnapshot(String dataSourceName, String group, DataSourceHealthSnapshot previous) {
        int successCount = previous == null ? 1 : previous.successCount() + 1;
        boolean recovering = previous != null
                && (previous.status() == DataSourceHealthStatus.DOWN || previous.status() == DataSourceHealthStatus.RECOVERING)
                && successCount < properties.getHealth().getRecoveryThreshold();
        DataSourceHealthStatus status = recovering ? DataSourceHealthStatus.RECOVERING : DataSourceHealthStatus.UP;
        return new DataSourceHealthSnapshot(
                dataSourceName,
                group,
                status,
                0,
                successCount,
                Instant.now(),
                previous == null ? null : previous.lastFailureAt(),
                previous == null ? null : previous.lastFailureMessage()
        );
    }

    private DataSourceHealthSnapshot failureSnapshot(String dataSourceName,
                                                     String group,
                                                     DataSourceHealthSnapshot previous,
                                                     Exception ex) {
        int failureCount = previous == null ? 1 : previous.failureCount() + 1;
        DataSourceHealthStatus status = failureCount >= properties.getHealth().getFailureThreshold()
                ? DataSourceHealthStatus.DOWN
                : DataSourceHealthStatus.DEGRADED;
        return new DataSourceHealthSnapshot(
                dataSourceName,
                group,
                status,
                failureCount,
                0,
                previous == null ? null : previous.lastSuccessAt(),
                Instant.now(),
                ex.getMessage()
        );
    }
}
