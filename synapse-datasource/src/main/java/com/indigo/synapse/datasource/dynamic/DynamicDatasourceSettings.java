package com.indigo.synapse.datasource.dynamic;

/**
 * dynamic-datasource 运行时设置快照。
 *
 * @param primaryName primary 数据源名称
 * @param strict 是否启用 strict 模式
 */
public record DynamicDatasourceSettings(String primaryName, boolean strict) {
}
