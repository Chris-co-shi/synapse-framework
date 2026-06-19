package com.indigo.synapse.datasource.loadbalance;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机负载均衡选择器。
 */
public class RandomLoadBalanceSelector implements LoadBalanceSelector {

    @Override
    public Optional<DataSourceDescriptor> select(List<DataSourceDescriptor> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())));
    }
}
