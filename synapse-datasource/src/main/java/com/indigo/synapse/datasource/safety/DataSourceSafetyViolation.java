package com.indigo.synapse.datasource.safety;

/**
 * 数据源安全检查问题项。
 *
 * @param code 问题编码
 * @param message 问题说明
 * @param target 问题目标
 */
public record DataSourceSafetyViolation(String code, String message, String target) {
}
