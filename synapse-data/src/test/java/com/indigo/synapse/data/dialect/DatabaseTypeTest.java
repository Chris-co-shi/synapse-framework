package com.indigo.synapse.data.dialect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseTypeTest {

    @Test
    void shouldResolveKnownJdbcUrls() {
        assertEquals(DatabaseType.H2, DatabaseType.fromJdbcUrl("jdbc:h2:mem:test"));
        assertEquals(DatabaseType.MYSQL, DatabaseType.fromJdbcUrl("jdbc:mysql://localhost:3306/app"));
        assertEquals(DatabaseType.POSTGRESQL, DatabaseType.fromJdbcUrl("jdbc:postgresql://localhost:5432/app"));
    }

    @Test
    void shouldReturnUnknownForBlankOrUnsupportedUrl() {
        assertEquals(DatabaseType.UNKNOWN, DatabaseType.fromJdbcUrl(null));
        assertEquals(DatabaseType.UNKNOWN, DatabaseType.fromJdbcUrl(" "));
        assertEquals(DatabaseType.UNKNOWN, DatabaseType.fromJdbcUrl("jdbc:oracle:thin:@localhost"));
    }
}
