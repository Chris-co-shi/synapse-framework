package com.indigo.synapse.datasource.lifecycle;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 数据源 inventory 同步快照。
 *
 * <p>该 record 属于 `synapse-datasource` 生命周期治理边界，由
 * {@link DatasourceInventorySynchronizer} 生成，供启动生命周期、定时健康检查和测试读取。它只保存当前有效
 * DataSource 映射、解析后的描述符和 primary 名称，不关闭、包装或持有连接。</p>
 *
 * <p>实例不可变、线程安全。返回的 map 和 list 均为不可修改副本，调用方不能通过快照修改内部注册表。</p>
 *
 * @param dataSources 当前有效数据源快照
 * @param descriptors 当前有效描述符快照
 * @param primaryName runtime inventory 显式 primary 名称
 * @param strict runtime inventory strict 模式
 */
public record DatasourceInventorySnapshot(
        Map<String, DataSource> dataSources,
        List<DataSourceDescriptor> descriptors,
        Optional<String> primaryName,
        boolean strict
) {
    public DatasourceInventorySnapshot {
        dataSources = dataSources == null ? Map.of() : Map.copyOf(dataSources);
        descriptors = descriptors == null ? List.of() : List.copyOf(descriptors);
        primaryName = primaryName == null ? Optional.empty() : primaryName;
    }
}
