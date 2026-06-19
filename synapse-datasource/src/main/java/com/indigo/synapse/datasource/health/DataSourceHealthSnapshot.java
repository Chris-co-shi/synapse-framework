package com.indigo.synapse.datasource.health;

import java.time.Instant;

public record DataSourceHealthSnapshot(
        String dataSourceName,
        String group,
        DataSourceHealthStatus status,
        int failureCount,
        int successCount,
        Instant lastSuccessAt,
        Instant lastFailureAt,
        String lastFailureMessage
) {
}
