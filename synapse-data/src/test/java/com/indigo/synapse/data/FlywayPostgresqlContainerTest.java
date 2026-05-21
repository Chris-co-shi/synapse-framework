package com.indigo.synapse.data;

import com.indigo.synapse.data.dialect.DatabaseDialectResolver;
import com.indigo.synapse.data.dialect.DatabaseType;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
class FlywayPostgresqlContainerTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void shouldRunMigrationOnPostgresqlContainer() throws Exception {
        assertEquals(DatabaseType.POSTGRESQL, DatabaseDialectResolver.fromJdbcUrl(POSTGRESQL.getJdbcUrl()).databaseType());

        Flyway flyway = Flyway.configure()
                .dataSource(POSTGRESQL.getJdbcUrl(), POSTGRESQL.getUsername(), POSTGRESQL.getPassword())
                .locations("classpath:db/migration")
                .load();

        MigrateResult result = flyway.migrate();

        assertEquals(1, result.migrationsExecuted);
        try (
                Connection connection = POSTGRESQL.createConnection("");
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select count(*) from synapse_data_probe")
        ) {
            resultSet.next();
            assertEquals(0, resultSet.getInt(1));
        }
    }
}
