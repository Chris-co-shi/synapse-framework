package com.indigo.synapse.data.dialect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardDatabaseDialectTest {

    @Test
    void postgresShouldSupportPartialIndexAndJsonColumn() {
        StandardDatabaseDialect dialect = StandardDatabaseDialect.of(DatabaseType.POSTGRESQL);

        assertTrue(dialect.supportsPartialIndex());
        assertTrue(dialect.supportsJsonColumn());
    }

    @Test
    void mysqlShouldSupportJsonColumnButNotPartialIndex() {
        StandardDatabaseDialect dialect = StandardDatabaseDialect.of(DatabaseType.MYSQL);

        assertFalse(dialect.supportsPartialIndex());
        assertTrue(dialect.supportsJsonColumn());
    }

    @Test
    void h2ShouldUseConservativeDefaults() {
        StandardDatabaseDialect dialect = StandardDatabaseDialect.of(DatabaseType.H2);

        assertFalse(dialect.supportsPartialIndex());
        assertFalse(dialect.supportsJsonColumn());
    }
}
