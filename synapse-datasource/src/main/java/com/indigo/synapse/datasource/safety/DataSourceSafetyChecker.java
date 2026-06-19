package com.indigo.synapse.datasource.safety;

import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;

public class DataSourceSafetyChecker {

    private final SynapseDatasourceProperties properties;

    public DataSourceSafetyChecker(SynapseDatasourceProperties properties) {
        this.properties = properties;
    }

    public DataSourceSafetyReport checkPrimary(String primary) {
        String required = properties.getConvention().getRequiredPrimary();
        boolean safe = required.equals(primary);
        return new DataSourceSafetyReport(
                safe,
                primary,
                safe ? "Primary datasource is valid." : "Primary datasource must be " + required
        );
    }

    public DataSourceSafetyReport checkStrict(boolean strict) {
        boolean required = properties.getConvention().isRequireStrict();
        boolean safe = !required || strict;
        return new DataSourceSafetyReport(
                safe,
                "dynamic-datasource",
                safe ? "Dynamic datasource strict mode is valid." : "spring.datasource.dynamic.strict must be true"
        );
    }
}
