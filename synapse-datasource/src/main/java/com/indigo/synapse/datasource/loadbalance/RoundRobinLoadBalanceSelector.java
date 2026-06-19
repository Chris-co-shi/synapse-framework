package com.indigo.synapse.datasource.loadbalance;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalanceSelector implements LoadBalanceSelector {

    private final AtomicInteger counter = new AtomicInteger();

    public RoundRobinLoadBalanceSelector() {
    }

    @Override
    public Optional<DataSourceDescriptor> select(List<DataSourceDescriptor> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }
        int index = Math.floorMod(counter.getAndIncrement(), candidates.size());
        return Optional.of(candidates.get(index));
    }
}
