package com.indigo.synapse.datasource.safety;

public record DataSourceSafetyReport(
        boolean safe,
        String dataSourceName,
        String message
) {
}
