package com.indigo.synapse.datasource.loadbalance;

import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;

/**
 * 负载均衡选择器工厂。
 */
public class LoadBalanceSelectorFactory {

    private final DataSourceHealthRegistry healthRegistry;

    public LoadBalanceSelectorFactory(DataSourceHealthRegistry healthRegistry) {
        this.healthRegistry = healthRegistry;
    }

    public LoadBalanceSelector create(LoadBalanceStrategy strategy) {
        return switch (strategy) {
            case RANDOM -> new RandomLoadBalanceSelector();
            case HEALTH_FIRST -> new HealthFirstLoadBalanceSelector(healthRegistry);
            case ROUND_ROBIN -> new RoundRobinLoadBalanceSelector();
        };
    }
}
