package com.indigo.synapse.datasource.loadbalance;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 健康状态优先负载均衡选择器。
 */
public class HealthFirstLoadBalanceSelector implements LoadBalanceSelector {

    private final DataSourceHealthRegistry healthRegistry;

    public HealthFirstLoadBalanceSelector(DataSourceHealthRegistry healthRegistry) {
        this.healthRegistry = healthRegistry;
    }

    @Override
    public Optional<DataSourceDescriptor> select(List<DataSourceDescriptor> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }
        return candidates.stream()
                .min(Comparator.comparingInt(candidate -> weight(candidate.name())));
    }

    private int weight(String dataSourceName) {
        return healthRegistry.find(dataSourceName)
                .map(snapshot -> switch (snapshot.status()) {
                    case UP -> 0;
                    case RECOVERING -> 1;
                    case DEGRADED -> 2;
                    case DOWN -> 3;
                    case UNKNOWN -> 4;
                    case DISABLED -> 5;
                })
                .orElse(6);
    }
}
