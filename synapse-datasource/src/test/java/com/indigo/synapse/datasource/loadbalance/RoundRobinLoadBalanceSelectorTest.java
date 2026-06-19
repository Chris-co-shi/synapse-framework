package com.indigo.synapse.datasource.loadbalance;

import com.indigo.synapse.datasource.descriptor.DataSourceDescriptor;
import com.indigo.synapse.datasource.descriptor.DataSourceRole;
import com.indigo.synapse.datasource.descriptor.SynapseDbType;
import com.indigo.synapse.datasource.health.DataSourceHealthRegistry;
import com.indigo.synapse.datasource.health.DataSourceHealthSnapshot;
import com.indigo.synapse.datasource.health.DataSourceHealthStatus;
import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RoundRobinLoadBalanceSelectorTest {

    @Test
    void shouldSelectCandidatesByRoundRobin() {
        RoundRobinLoadBalanceSelector selector = new RoundRobinLoadBalanceSelector();

        assertThat(selector.select(List.of(descriptor("slave_1"), descriptor("slave_2"))))
                .contains(descriptor("slave_1"));
        assertThat(selector.select(List.of(descriptor("slave_1"), descriptor("slave_2"))))
                .contains(descriptor("slave_2"));
    }

    @Test
    void shouldFilterDownDatasourceBeforeSelection() {
        DataSourceHealthRegistry registry = new DataSourceHealthRegistry();
        registry.update(snapshot("slave_1", DataSourceHealthStatus.DOWN));
        registry.update(snapshot("slave_2", DataSourceHealthStatus.UP));
        DataSourceCandidateFilter filter = new DataSourceCandidateFilter(new SynapseDatasourceProperties(), registry);
        RoundRobinLoadBalanceSelector selector = new RoundRobinLoadBalanceSelector();

        List<DataSourceDescriptor> candidates = filter.filter(
                "slave",
                DataSourceRole.SLAVE,
                List.of(descriptor("slave_1"), descriptor("slave_2"))
        );

        assertThat(selector.select(candidates)).contains(descriptor("slave_2"));
    }

    private static DataSourceDescriptor descriptor(String name) {
        return new DataSourceDescriptor(name, "slave", DataSourceRole.SLAVE, SynapseDbType.POSTGRESQL, false, true, true, Map.of());
    }

    private static DataSourceHealthSnapshot snapshot(String name, DataSourceHealthStatus status) {
        return new DataSourceHealthSnapshot(name, "slave", status, 0, 1, Instant.now(), null, null);
    }
}
