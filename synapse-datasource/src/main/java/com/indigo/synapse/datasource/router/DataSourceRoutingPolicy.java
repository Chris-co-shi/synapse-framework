package com.indigo.synapse.datasource.router;

/**
 * 数据源路由策略。
 */
public interface DataSourceRoutingPolicy {

    DataSourceRouteDecision decide(DataSourceRouteRequest request);
}
