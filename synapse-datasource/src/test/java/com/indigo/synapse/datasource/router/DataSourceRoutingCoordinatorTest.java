package com.indigo.synapse.datasource.router;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceDescriptorRegistry;
import com.indigo.synapse.datasource.descriptor.DataSourceRole;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;
import com.indigo.synapse.datasource.failover.DataSourceFailoverManager;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.health.DataSourceHealthSnapshot;
import com.indigo.synapse.datasource.health.DataSourceHealthStatus;
import com.indigo.synapse.datasource.loadbalance.DataSourceCandidateFilter;
import com.indigo.synapse.datasource.loadbalance.RoundRobinLoadBalanceSelector;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceRoutingCoordinatorTest {

    @Test
    void shouldSelectHealthyPreferredReadonlyGroup() {
        SynapseDatasourceProperties properties = new SynapseDatasourceProperties();
        DataSourceDescriptorRegistry descriptors = new DataSourceDescriptorRegistry();
        DataSourceHealthRegistry health = new DataSourceHealthRegistry();
        descriptors.register(descriptor("master", "master", DataSourceRole.MASTER, true, false));
        descriptors.register(descriptor("report_1", "report", DataSourceRole.REPORT, false, true));
        health.update(snapshot("master", "master", DataSourceHealthStatus.UP));
        health.update(snapshot("report_1", "report", DataSourceHealthStatus.UP));
        DataSourceRoutingCoordinator coordinator = new DataSourceRoutingCoordinator(
                new DefaultDataSourceRoutingPolicy(properties),
                descriptors,
                new DataSourceCandidateFilter(properties, health),
                new RoundRobinLoadBalanceSelector(),
                new DataSourceFailoverManager(properties, descriptors, health)
        );

        DataSourceRouteDecision decision = coordinator.route(new DataSourceRouteRequest(
                DataSourceOperation.READ,
                false,
                false,
                false,
                "report"
        ));

        assertThat(decision.target()).isEqualTo(RouteTarget.SPECIFIC_DATASOURCE);
        assertThat(decision.dataSourceName()).isEqualTo("report_1");
        assertThat(decision.group()).isEqualTo("report");
    }

    @Test
    void shouldFallbackToMasterWhenNoReadonlyCandidateAvailable() {
        SynapseDatasourceProperties properties = new SynapseDatasourceProperties();
        DataSourceDescriptorRegistry descriptors = new DataSourceDescriptorRegistry();
        DataSourceHealthRegistry health = new DataSourceHealthRegistry();
        descriptors.register(descriptor("master", "master", DataSourceRole.MASTER, true, false));
        descriptors.register(descriptor("slave_1", "slave", DataSourceRole.SLAVE, false, true));
        health.update(snapshot("master", "master", DataSourceHealthStatus.UP));
        health.update(snapshot("slave_1", "slave", DataSourceHealthStatus.DOWN));
        DataSourceRoutingCoordinator coordinator = new DataSourceRoutingCoordinator(
                new DefaultDataSourceRoutingPolicy(properties),
                descriptors,
                new DataSourceCandidateFilter(properties, health),
                new RoundRobinLoadBalanceSelector(),
                new DataSourceFailoverManager(properties, descriptors, health)
        );

        DataSourceRouteDecision decision = coordinator.route(new DataSourceRouteRequest(
                DataSourceOperation.READ,
                false,
                false,
                false,
                null
        ));

        assertThat(decision.target()).isEqualTo(RouteTarget.MASTER);
        assertThat(decision.dataSourceName()).isEqualTo("master");
    }

    private static DataSourceDescriptor descriptor(
            String name,
            String group,
            DataSourceRole role,
            boolean primary,
            boolean readonly
    ) {
        return new DataSourceDescriptor(name, group, role, SynapseDbType.POSTGRESQL, primary, readonly, true, Map.of());
    }

    private static DataSourceHealthSnapshot snapshot(String name, String group, DataSourceHealthStatus status) {
        return new DataSourceHealthSnapshot(name, group, status, 0, 1, Instant.now(), null, null);
    }
}
