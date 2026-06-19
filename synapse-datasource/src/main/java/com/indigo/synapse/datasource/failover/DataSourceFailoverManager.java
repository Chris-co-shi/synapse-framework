package com.indigo.synapse.datasource.failover;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import com.indigo.synapse.datasource.router.DataSourceRouteDecision;
import com.indigo.synapse.datasource.router.RouteReason;
import com.indigo.synapse.datasource.router.RouteTarget;

public class DataSourceFailoverManager {

    private final SynapseDatasourceProperties properties;
    private final DataSourceDescriptorRegistry descriptorRegistry;
    private final DataSourceHealthRegistry healthRegistry;

    public DataSourceFailoverManager(SynapseDatasourceProperties properties) {
        this(properties, null, null);
    }

    public DataSourceFailoverManager(
            SynapseDatasourceProperties properties,
            DataSourceDescriptorRegistry descriptorRegistry,
            DataSourceHealthRegistry healthRegistry
    ) {
        this.properties = properties;
        this.descriptorRegistry = descriptorRegistry;
        this.healthRegistry = healthRegistry;
    }

    public FailoverDecision decideForRead(boolean hasAvailableReadDatasource) {
        if (hasAvailableReadDatasource) {
            return FailoverDecision.USE_SELECTED_DATASOURCE;
        }
        return properties.getFailover().isReadFallbackToMaster()
                ? FailoverDecision.FALLBACK_TO_MASTER
                : FailoverDecision.FAIL_FAST;
    }

    public FailoverDecision decideForMaster(boolean masterAvailable) {
        if (masterAvailable) {
            return FailoverDecision.USE_MASTER;
        }
        return properties.getFailover().isFailFastWhenMasterDown()
                ? FailoverDecision.FAIL_FAST
                : FailoverDecision.FALLBACK_TO_MASTER;
    }

    public DataSourceRouteDecision fallbackForRead(RouteReason reason) {
        if (!properties.getFailover().isReadFallbackToMaster()) {
            return new DataSourceRouteDecision(RouteTarget.SLAVE_GROUP, null, null, RouteReason.NO_AVAILABLE_SLAVE);
        }
        return master(reason);
    }

    public DataSourceRouteDecision fallbackForMaster(RouteReason reason) {
        if (descriptorRegistry == null || healthRegistry == null) {
            return master(reason);
        }
        boolean masterAvailable = descriptorRegistry.findPrimary()
                .map(primary -> healthRegistry.isAvailable(primary.name()))
                .orElse(false);
        if (!masterAvailable && properties.getFailover().isFailFastWhenMasterDown()) {
            return new DataSourceRouteDecision(RouteTarget.MASTER, null, null, RouteReason.NO_AVAILABLE_SLAVE);
        }
        return master(reason);
    }

    private DataSourceRouteDecision master(RouteReason reason) {
        return new DataSourceRouteDecision(
                RouteTarget.MASTER,
                properties.getConvention().getMasterName(),
                null,
                reason
        );
    }
}
