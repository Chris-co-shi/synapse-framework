package com.indigo.synapse.data.dialect;

public interface DatabaseDialect {

    DatabaseType databaseType();

    default boolean supportsPartialIndex() {
        return false;
    }

    default boolean supportsJsonColumn() {
        return false;
    }
}
