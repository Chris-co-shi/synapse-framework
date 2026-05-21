package com.indigo.synapse.data.dialect;

import java.util.Locale;

public enum DatabaseType {

    H2,
    MYSQL,
    POSTGRESQL,
    UNKNOWN;

    public static DatabaseType fromJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return UNKNOWN;
        }
        String normalized = jdbcUrl.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("jdbc:h2:")) {
            return H2;
        }
        if (normalized.startsWith("jdbc:mysql:")) {
            return MYSQL;
        }
        if (normalized.startsWith("jdbc:postgresql:")) {
            return POSTGRESQL;
        }
        return UNKNOWN;
    }
}
