package com.indigo.synapse.datasource.health;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DataSourceHealthRegistry {

    private final Map<String, DataSourceHealthSnapshot> snapshots = new ConcurrentHashMap<>();

    /**
     * 更新指定数据源的健康快照。
     *
     * @param snapshot 健康快照
     */
    public void update(DataSourceHealthSnapshot snapshot) {
        snapshots.put(snapshot.dataSourceName(), snapshot);
    }

    /**
     * 按数据源名称查找健康快照。
     *
     * @param dataSourceName 数据源名称
     * @return 健康快照
     */
    public Optional<DataSourceHealthSnapshot> find(String dataSourceName) {
        return Optional.ofNullable(snapshots.get(dataSourceName));
    }

    /**
     * 返回所有健康快照。
     *
     * @return 不可变健康快照集合
     */
    public Collection<DataSourceHealthSnapshot> findAll() {
        return List.copyOf(snapshots.values());
    }

    /**
     * 按数据源组查找健康快照。
     *
     * @param group 数据源组名
     * @return 不可变健康快照列表
     */
    public List<DataSourceHealthSnapshot> findByGroup(String group) {
        return snapshots.values().stream()
                .filter(snapshot -> snapshot.group().equals(group))
                .toList();
    }

    /**
     * 移除指定数据源健康快照。
     *
     * @param dataSourceName 数据源名称
     * @return 被移除的健康快照
     */
    public Optional<DataSourceHealthSnapshot> remove(String dataSourceName) {
        return Optional.ofNullable(snapshots.remove(dataSourceName));
    }

    /**
     * 清空健康注册表。
     */
    public void clear() {
        snapshots.clear();
    }

    /**
     * 判断数据源是否可作为读候选。
     *
     * @param dataSourceName 数据源名称
     * @return UP 或 DEGRADED 时返回 true
     */
    public boolean isAvailable(String dataSourceName) {
        return find(dataSourceName)
                .map(snapshot -> snapshot.status() == DataSourceHealthStatus.UP
                        || snapshot.status() == DataSourceHealthStatus.DEGRADED)
                .orElse(false);
    }

    /**
     * 判断数据源是否处于稳定 UP 状态。
     *
     * @param dataSourceName 数据源名称
     * @return 仅当存在快照且状态为 UP 时返回 true
     */
    public boolean isUp(String dataSourceName) {
        return find(dataSourceName)
                .map(snapshot -> snapshot.status() == DataSourceHealthStatus.UP)
                .orElse(false);
    }

    /**
     * 为新发现的数据源登记 UNKNOWN 初始快照。
     *
     * <p>如果快照已存在，本方法不会覆盖已有健康状态，保证 inventory 重复同步不会清空历史健康判断。</p>
     *
     * @param dataSourceName 数据源名称
     * @param group 数据源组
     */
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
