package com.indigo.synapse.datasource.router;

public record DataSourceRouteDecision(
        RouteTarget target,
        String dataSourceName,
        String group,
        RouteReason reason
) {
}
