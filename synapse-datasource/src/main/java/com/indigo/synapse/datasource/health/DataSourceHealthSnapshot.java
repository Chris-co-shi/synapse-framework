package com.indigo.synapse.datasource.health;

import com.indigo.synapse.datasource.descriptor.DataSourceRole;

import java.time.Instant;

/**
 * 数据源健康状态快照。
 *
 * <p>该 record 属于 `synapse-datasource` 健康注册表边界，由 {@link DataSourceHealthChecker}
 * 写入，由路由协调、安全检查和启动诊断读取。它只保存脱敏状态信息，不持有 {@code DataSource}、
 * {@code Connection}、账号、密码或完整 JDBC URL。</p>
 *
 * <p>实例不可变、线程安全。状态机计数语义由 {@link DataSourceHealthChecker} 保证：稳定 UP 时计数归零，
 * RECOVERING 期间才累计成功次数，失败次数用于 DEGRADED/DOWN 阈值判断。</p>
 */
public record DataSourceHealthSnapshot(
        String dataSourceName,
        String group,
        DataSourceHealthStatus status,
        int failureCount,
        int successCount,
        Instant lastSuccessAt,
        Instant lastFailureAt,
        String lastFailureMessage,
        DataSourceRole detectedRole,
        String databaseProductName,
        String databaseVersion,
        String roleDetectionMessage
) {
    public DataSourceHealthSnapshot(
            String dataSourceName,
            String group,
            DataSourceHealthStatus status,
            int failureCount,
            int successCount,
            Instant lastSuccessAt,
            Instant lastFailureAt,
            String lastFailureMessage
    ) {
        this(dataSourceName, group, status, failureCount, successCount, lastSuccessAt, lastFailureAt,
                lastFailureMessage, null, null, null, null);
    }
}
