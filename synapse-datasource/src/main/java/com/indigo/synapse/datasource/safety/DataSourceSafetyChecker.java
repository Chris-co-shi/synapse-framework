package com.indigo.synapse.datasource.safety;

import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;

public class DataSourceSafetyChecker {

    private final SynapseDatasourceProperties properties;

    public DataSourceSafetyChecker(SynapseDatasourceProperties properties) {
        this.properties = properties;
    }

    public DataSourceSafetyReport checkPrimary(String primary) {
        String required = properties.getConvention().getMasterName();
        boolean safe = required.equals(primary);
        return safe
                ? DataSourceSafetyReport.safe(primary, "Primary datasource is valid.")
                : DataSourceSafetyReport.violation(primary, "Primary datasource must be " + required, "PRIMARY_NAME_MISMATCH");
    }

    public DataSourceSafetyReport checkStrict(boolean strict) {
        boolean required = properties.getConvention().isRequireStrict();
        boolean safe = !required || strict;
        return safe
                ? DataSourceSafetyReport.safe("dynamic-datasource", "Dynamic datasource strict mode is valid.")
                : DataSourceSafetyReport.violation(
                        "dynamic-datasource",
                        "spring.datasource.dynamic.strict must be true",
                        "STRICT_MODE_REQUIRED"
                );
    }

    public void assertSafe(DataSourceSafetyReport report) {
        if (!report.safe()) {
            throw new DatasourceSafetyException(report);
        }
    }
}
