package com.indigo.synapse.datasource.router;

import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;

/**
 * 默认数据源路由策略。
 */
public class DefaultDataSourceRoutingPolicy implements DataSourceRoutingPolicy {

    private final SynapseDatasourceProperties properties;

    public DefaultDataSourceRoutingPolicy(SynapseDatasourceProperties properties) {
        this.properties = properties;
    }

    @Override
    public DataSourceRouteDecision decide(DataSourceRouteRequest request) {
        DataSourceOperation operation = request == null ? DataSourceOperation.UNKNOWN : request.operation();
        if (operation == DataSourceOperation.WRITE || operation == DataSourceOperation.DDL || operation == DataSourceOperation.CALL) {
            return master(RouteReason.WRITE_OPERATION);
        }
        if (request == null || operation == DataSourceOperation.UNKNOWN) {
            return master(RouteReason.UNKNOWN);
        }
        if (request.transactional() && properties.getRouter().isForceMasterInTransaction()) {
            return master(RouteReason.TRANSACTION_ACTIVE);
        }
        if (request.afterWrite() && properties.getRouter().isForceMasterAfterWrite()) {
            return master(RouteReason.AFTER_WRITE_READ);
        }
        if (request.lockQuery() && properties.getRouter().isForceMasterForLockQuery()) {
            return master(RouteReason.LOCK_QUERY);
        }
        String preferredGroup = request.preferredGroup();
        if (preferredGroup != null && !preferredGroup.isBlank()) {
            return new DataSourceRouteDecision(RouteTarget.SLAVE_GROUP, null, preferredGroup, RouteReason.MANUAL_POLICY);
        }
        return new DataSourceRouteDecision(
                RouteTarget.SLAVE_GROUP,
                null,
                properties.getConvention().getSlaveGroup(),
                RouteReason.READONLY_QUERY
        );
    }

    private DataSourceRouteDecision master(RouteReason reason) {
        return new DataSourceRouteDecision(
                RouteTarget.MASTER,
                properties.getConvention().getMasterName(),
                null,
                reason
        );
    }
}
