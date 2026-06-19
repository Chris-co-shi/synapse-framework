package com.indigo.synapse.datasource.dynamic;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * dynamic-datasource 运行时清单适配器。
 *
 * <p>该适配器只读取 DynamicRoutingDataSource 和 DynamicDataSourceProperties 的公开信息，
 * 不发起线程上下文切换，也不声明动态数据源注解。</p>
 */
public class DynamicDatasourceInventoryAdapter implements DatasourceInventory {

    private final DynamicRoutingDataSource routingDataSource;
    private final DynamicDataSourceProperties properties;
    private volatile Map<String, DataSource> dataSources = Map.of();

    public DynamicDatasourceInventoryAdapter(
            DynamicRoutingDataSource routingDataSource,
            DynamicDataSourceProperties properties
    ) {
        this.routingDataSource = routingDataSource;
        this.properties = properties;
    }

    @Override
    public Map<String, DataSource> refreshInventory() {
        Map<String, DataSource> snapshot = new LinkedHashMap<>(routingDataSource.getDataSources());
        dataSources = Map.copyOf(snapshot);
        return dataSources;
    }

    @Override
    public Map<String, DataSource> getDataSources() {
        if (dataSources.isEmpty()) {
            return refreshInventory();
        }
        return dataSources;
    }

    @Override
    public Optional<String> getPrimaryName() {
        return Optional.ofNullable(properties.getPrimary()).filter(primary -> !primary.isBlank());
    }

    @Override
    public boolean isStrict() {
        return Boolean.TRUE.equals(properties.getStrict());
    }

    @Override
    public Optional<String> getJdbcUrl(String name) {
        if (properties.getDatasource() == null) {
            return Optional.empty();
        }
        DataSourceProperty property = properties.getDatasource().get(name);
        return property == null ? Optional.empty() : Optional.ofNullable(property.getUrl());
    }
}
