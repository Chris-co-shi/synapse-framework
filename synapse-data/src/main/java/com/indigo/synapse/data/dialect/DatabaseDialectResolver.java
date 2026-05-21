package com.indigo.synapse.data.dialect;

public final class DatabaseDialectResolver {

    private DatabaseDialectResolver() {
    }

    public static DatabaseDialect fromJdbcUrl(String jdbcUrl) {
        return StandardDatabaseDialect.of(DatabaseType.fromJdbcUrl(jdbcUrl));
    }

    public static DatabaseDialect fromType(DatabaseType databaseType) {
        return StandardDatabaseDialect.of(databaseType);
    }
}
