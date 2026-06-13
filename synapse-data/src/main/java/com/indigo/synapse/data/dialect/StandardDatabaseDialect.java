package com.indigo.synapse.data.dialect;

import com.indigo.synapse.data.json.JsonStorageType;

public final class StandardDatabaseDialect implements DatabaseDialect {

    private final DatabaseType databaseType;

    private StandardDatabaseDialect(DatabaseType databaseType) {
        this.databaseType = databaseType == null ? DatabaseType.UNKNOWN : databaseType;
    }

    public static StandardDatabaseDialect of(DatabaseType databaseType) {
        return new StandardDatabaseDialect(databaseType);
    }

    @Override
    public DatabaseType databaseType() {
        return databaseType;
    }

    @Override
    public boolean supportsPartialIndex() {
        return databaseType == DatabaseType.POSTGRESQL;
    }

    @Override
    public boolean supportsJsonColumn() {
        return databaseType == DatabaseType.MYSQL || databaseType == DatabaseType.POSTGRESQL;
    }

    @Override
    public String jsonColumnType(JsonStorageType storageType) {
        JsonStorageType resolvedStorageType = storageType == null ? JsonStorageType.JSON : storageType;
        if (databaseType == DatabaseType.POSTGRESQL) {
            return resolvedStorageType == JsonStorageType.JSONB ? "jsonb" : "json";
        }
        if (databaseType == DatabaseType.MYSQL) {
            return "json";
        }
        return DatabaseDialect.super.jsonColumnType(resolvedStorageType);
    }
}
