package com.indigo.synapse.datasource.router;

import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;

public class DefaultDataSourceRouter implements DataSourceRouter {

    private final DataSourceRoutingPolicy policy;

    public DefaultDataSourceRouter(SynapseDatasourceProperties properties) {
        this.policy = new DefaultDataSourceRoutingPolicy(properties);
    }

    @Override
    public DataSourceRouteDecision route(DataSourceRouteRequest request) {
        return policy.decide(request);
    }
}
