package com.indigo.synapse.data.datasource;

import java.util.Optional;

public final class DataSourceContext {

    private static final ThreadLocal<String> DATA_SOURCE = new ThreadLocal<>();

    private DataSourceContext() {
    }

    public static void use(String dataSourceName) {
        if (dataSourceName == null || dataSourceName.isBlank()) {
            DATA_SOURCE.remove();
            return;
        }
        DATA_SOURCE.set(dataSourceName);
    }

    public static Optional<String> current() {
        return Optional.ofNullable(DATA_SOURCE.get());
    }

    public static DataSourceScope scope(String dataSourceName) {
        Optional<String> previous = current();
        use(dataSourceName);
        return new DataSourceScope(previous.orElse(null));
    }

    public static void clear() {
        DATA_SOURCE.remove();
    }
}
