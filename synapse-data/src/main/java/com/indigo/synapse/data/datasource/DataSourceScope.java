package com.indigo.synapse.data.datasource;

public final class DataSourceScope implements AutoCloseable {

    private final String previousDataSourceName;
    private boolean closed;

    DataSourceScope(String previousDataSourceName) {
        this.previousDataSourceName = previousDataSourceName;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        DataSourceContext.use(previousDataSourceName);
    }
}
