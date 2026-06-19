package com.indigo.synapse.datasource.testsupport;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public final class TestDataSources {

    private TestDataSources() {
    }

    public static DataSource healthy(String productName) {
        return proxy(productName, true);
    }

    public static DataSource failing(String productName) {
        return proxy(productName, false);
    }

    private static DataSource proxy(String productName, boolean healthy) {
        return (DataSource) Proxy.newProxyInstance(
                TestDataSources.class.getClassLoader(),
                new Class<?>[]{DataSource.class},
                (proxy, method, args) -> {
                    if ("getConnection".equals(method.getName())) {
                        if (!healthy) {
                            throw new SQLException("connection failed");
                        }
                        return connection(productName);
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

    private static Connection connection(String productName) {
        return (Connection) Proxy.newProxyInstance(
                TestDataSources.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isValid" -> true;
                    case "isReadOnly" -> false;
                    case "createStatement" -> statement();
                    case "getMetaData" -> metadata(productName);
                    case "close" -> null;
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private static Statement statement() {
        return (Statement) Proxy.newProxyInstance(
                TestDataSources.class.getClassLoader(),
                new Class<?>[]{Statement.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "execute" -> true;
                    case "setQueryTimeout", "close" -> null;
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private static DatabaseMetaData metadata(String productName) {
        return (DatabaseMetaData) Proxy.newProxyInstance(
                TestDataSources.class.getClassLoader(),
                new Class<?>[]{DatabaseMetaData.class},
                (proxy, method, args) -> "getDatabaseProductName".equals(method.getName())
                        ? productName
                        : defaultValue(method.getReturnType())
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
