package com.indigo.synapse.datasource.router;

import com.indigo.synapse.datasource.properties.SynapseDatasourceProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultDataSourceRouterTest {

    private final DefaultDataSourceRouter router = new DefaultDataSourceRouter(new SynapseDatasourceProperties());

    @Test
    void shouldRouteWriteDdlAndCallToMaster() {
        assertThat(route(new DataSourceRouteRequest(DataSourceOperation.WRITE, false, false, false, null)).target())
                .isEqualTo(RouteTarget.MASTER);
        assertThat(route(new DataSourceRouteRequest(DataSourceOperation.DDL, false, false, false, null)).target())
                .isEqualTo(RouteTarget.MASTER);
        assertThat(route(new DataSourceRouteRequest(DataSourceOperation.CALL, false, false, false, null)).target())
                .isEqualTo(RouteTarget.MASTER);
    }

    @Test
    void shouldRouteReadToMasterWhenContextRequiresMaster() {
        assertThat(route(new DataSourceRouteRequest(DataSourceOperation.READ, true, false, false, null)).reason())
                .isEqualTo(RouteReason.TRANSACTION_ACTIVE);
        assertThat(route(new DataSourceRouteRequest(DataSourceOperation.READ, false, true, false, null)).reason())
                .isEqualTo(RouteReason.AFTER_WRITE_READ);
        assertThat(route(new DataSourceRouteRequest(DataSourceOperation.READ, false, false, true, null)).reason())
                .isEqualTo(RouteReason.LOCK_QUERY);
    }

    @Test
    void shouldRouteNormalReadToSlaveGroup() {
        DataSourceRouteDecision decision = route(new DataSourceRouteRequest(DataSourceOperation.READ, false, false, false, null));

        assertThat(decision.target()).isEqualTo(RouteTarget.SLAVE_GROUP);
        assertThat(decision.group()).isEqualTo("slave");
        assertThat(decision.reason()).isEqualTo(RouteReason.READONLY_QUERY);
    }

    @Test
    void shouldRouteUnknownToMaster() {
        DataSourceRouteDecision decision = route(new DataSourceRouteRequest(DataSourceOperation.UNKNOWN, false, false, false, null));

        assertThat(decision.target()).isEqualTo(RouteTarget.MASTER);
        assertThat(decision.reason()).isEqualTo(RouteReason.UNKNOWN);
    }

    private DataSourceRouteDecision route(DataSourceRouteRequest request) {
        return router.route(request);
    }
}
