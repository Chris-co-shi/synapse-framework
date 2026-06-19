package com.indigo.synapse.datasource.lifecycle;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorResolver;
import com.indigo.synapse.datasource.dynamic.DatasourceInventory;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 数据源 inventory 同步器。
 *
 * <p>该类属于 `synapse-datasource` 生命周期治理边界，主要调用方是
 * {@link DatasourceGovernanceLifecycle} 和 {@link ScheduledDataSourceHealthMonitor}。它把
 * {@link DatasourceInventory} 暴露的运行时清单同步到 {@link DataSourceDescriptorRegistry} 和
 * {@link DataSourceHealthRegistry}。</p>
 *
 * <p>它不创建、不关闭、不替换 dynamic-datasource 管理的数据源，不执行数据源上下文切换，不解析业务 SQL。
 * 同步方法通过锁串行化，线程安全、可重复调用且幂等。解析阶段全部成功后才提交注册表变更，避免异常造成新增/删除半完成。</p>
 */
public final class DatasourceInventorySynchronizer {

    private final DatasourceInventory inventory;
    private final DataSourceDescriptorResolver descriptorResolver;
    private final DataSourceDescriptorRegistry descriptorRegistry;
    private final DataSourceHealthRegistry healthRegistry;
    private final ReentrantLock lock = new ReentrantLock();

    public DatasourceInventorySynchronizer(
            DatasourceInventory inventory,
            DataSourceDescriptorResolver descriptorResolver,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceHealthRegistry healthRegistry
    ) {
        this.inventory = inventory;
        this.descriptorResolver = descriptorResolver;
        this.descriptorRegistry = descriptorRegistry;
        this.healthRegistry = healthRegistry;
    }

    /**
     * 同步运行时 inventory 到治理注册表。
     *
     * <p>新增节点会注册描述符并登记 UNKNOWN 健康快照；已有节点会覆盖描述符并保留健康状态；
     * 删除节点会从两个注册表移除。方法返回不可修改快照。若解析过程中抛出异常，注册表保持调用前状态。</p>
     *
     * @return 同步后的 inventory 快照
     */
    public DatasourceInventorySnapshot synchronize() {
        lock.lock();
        try {
            Map<String, DataSource> dataSources = inventory.refreshInventory();
            List<DataSourceDescriptor> resolvedDescriptors = new ArrayList<>();
            dataSources.forEach((name, dataSource) -> resolvedDescriptors.add(descriptorResolver.resolve(
                    name,
                    dataSource,
                    inventory.getJdbcUrl(name),
                    inventory.getPrimaryName()
            )));

            Set<String> activeNames = dataSources.keySet();
            descriptorRegistry.findAll().stream()
                    .map(DataSourceDescriptor::name)
                    .filter(name -> !activeNames.contains(name))
                    .toList()
                    .forEach(name -> {
                        descriptorRegistry.remove(name);
                        healthRegistry.remove(name);
                    });
            descriptorRegistry.registerAll(resolvedDescriptors);
            resolvedDescriptors.forEach(descriptor -> healthRegistry.registerUnknown(descriptor.name(), descriptor.group()));
            return new DatasourceInventorySnapshot(
                    dataSources,
                    resolvedDescriptors,
                    inventory.getPrimaryName(),
                    inventory.isStrict()
            );
        } finally {
            lock.unlock();
        }
    }
}
