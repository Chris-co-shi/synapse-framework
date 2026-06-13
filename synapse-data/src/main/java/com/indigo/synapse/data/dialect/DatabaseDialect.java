package com.indigo.synapse.data.dialect;

import com.indigo.synapse.data.json.JsonStorageType;

public interface DatabaseDialect {

    DatabaseType databaseType();

    default boolean supportsPartialIndex() {
        return false;
    }

    default boolean supportsJsonColumn() {
        return false;
    }

    default String jsonColumnType(JsonStorageType storageType) {
        throw new UnsupportedOperationException(databaseType() + " does not support JSON column");
    }
}
