package com.indigo.synapse.datasource.loadbalance;

import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;

/**
 * 负载均衡选择器工厂。
 *
 * <p>该工厂属于 `synapse-datasource` 负载均衡边界，由自动配置创建默认
 * {@link LoadBalanceSelector}。它不负责候选过滤，也不读取 DataSource 连接。</p>
 */
public class LoadBalanceSelectorFactory {

    private final DataSourceHealthRegistry healthRegistry;

    public LoadBalanceSelectorFactory(DataSourceHealthRegistry healthRegistry) {
        this.healthRegistry = healthRegistry;
    }

    /**
     * 根据配置创建选择器。
     *
     * @param properties 数据源治理配置
     * @return 选择器实例；关闭负载均衡时返回首个可用选择器
     */
    public LoadBalanceSelector create(SynapseDatasourceProperties properties) {
        if (!properties.getLoadBalance().isEnabled()) {
            return new FirstAvailableLoadBalanceSelector();
        }
        return create(properties.getLoadBalance().getDefaultStrategy());
    }

    /**
     * 根据策略创建选择器。
     *
     * @param strategy 负载均衡策略
     * @return 选择器实例
     */
    public LoadBalanceSelector create(LoadBalanceStrategy strategy) {
        return switch (strategy) {
            case RANDOM -> new RandomLoadBalanceSelector();
            case HEALTH_FIRST -> new HealthFirstLoadBalanceSelector(healthRegistry);
            case ROUND_ROBIN -> new RoundRobinLoadBalanceSelector();
        };
    }
}
