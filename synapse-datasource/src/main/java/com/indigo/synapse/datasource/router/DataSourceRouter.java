package com.indigo.synapse.datasource.router;

public interface DataSourceRouter {

    DataSourceRouteDecision route(DataSourceRouteRequest request);
}
