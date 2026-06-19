package com.indigo.synapse.datasource.health;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DataSourceHealthRegistry {

    private final Map<String, DataSourceHealthSnapshot> snapshots = new ConcurrentHashMap<>();

    public void update(DataSourceHealthSnapshot snapshot) {
        snapshots.put(snapshot.dataSourceName(), snapshot);
    }

    public Optional<DataSourceHealthSnapshot> find(String dataSourceName) {
        return Optional.ofNullable(snapshots.get(dataSourceName));
    }

    public Collection<DataSourceHealthSnapshot> findAll() {
        return snapshots.values();
    }

    public boolean isAvailable(String dataSourceName) {
        return find(dataSourceName)
                .map(snapshot -> snapshot.status() == DataSourceHealthStatus.UP
                        || snapshot.status() == DataSourceHealthStatus.DEGRADED)
                .orElse(false);
    }
}
