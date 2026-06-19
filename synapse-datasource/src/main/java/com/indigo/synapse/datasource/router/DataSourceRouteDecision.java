package com.indigo.synapse.datasource.router;

import java.util.Objects;

/**
 * 数据源路由决策。
 *
 * <p>该 record 属于 `synapse-datasource` 的路由决策模型，只描述“应该使用哪个数据源或候选组”，
 * 不执行动态数据源上下文切换，不解析 SQL，也不参与事务控制。主要由
 * {@link DataSourceRoutingPolicy} 生成组级意图，并由 {@link DataSourceRoutingCoordinator} 解析为具体
 * 数据源。</p>
 *
 * <p>实例不可变、线程安全。对外返回的 {@link RouteTarget#MASTER} 和
 * {@link RouteTarget#SPECIFIC_DATASOURCE} 决策必须包含非空数据源名称；组级目标只允许作为内部策略意图。</p>
 */
public record DataSourceRouteDecision(
        RouteTarget target,
        String dataSourceName,
        String group,
        RouteReason reason
) {
    public DataSourceRouteDecision {
        target = Objects.requireNonNull(target, "target must not be null");
        reason = reason == null ? RouteReason.UNKNOWN : reason;
        if ((target == RouteTarget.MASTER || target == RouteTarget.SPECIFIC_DATASOURCE)
                && (dataSourceName == null || dataSourceName.isBlank())) {
            throw new IllegalArgumentException("dataSourceName must not be blank for " + target);
        }
    }
}
