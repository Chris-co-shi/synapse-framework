package com.indigo.synapse.data.dialect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseDialectResolverTest {

    @Test
    void shouldResolveDialectFromJdbcUrl() {
        DatabaseDialect postgres = DatabaseDialectResolver.fromJdbcUrl("jdbc:postgresql://localhost:5432/app");
        DatabaseDialect mysql = DatabaseDialectResolver.fromJdbcUrl("jdbc:mysql://localhost:3306/app");
        DatabaseDialect h2 = DatabaseDialectResolver.fromJdbcUrl("jdbc:h2:mem:test");

        assertEquals(DatabaseType.POSTGRESQL, postgres.databaseType());
        assertTrue(postgres.supportsPartialIndex());
        assertTrue(postgres.supportsJsonColumn());
        assertEquals(DatabaseType.MYSQL, mysql.databaseType());
        assertFalse(mysql.supportsPartialIndex());
        assertTrue(mysql.supportsJsonColumn());
        assertEquals(DatabaseType.H2, h2.databaseType());
        assertFalse(h2.supportsPartialIndex());
    }
}
