package com.indigo.synapse.datasource.routing;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.indigo.synapse.datasource.definition.DatasourceKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatasourceRouteContextTest {

    @AfterEach
    void clear() {
        DynamicDataSourceContextHolder.clear();
    }

    @Test
    void shouldRestoreNestedScopeAndClearThreadState() {
        DatasourceRouteContext context = new DatasourceRouteContext(() -> false);

        try (DatasourceRouteScope ignored = context.open(new DatasourceKey("master"))) {
            assertThat(context.current()).contains(new DatasourceKey("master"));
            try (DatasourceRouteScope nested = context.open(new DatasourceKey("report"))) {
                assertThat(context.current()).contains(new DatasourceKey("report"));
            }
            assertThat(context.current()).contains(new DatasourceKey("master"));
        }

        assertThat(context.current()).isEmpty();
    }

    @Test
    void shouldRejectSwitchInsideActiveTransaction() {
        AtomicBoolean transactionActive = new AtomicBoolean();
        DatasourceRouteContext context = new DatasourceRouteContext(transactionActive::get);

        try (DatasourceRouteScope ignored = context.open(new DatasourceKey("master"))) {
            transactionActive.set(true);
            assertThatThrownBy(() -> context.open(new DatasourceKey("report")))
                    .isInstanceOf(DatasourceTransactionSwitchException.class);
            assertThat(context.current()).contains(new DatasourceKey("master"));
        }
    }

    @Test
    void shouldRejectFirstSelectionAfterTransactionStarted() {
        DatasourceRouteContext context = new DatasourceRouteContext(() -> true);

        assertThatThrownBy(() -> context.open(new DatasourceKey("master")))
                .isInstanceOf(DatasourceTransactionSwitchException.class);
        assertThat(context.current()).isEmpty();
    }
}
