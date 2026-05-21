package com.indigo.synapse.data;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlywayMigrationTest {

    @Test
    void shouldRunH2Migration() throws Exception {
        String jdbcUrl = "jdbc:h2:mem:synapse_data;DB_CLOSE_DELAY=-1";
        Flyway flyway = Flyway.configure()
                .dataSource(jdbcUrl, "sa", "")
                .locations("classpath:db/migration")
                .load();

        MigrateResult result = flyway.migrate();

        assertEquals(1, result.migrationsExecuted);
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "");
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select count(*) from synapse_data_probe")
        ) {
            resultSet.next();
            assertEquals(0, resultSet.getInt(1));
        }
    }
}
