package com.indigo.synapse.datasource.safety;

/**
 * 数据源安全检查违规编码。
 *
 * <p>该枚举属于 `synapse-datasource` 安全检查边界，用于稳定表达启动期和运行期安全检查失败原因。
 * 它不包含业务错误码语义，不负责 HTTP 响应转换。</p>
 */
public enum DataSourceSafetyViolationCode {
    PRIMARY_MISSING,
    PRIMARY_NAME_MISMATCH,
    MASTER_DATASOURCE_MISSING,
    MASTER_UNAVAILABLE,
    MULTIPLE_PRIMARY_DATASOURCES,
    PRIMARY_ROLE_MISMATCH,
    STRICT_MODE_REQUIRED,
    UNKNOWN_DATABASE_TYPE,
    READONLY_ROLE_MISMATCH
}
