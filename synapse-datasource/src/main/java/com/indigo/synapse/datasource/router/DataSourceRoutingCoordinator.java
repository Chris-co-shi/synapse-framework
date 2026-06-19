package com.indigo.synapse.datasource.router;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.failover.DataSourceFailoverManager;
import com.indigo.synapse.datasource.loadbalance.DataSourceCandidateFilter;
import com.indigo.synapse.datasource.loadbalance.LoadBalanceSelector;

import java.util.List;

/**
 * 数据源路由协调器。
 *
 * <p>协调器只生成路由决策，不切换线程上下文，也不参与 SQL 拦截。</p>
 */
public class DataSourceRoutingCoordinator implements DataSourceRouter {

    private final DataSourceRoutingPolicy routingPolicy;
    private final DataSourceDescriptorRegistry descriptorRegistry;
    private final DataSourceCandidateFilter candidateFilter;
    private final LoadBalanceSelector loadBalanceSelector;
    private final DataSourceFailoverManager failoverManager;

    public DataSourceRoutingCoordinator(
            DataSourceRoutingPolicy routingPolicy,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceCandidateFilter candidateFilter,
            LoadBalanceSelector loadBalanceSelector,
            DataSourceFailoverManager failoverManager
    ) {
        this.routingPolicy = routingPolicy;
        this.descriptorRegistry = descriptorRegistry;
        this.candidateFilter = candidateFilter;
        this.loadBalanceSelector = loadBalanceSelector;
        this.failoverManager = failoverManager;
    }

    @Override
    public DataSourceRouteDecision route(DataSourceRouteRequest request) {
        DataSourceRouteDecision policyDecision = routingPolicy.decide(request);
        if (policyDecision.target() == RouteTarget.MASTER) {
            return descriptorRegistry.findPrimary()
                    .map(primary -> new DataSourceRouteDecision(
                            RouteTarget.MASTER,
                            primary.name(),
                            primary.group(),
                            policyDecision.reason()
                    ))
                    .orElse(policyDecision);
        }
        List<DataSourceDescriptor> candidates = descriptorRegistry.findAll().stream()
                .filter(DataSourceDescriptor::readonly)
                .toList();
        List<DataSourceDescriptor> available = candidateFilter.filter(policyDecision.group(), null, candidates);
        return loadBalanceSelector.select(available)
                .map(selected -> new DataSourceRouteDecision(
                        RouteTarget.SPECIFIC_DATASOURCE,
                        selected.name(),
                        selected.group(),
                        policyDecision.reason()
                ))
                .orElseGet(() -> failoverManager.fallbackForRead(RouteReason.NO_AVAILABLE_SLAVE));
    }
}
