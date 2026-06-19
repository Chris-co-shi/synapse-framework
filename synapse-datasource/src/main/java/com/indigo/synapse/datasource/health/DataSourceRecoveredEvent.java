package com.indigo.synapse.datasource.health;

/**
 * 数据源恢复为 UP 状态事件。
 *
 * @param snapshot 当前健康快照
 */
public record DataSourceRecoveredEvent(DataSourceHealthSnapshot snapshot) {
}
