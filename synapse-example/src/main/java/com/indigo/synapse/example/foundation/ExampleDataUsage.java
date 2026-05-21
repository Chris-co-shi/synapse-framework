package com.indigo.synapse.example.foundation;

public record ExampleDataUsage(
        String selectedDataSource,
        String restoredDataSource,
        String databaseType,
        boolean supportsJsonColumn
) {
}
