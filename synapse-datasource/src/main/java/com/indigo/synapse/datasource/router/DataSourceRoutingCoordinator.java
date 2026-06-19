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
 * <p>该类属于 `synapse-datasource` 路由治理边界，对外实现 {@link DataSourceRouter}。它把
 * {@link DataSourceRoutingPolicy} 生成的组级意图解析为具体数据源名称：读请求经过候选过滤和负载均衡，
 * 写请求或强制主库读请求必须通过 {@link DataSourceFailoverManager} 校验唯一健康 master。</p>
 *
 * <p>协调器只生成路由决策，不切换线程上下文，不操作 dynamic-datasource API，不解析 SQL，也不做应用层主库晋升。
 * 实例无状态且线程安全。无法生成安全决策时抛出 {@link DatasourceUnavailableException}。</p>
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

    /**
     * 生成具体数据源路由决策。
     *
     * @param request 路由请求，包含操作类型、事务状态、写后读状态、锁查询标识和可选偏好组
     * @return 可直接用于下游适配层的具体路由决策
     * @throws DatasourceUnavailableException 当无法选择健康数据源或 master 不可用时抛出
     */
    @Override
    public DataSourceRouteDecision route(DataSourceRouteRequest request) {
        DataSourceRouteDecision policyDecision = routingPolicy.decide(request);
        if (policyDecision.target() == RouteTarget.MASTER) {
            return failoverManager.requireMaster(request, policyDecision.reason());
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
                .orElseGet(() -> failoverManager.fallbackForRead(request, RouteReason.NO_AVAILABLE_SLAVE));
    }
}
