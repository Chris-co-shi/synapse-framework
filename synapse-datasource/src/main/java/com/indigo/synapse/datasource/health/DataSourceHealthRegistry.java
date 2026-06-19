package com.indigo.synapse.datasource.health;

import java.util.Collection;
import java.util.List;
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
        return List.copyOf(snapshots.values());
    }

    public List<DataSourceHealthSnapshot> findByGroup(String group) {
        return snapshots.values().stream()
                .filter(snapshot -> snapshot.group().equals(group))
                .toList();
    }

    public Optional<DataSourceHealthSnapshot> remove(String dataSourceName) {
        return Optional.ofNullable(snapshots.remove(dataSourceName));
    }

    public void clear() {
        snapshots.clear();
    }

    public boolean isAvailable(String dataSourceName) {
        return find(dataSourceName)
                .map(snapshot -> snapshot.status() == DataSourceHealthStatus.UP
                        || snapshot.status() == DataSourceHealthStatus.DEGRADED)
                .orElse(false);
    }

    public void registerUnknown(String dataSourceName, String group) {
        snapshots.putIfAbsent(dataSourceName, new DataSourceHealthSnapshot(
                dataSourceName,
                group,
                DataSourceHealthStatus.UNKNOWN,
                0,
                0,
                null,
                null,
                null
        ));
    }
}
