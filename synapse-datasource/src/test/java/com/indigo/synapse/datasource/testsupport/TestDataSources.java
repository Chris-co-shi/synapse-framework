package com.indigo.synapse.datasource.testsupport;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public final class TestDataSources {

    private TestDataSources() {
    }

    public static DataSource healthy(String productName) {
        return proxy(productName, true, false);
    }

    public static DataSource healthyReadonly(String productName) {
        return proxy(productName, true, true);
    }

    public static DataSource failing(String productName) {
        return proxy(productName, false, false);
    }

    private static DataSource proxy(String productName, boolean healthy, boolean readonly) {
        return (DataSource) Proxy.newProxyInstance(
                TestDataSources.class.getClassLoader(),
                new Class<?>[]{DataSource.class},
                (proxy, method, args) -> {
                    if ("getConnection".equals(method.getName())) {
                        if (!healthy) {
                            throw new SQLException("connection failed");
                        }
                        return connection(productName, readonly);
                    }
                    if ("unwrap".equals(method.getName())) {
                        return proxy;
                    }
                    if ("isWrapperFor".equals(method.getName())) {
                        return false;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private static Connection connection(String productName, boolean readonly) {
        return (Connection) Proxy.newProxyInstance(
                TestDataSources.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isValid" -> true;
                    case "isReadOnly" -> readonly;
                    case "createStatement" -> statement(readonly);
                    case "getMetaData" -> metadata(productName);
                    case "close" -> null;
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private static Statement statement(boolean readonly) {
        return (Statement) Proxy.newProxyInstance(
                TestDataSources.class.getClassLoader(),
                new Class<?>[]{Statement.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "execute" -> true;
                    case "executeQuery" -> resultSet((String) args[0], readonly);
                    case "setQueryTimeout", "close" -> null;
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private static ResultSet resultSet(String sql, boolean readonly) {
        String normalizedSql = sql.toLowerCase(Locale.ROOT);
        boolean[] consumed = {false};
        return (ResultSet) Proxy.newProxyInstance(
                TestDataSources.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> {
                        if (consumed[0]) {
                            yield false;
                        }
                        consumed[0] = true;
                        yield true;
                    }
                    case "getBoolean" -> booleanResult(normalizedSql, readonly, (Integer) args[0]);
                    case "close" -> null;
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private static boolean booleanResult(String sql, boolean readonly, int columnIndex) {
        if (sql.contains("pg_is_in_recovery")) {
            return readonly;
        }
        if (sql.contains("@@read_only") || sql.contains("@@super_read_only")) {
            return readonly && (columnIndex == 1 || columnIndex == 2);
        }
        return false;
    }

    private static DatabaseMetaData metadata(String productName) {
        return (DatabaseMetaData) Proxy.newProxyInstance(
                TestDataSources.class.getClassLoader(),
                new Class<?>[]{DatabaseMetaData.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getDatabaseProductName" -> productName;
                    case "getDatabaseProductVersion" -> "test-version";
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class || type == short.class || type == byte.class || type == long.class) {
            return 0;
        }
        if (type == float.class || type == double.class) {
            return 0D;
        }
        if (type == char.class) {
            return '\0';
        }
        return null;
    }
}
