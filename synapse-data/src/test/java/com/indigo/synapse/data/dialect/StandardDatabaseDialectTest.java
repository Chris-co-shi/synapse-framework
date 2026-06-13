package com.indigo.synapse.data.dialect;

import com.indigo.synapse.data.json.JsonStorageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StandardDatabaseDialectTest {

    @Test
    void postgresShouldSupportPartialIndexAndJsonColumn() {
        StandardDatabaseDialect dialect = StandardDatabaseDialect.of(DatabaseType.POSTGRESQL);

        assertTrue(dialect.supportsPartialIndex());
        assertTrue(dialect.supportsJsonColumn());
        assertEquals("json", dialect.jsonColumnType(JsonStorageType.JSON));
        assertEquals("jsonb", dialect.jsonColumnType(JsonStorageType.JSONB));
    }

    @Test
    void mysqlShouldSupportJsonColumnButNotPartialIndex() {
        StandardDatabaseDialect dialect = StandardDatabaseDialect.of(DatabaseType.MYSQL);

        assertFalse(dialect.supportsPartialIndex());
        assertTrue(dialect.supportsJsonColumn());
        assertEquals("json", dialect.jsonColumnType(JsonStorageType.JSON));
        assertEquals("json", dialect.jsonColumnType(JsonStorageType.JSONB));
    }

    @Test
    void h2ShouldUseConservativeDefaults() {
        StandardDatabaseDialect dialect = StandardDatabaseDialect.of(DatabaseType.H2);

        assertFalse(dialect.supportsPartialIndex());
        assertFalse(dialect.supportsJsonColumn());
        assertThrows(UnsupportedOperationException.class, () -> dialect.jsonColumnType(JsonStorageType.JSON));
    }
}
