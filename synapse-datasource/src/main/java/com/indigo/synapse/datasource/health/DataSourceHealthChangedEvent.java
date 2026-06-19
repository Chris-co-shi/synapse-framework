package com.indigo.synapse.datasource.health;

/**
 * 数据源健康状态变化事件。
 *
 * @param previous 变化前快照
 * @param current 变化后快照
 */
public record DataSourceHealthChangedEvent(
        DataSourceHealthSnapshot previous,
        DataSourceHealthSnapshot current
) {
}
