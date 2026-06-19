package com.indigo.synapse.datasource.failover;

import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;

public class DataSourceFailoverManager {

    private final SynapseDatasourceProperties properties;

    public DataSourceFailoverManager(SynapseDatasourceProperties properties) {
        this.properties = properties;
    }

    public FailoverDecision decideForRead(boolean hasAvailableReadDatasource) {
        if (hasAvailableReadDatasource) {
            return FailoverDecision.USE_AVAILABLE_READ_DATASOURCE;
        }
        return properties.getFailover().isReadFallbackToMaster()
                ? FailoverDecision.FALLBACK_TO_MASTER
                : FailoverDecision.FAIL_FAST;
    }

    public FailoverDecision decideForMaster(boolean masterAvailable) {
        if (masterAvailable) {
            return FailoverDecision.FALLBACK_TO_MASTER;
        }
        return properties.getFailover().isFailFastWhenMasterDown()
                ? FailoverDecision.FAIL_FAST
                : FailoverDecision.FALLBACK_TO_MASTER;
    }
}
