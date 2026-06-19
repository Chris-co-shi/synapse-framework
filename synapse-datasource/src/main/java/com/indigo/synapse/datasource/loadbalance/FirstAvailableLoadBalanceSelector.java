package com.indigo.synapse.datasource.loadbalance;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;

import java.util.List;
import java.util.Optional;

/**
 * 首个可用数据源选择器。
 *
 * <p>该选择器属于 `synapse-datasource` 负载均衡边界，用于 `load-balance.enabled=false` 时的稳定选择语义。
 * 调用方必须先通过 {@link DataSourceCandidateFilter} 完成健康过滤；本选择器只从候选列表中返回第一个元素，
 * 不做随机、轮询或健康判断。</p>
 *
 * <p>实例无状态、线程安全。空候选返回 {@link Optional#empty()}，不会抛出异常。</p>
 */
public class FirstAvailableLoadBalanceSelector implements LoadBalanceSelector {

    @Override
    public Optional<DataSourceDescriptor> select(List<DataSourceDescriptor> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(candidates.getFirst());
    }
}
