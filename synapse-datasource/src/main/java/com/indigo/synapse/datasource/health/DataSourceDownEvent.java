package com.indigo.synapse.datasource.health;

/**
 * 数据源进入 DOWN 状态事件。
 *
 * @param snapshot 当前健康快照
 */
public record DataSourceDownEvent(DataSourceHealthSnapshot snapshot) {
}
