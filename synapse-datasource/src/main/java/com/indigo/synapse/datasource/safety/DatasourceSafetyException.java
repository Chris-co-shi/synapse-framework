package com.indigo.synapse.datasource.safety;

/**
 * 数据源安全检查失败异常。
 */
public class DatasourceSafetyException extends RuntimeException {

    private final DataSourceSafetyReport report;

    public DatasourceSafetyException(DataSourceSafetyReport report) {
        super(report.message());
        this.report = report;
    }

    public DataSourceSafetyReport getReport() {
        return report;
    }
}
