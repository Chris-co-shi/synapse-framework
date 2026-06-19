package com.indigo.synapse.datasource.loadbalance;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceRole;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.health.DataSourceHealthStatus;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;

import java.util.List;
import java.util.Optional;

/**
 * 数据源候选过滤器。
 */
public class DataSourceCandidateFilter {

    private final SynapseDatasourceProperties properties;
    private final DataSourceHealthRegistry healthRegistry;

    public DataSourceCandidateFilter(
            SynapseDatasourceProperties properties,
            DataSourceHealthRegistry healthRegistry
    ) {
        this.properties = properties;
        this.healthRegistry = healthRegistry;
    }

    public List<DataSourceDescriptor> filter(String group, DataSourceRole role, List<DataSourceDescriptor> candidates) {
        return candidates.stream()
                .filter(candidate -> group == null || candidate.group().equals(group))
                .filter(candidate -> role == null || candidate.role() == role)
                .filter(candidate -> acceptHealth(candidate.name()))
                .toList();
    }

    private boolean acceptHealth(String dataSourceName) {
        Optional<DataSourceHealthStatus> status = healthRegistry.find(dataSourceName).map(snapshot -> snapshot.status());
        if (status.isEmpty()) {
            return false;
        }
        return switch (status.get()) {
            case UP -> true;
            case DEGRADED -> properties.getLoadBalance().isAcceptDegraded();
            case RECOVERING -> properties.getLoadBalance().isAcceptRecovering();
            case DOWN, DISABLED, UNKNOWN -> false;
        };
    }
}
