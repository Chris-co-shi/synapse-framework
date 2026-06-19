package com.indigo.synapse.datasource.safety;

import java.time.Instant;
import java.util.List;

/**
 * 数据源安全检查报告。
 *
 * @param safe 是否安全
 * @param dataSourceName 检查目标
 * @param message 摘要消息
 * @param violations 阻断性违规项
 * @param warnings 非阻断性告警项
 * @param checkedAt 检查时间
 */
public record DataSourceSafetyReport(
        boolean safe,
        String dataSourceName,
        String message,
        List<DataSourceSafetyViolation> violations,
        List<DataSourceSafetyViolation> warnings,
        Instant checkedAt
) {
    public DataSourceSafetyReport {
        violations = violations == null ? List.of() : List.copyOf(violations);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        checkedAt = checkedAt == null ? Instant.now() : checkedAt;
    }

    public static DataSourceSafetyReport safe(String dataSourceName, String message) {
        return new DataSourceSafetyReport(true, dataSourceName, message, List.of(), List.of(), Instant.now());
    }

    public static DataSourceSafetyReport violation(String dataSourceName, String message, String code) {
        return new DataSourceSafetyReport(
                false,
                dataSourceName,
                message,
                List.of(new DataSourceSafetyViolation(code, message, dataSourceName)),
                List.of(),
                Instant.now()
        );
    }

    public static DataSourceSafetyReport violation(
            String dataSourceName,
            String message,
            DataSourceSafetyViolationCode code
    ) {
        return violation(dataSourceName, message, code.name());
    }
}
