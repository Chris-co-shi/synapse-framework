package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceRole;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import org.springframework.context.ApplicationEventPublisher;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class DataSourceHealthChecker {

    private final SynapseDatasourceProperties properties;
    private final DataSourceHealthRegistry registry;
    private final List<DataSourceValidationStrategy> strategies;
    private final ApplicationEventPublisher eventPublisher;

    public DataSourceHealthChecker(SynapseDatasourceProperties properties, DataSourceHealthRegistry registry) {
        this(properties, registry, List.of(
                new PostgreSqlDataSourceValidationStrategy(),
                new MySqlDataSourceValidationStrategy(),
                new OracleDataSourceValidationStrategy(),
                new GenericDataSourceValidationStrategy()
        ), null);
    }

    public DataSourceHealthChecker(
            SynapseDatasourceProperties properties,
            DataSourceHealthRegistry registry,
            List<DataSourceValidationStrategy> strategies
    ) {
        this(properties, registry, strategies, null);
    }

    public DataSourceHealthChecker(
            SynapseDatasourceProperties properties,
            DataSourceHealthRegistry registry,
            List<DataSourceValidationStrategy> strategies,
            ApplicationEventPublisher eventPublisher
    ) {
        this.properties = properties;
        this.registry = registry;
        this.strategies = List.copyOf(strategies);
        this.eventPublisher = eventPublisher;
    }

    public DataSourceHealthSnapshot check(String dataSourceName, String group, DataSource dataSource) {
        DataSourceDescriptor descriptor = new DataSourceDescriptor(
                dataSourceName,
                group,
                DataSourceRole.UNKNOWN,
                SynapseDbType.UNKNOWN,
                false,
                false,
                true,
                Map.of()
        );
        return check(descriptor, dataSource);
    }

    public DataSourceHealthSnapshot check(DataSourceDescriptor descriptor, DataSource dataSource) {
        DataSourceHealthSnapshot previous = registry.find(descriptor.name()).orElse(null);
        DataSourceValidationResult result = selectStrategy(descriptor.dbType()).validate(
                descriptor,
                dataSource,
                (int) Math.max(1, properties.getHealth().getTimeout().toSeconds())
        );
        if (result.success()) {
            DataSourceHealthSnapshot snapshot = successSnapshot(descriptor.name(), descriptor.group(), previous);
            registry.update(snapshot);
            publishIfChanged(previous, snapshot);
            return snapshot;
        }
        DataSourceHealthSnapshot snapshot = failureSnapshot(descriptor.name(), descriptor.group(), previous, result.message());
        registry.update(snapshot);
        publishIfChanged(previous, snapshot);
        return snapshot;
    }

    private DataSourceHealthSnapshot successSnapshot(String dataSourceName, String group, DataSourceHealthSnapshot previous) {
        if (previous != null && previous.status() == DataSourceHealthStatus.DISABLED) {
            return previous;
        }
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
                                                     String failureMessage) {
        if (previous != null && previous.status() == DataSourceHealthStatus.DISABLED) {
            return previous;
        }
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
                failureMessage
        );
    }

    private DataSourceValidationStrategy selectStrategy(SynapseDbType dbType) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(dbType))
                .findFirst()
                .orElseGet(GenericDataSourceValidationStrategy::new);
    }

    private void publishIfChanged(DataSourceHealthSnapshot previous, DataSourceHealthSnapshot current) {
        if (eventPublisher == null || previous == null || previous.status() == current.status()) {
            return;
        }
        eventPublisher.publishEvent(new DataSourceHealthChangedEvent(previous, current));
        if (current.status() == DataSourceHealthStatus.DOWN) {
            eventPublisher.publishEvent(new DataSourceDownEvent(current));
        }
        if (previous.status() == DataSourceHealthStatus.DOWN && current.status() == DataSourceHealthStatus.UP) {
            eventPublisher.publishEvent(new DataSourceRecoveredEvent(current));
        }
    }
}
