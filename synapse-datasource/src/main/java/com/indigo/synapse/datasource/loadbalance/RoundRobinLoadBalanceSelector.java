package com.indigo.synapse.datasource.loadbalance;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalanceSelector implements LoadBalanceSelector {

    private final DataSourceHealthRegistry registry;
    private final AtomicInteger counter = new AtomicInteger();

    public RoundRobinLoadBalanceSelector(DataSourceHealthRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Optional<DataSourceDescriptor> select(List<DataSourceDescriptor> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }
        List<DataSourceDescriptor> availableCandidates = candidates.stream()
                .filter(candidate -> registry.isAvailable(candidate.name()))
                .toList();
        if (availableCandidates.isEmpty()) {
            return Optional.empty();
        }
        int index = Math.floorMod(counter.getAndIncrement(), availableCandidates.size());
        return Optional.of(availableCandidates.get(index));
    }
}
