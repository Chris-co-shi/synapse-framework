package com.indigo.synapse.datasource.router;

public record DataSourceRouteRequest(
        DataSourceOperation operation,
        boolean transactional,
        boolean afterWrite,
        boolean lockQuery,
        String preferredGroup
) {
}
